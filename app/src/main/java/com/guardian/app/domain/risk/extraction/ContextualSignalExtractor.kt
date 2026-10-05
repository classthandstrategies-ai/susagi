package com.guardian.app.domain.risk.extraction

import com.guardian.app.domain.risk.ScamSignal
import com.guardian.app.domain.risk.SignalContext
import com.guardian.app.domain.risk.SignalSource
import com.guardian.app.domain.risk.SignalType
import java.util.Locale

/**
 * Deterministic local contextual extractor for SuSagi V1.
 *
 * Implements [SignalExtractor] to extract categorized [ScamSignal] evidence from conversation
 * transcripts with fine-grained, local context classification to distinguish active demands
 * from benign mentions, warnings, quotes, or negations.
 *
 * Design Guarantees:
 * - Deterministic: Identical text consistently produces identical signals.
 * - Sub-clause localized: Contrastive and coordinating clause boundaries prevent defensive
 *   or warning language from poisoning real active demands coexisting in the same utterance.
 * - Quote & Reported Speech Isolation: Quoted demands (straight/curly quotes) and reported
 *   speech framing are marked as [SignalContext.QUOTE] so they remain informational.
 * - Defensive Scam Advisories: Targeted advisory phrases (e.g. "warning people about",
 *   "beware of", "savdhan") produce [SignalContext.WARNING] and general scam-warning evidence
 *   without letting bare "scams" suppress active demands.
 * - Explicit Identity Boundary: Institutional references (bank, police) remain passive [SignalContext.MENTION]
 *   unless accompanied by an explicit speaker identity assertion pattern.
 * - Safe Credential Anaphora: Pronoun/referent inheritance only activates on explicit anaphoric
 *   references (e.g. "yours", "it", "that code") when no separate primary target exists.
 * - Zero external framework or cloud dependencies.
 */
class ContextualSignalExtractor : SignalExtractor {

    /**
     * Extracts all observed [ScamSignal]s from [transcript] according to the [SignalExtractor] contract.
     */
    override fun extract(
        transcript: String,
        language: String,
        timestampMs: Long
    ): List<ScamSignal> {
        if (transcript.isBlank()) return emptyList()

        val effectiveTimestamp = if (timestampMs > 0L) timestampMs else System.currentTimeMillis()
        val normalized = transcript.trim()
        val clauses = splitIntoClauses(normalized)

        val rawSignals = mutableListOf<ScamSignal>()
        var priorCredentialTarget: SignalType? = null
        var priorClauseWasAttribution = false
        var insideQuote = false

        for (clause in clauses) {
            val (clauseSignals, attributionFound, updatedInsideQuote) = extractFromClause(
                clause = clause,
                inheritedCredentialTarget = priorCredentialTarget,
                wasPriorAttribution = priorClauseWasAttribution,
                currentlyInsideQuote = insideQuote,
                timestampMs = effectiveTimestamp
            )

            insideQuote = updatedInsideQuote
            priorClauseWasAttribution = attributionFound

            for (signal in clauseSignals) {
                if (signal.type.isCredentialExtraction) {
                    priorCredentialTarget = signal.type
                }
                rawSignals.add(signal)
            }
        }

        // Deduplicate signals by Type + Context to avoid repeating redundant findings
        return rawSignals.distinctBy { Pair(it.type, it.context) }
    }

    /**
     * Convenience single-argument overload for local and test callers.
     */
    fun extract(transcript: String): List<ScamSignal> =
        extract(transcript, language = "en", timestampMs = 0L)

    fun extractSignals(transcript: String): List<ScamSignal> =
        extract(transcript)

    // -------------------------------------------------------------------------
    // Clause Segmentation
    // -------------------------------------------------------------------------

    /**
     * Splits an utterance into logical sub-clauses along punctuation and contrastive/conjunction
     * boundaries to ensure localized contextual scope.
     */
    private fun splitIntoClauses(text: String): List<String> {
        // Conjunctions and punctuation that demarcate distinct semantic assertions:
        // English: but, however, although, yet, nevertheless, whereas, while, so, and then, except
        // Hindi: lekin, magar, parantu, kintu, par, isliye
        val regex = Regex(
            """(?:\s*[,;!?\n]+\s*(?:but|however|although|yet|nevertheless|whereas|while|so|lekin|magar|parantu|kintu|isliye)?\s*|\s+(?:but|however|although|yet|nevertheless|whereas|while|so|lekin|magar|parantu|kintu|isliye)\s+|\s*(?<!\b(?:rs|inr|mr|mrs|ms|dr|approx|vs|no))\.\s+)""",
            RegexOption.IGNORE_CASE
        )
        return text.split(regex).map { it.trim() }.filter { it.isNotEmpty() }
    }

    // -------------------------------------------------------------------------
    // Clause-level Extraction Result
    // -------------------------------------------------------------------------

    private data class ClauseResult(
        val signals: List<ScamSignal>,
        val isAttributionClause: Boolean,
        val insideQuote: Boolean
    )

    // -------------------------------------------------------------------------
    // Clause-level Extraction
    // -------------------------------------------------------------------------

    private fun extractFromClause(
        clause: String,
        inheritedCredentialTarget: SignalType?,
        wasPriorAttribution: Boolean,
        currentlyInsideQuote: Boolean,
        timestampMs: Long
    ): ClauseResult {
        val lower = clause.lowercase(Locale.ROOT)
        val signals = mutableListOf<ScamSignal>()

        // --- Quote & Reported Speech Detection ---
        val hasEnclosingQuotes = (clause.startsWith("\"") && (clause.endsWith("\"") || clause.endsWith("\"."))) ||
                (clause.startsWith("“") && (clause.endsWith("”") || clause.endsWith("”."))) ||
                (clause.startsWith("'") && (clause.endsWith("'") || clause.endsWith("'."))) ||
                (clause.startsWith("‘") && (clause.endsWith("’") || clause.endsWith("’.")))

        val containsQuotedSpan = quotedSpanPattern.containsMatchIn(clause)
        val isReportedSpeech = reportedSpeechPattern.containsMatchIn(clause)
        val isAttributionClause = attributionClausePattern.containsMatchIn(clause)

        // Check if this clause toggles an unmatched quotation mark
        val doubleQuoteCount = clause.count { it == '"' || it == '“' || it == '”' }
        val singleQuoteCount = if (clause.startsWith("'") || clause.endsWith("'") || clause.contains(" '") || clause.contains("' ")) {
            clause.count { it == '\'' || it == '‘' || it == '’' }
        } else {
            0
        }
        val togglesQuote = (doubleQuoteCount % 2 != 0) || (singleQuoteCount % 2 != 0)
        val newInsideQuote = if (togglesQuote) !currentlyInsideQuote else currentlyInsideQuote

        val hasOpenQuote = clause.startsWith("\"") || clause.startsWith("“") || clause.startsWith("'") || clause.startsWith("‘")
        val isQuoted = currentlyInsideQuote || hasEnclosingQuotes || containsQuotedSpan || isReportedSpeech || wasPriorAttribution || hasOpenQuote

        // --- Defensive / Negation / Demand Flags ---
        val hasAdvisoryWarning = warningPattern.containsMatchIn(lower)
        val hasScamAdvisory = scamAdvisoryPattern.containsMatchIn(lower)
        val hasExplicitNegation = negationPattern.containsMatchIn(lower)
        val hasActiveCommand = commandPattern.containsMatchIn(lower)
        val hasActiveRequest = requestPattern.containsMatchIn(lower)

        // 1. Credentials (CVV, OTP, PIN, Password, Sensitive Info)
        val hasCvv = cvvPattern.containsMatchIn(lower)
        val hasOtp = otpPattern.containsMatchIn(lower)
        val hasPin = pinPattern.containsMatchIn(lower)
        val hasPassword = passwordPattern.containsMatchIn(lower)
        val hasSensitive = sensitivePattern.containsMatchIn(lower)

        // Resolve credential context with strict precedence
        val credentialContext = when {
            hasExplicitNegation -> SignalContext.NEGATION
            hasAdvisoryWarning -> SignalContext.WARNING
            isQuoted -> SignalContext.QUOTE
            hasActiveCommand -> SignalContext.COMMAND
            hasActiveRequest -> SignalContext.REQUEST
            else -> SignalContext.MENTION
        }

        if (hasCvv) {
            signals.add(ScamSignal(SignalType.CVV_REQUEST, credentialContext, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }
        if (hasOtp) {
            signals.add(ScamSignal(SignalType.OTP_REQUEST, credentialContext, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }
        if (hasPin) {
            signals.add(ScamSignal(SignalType.PIN_REQUEST, credentialContext, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }
        if (hasPassword) {
            signals.add(ScamSignal(SignalType.PASSWORD_REQUEST, credentialContext, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }
        if (hasSensitive) {
            signals.add(ScamSignal(SignalType.SENSITIVE_INFO_REQUEST, credentialContext, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        // 2. Financial Extraction (Money Transfer) & Currency Amounts
        val hasCurrencyAmount = currencyAmountPattern.containsMatchIn(clause)
        val hasMoneyTransfer = moneyTransferPattern.containsMatchIn(lower) || hasCurrencyAmount

        val hasMoneyCommand = moneyCommandPattern.containsMatchIn(lower)
        val hasMoneyRequest = moneyRequestPattern.containsMatchIn(lower)

        if (hasMoneyTransfer) {
            val context = when {
                hasExplicitNegation -> SignalContext.NEGATION
                hasAdvisoryWarning -> SignalContext.WARNING
                isQuoted -> SignalContext.QUOTE
                hasMoneyCommand -> SignalContext.COMMAND
                hasMoneyRequest -> SignalContext.REQUEST
                hasActiveCommand -> SignalContext.COMMAND
                hasActiveRequest -> SignalContext.REQUEST
                else -> SignalContext.MENTION
            }
            signals.add(ScamSignal(SignalType.MONEY_TRANSFER_REQUEST, context, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        // 3. Remote Access & Screen Sharing
        val hasAnyDesk = remoteToolPattern.containsMatchIn(lower)
        val hasScreenShare = screenSharePattern.containsMatchIn(lower)
        if (hasAnyDesk || hasScreenShare) {
            val context = when {
                hasExplicitNegation -> SignalContext.NEGATION
                hasAdvisoryWarning -> SignalContext.WARNING
                isQuoted -> SignalContext.QUOTE
                hasActiveCommand -> SignalContext.COMMAND
                hasActiveRequest -> SignalContext.REQUEST
                // Direct tool invocation like "Install AnyDesk" or "Download AnyDesk" is a COMMAND
                lower.contains("install") || lower.contains("download") -> SignalContext.COMMAND
                else -> SignalContext.MENTION
            }
            val type = if (hasScreenShare) SignalType.SCREEN_SHARING_REQUEST else SignalType.REMOTE_ACCESS_REQUEST
            signals.add(ScamSignal(type, context, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        // Safe Credential Anaphora Resolution:
        // ONLY inherit prior credential target when:
        // 1. Current clause does NOT have its own credential target.
        // 2. Current clause does NOT have a separate primary target (money transfer or remote access).
        // 3. Current clause contains an explicit referent pronoun/phrase (e.g. "yours", "it", "that code").
        val hasNoCredentialInClause = !hasCvv && !hasOtp && !hasPin && !hasPassword && !hasSensitive
        val hasNoOtherPrimaryTarget = !hasMoneyTransfer && !hasAnyDesk && !hasScreenShare
        if (hasNoCredentialInClause && hasNoOtherPrimaryTarget && inheritedCredentialTarget != null) {
            if (credentialAnaphoraPattern.containsMatchIn(lower)) {
                val context = when {
                    isQuoted -> SignalContext.QUOTE
                    hasActiveCommand -> SignalContext.COMMAND
                    else -> SignalContext.REQUEST
                }
                signals.add(ScamSignal(inheritedCredentialTarget, context, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
            }
        }

        // 4. Institutions: Law Enforcement & Authority Claims
        // Strict boundary: Only explicit speaker identity assertion constitutes an active claim.
        // Commands/requests directed at police (e.g. "Call the police now") remain MENTION.
        // Advisory statements (e.g. "Police are warning people about digital arrest scams") become WARNING.
        val hasAuthorityTopic = authorityTopicPattern.containsMatchIn(lower)
        if (hasAuthorityTopic) {
            val isExplicitAuthorityClaim = activeAuthorityIdentityPatterns.any { it.containsMatchIn(lower) }
            val context = when {
                isExplicitAuthorityClaim -> SignalContext.COMMAND
                hasScamAdvisory || hasAdvisoryWarning -> SignalContext.WARNING
                isQuoted -> SignalContext.QUOTE
                else -> SignalContext.MENTION
            }
            signals.add(ScamSignal(SignalType.AUTHORITY_CLAIM, context, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        // 5. Institutions: Bank / Company Claims
        // Strict boundary: Only explicit speaker identity assertion constitutes an active bank claim.
        // Topics (e.g. "Send the bank statement", "fix your bank account") remain MENTION.
        val hasBankTopic = bankTopicPattern.containsMatchIn(lower)
        if (hasBankTopic) {
            val isExplicitBankClaim = activeBankIdentityPatterns.any { it.containsMatchIn(lower) }
            val context = when {
                isExplicitBankClaim -> SignalContext.COMMAND
                hasAdvisoryWarning -> SignalContext.WARNING
                isQuoted -> SignalContext.QUOTE
                else -> SignalContext.MENTION
            }
            signals.add(ScamSignal(SignalType.BANK_COMPANY_CLAIM, context, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        // 6. Defensive Scam Advisory Signals
        // If actual advisory phrasing is present ("warning people about", "beware of", "savdhan", etc.),
        // emit SUSPICIOUS_PHRASE_PATTERN with WARNING so RiskEngine suppresses passive signals and scores 0.
        if (hasScamAdvisory) {
            signals.add(ScamSignal(SignalType.SUSPICIOUS_PHRASE_PATTERN, SignalContext.WARNING, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        // 7. Urgency & Time Pressure
        if (urgencyPattern.containsMatchIn(lower)) {
            val context = when {
                isQuoted -> SignalContext.QUOTE
                hasActiveCommand || hasMoneyCommand -> SignalContext.COMMAND
                hasActiveRequest || hasMoneyRequest -> SignalContext.REQUEST
                else -> SignalContext.MENTION
            }
            signals.add(ScamSignal(SignalType.URGENCY, context, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        // 8. Threats & Fear
        if (threatPattern.containsMatchIn(lower)) {
            val isCoerciveThreat = lower.contains("you are under") ||
                    lower.contains("you will be") ||
                    lower.contains("against you") ||
                    lower.contains("your account") ||
                    hasActiveCommand
            val context = when {
                hasScamAdvisory || hasAdvisoryWarning -> SignalContext.WARNING
                isQuoted -> SignalContext.QUOTE
                isCoerciveThreat -> SignalContext.COMMAND
                else -> SignalContext.MENTION
            }
            signals.add(ScamSignal(SignalType.THREAT, context, SignalSource.LOCAL_RULE, timestampMs = timestampMs, rawEvidence = clause))
        }

        return ClauseResult(
            signals = signals,
            isAttributionClause = isAttributionClause,
            insideQuote = newInsideQuote
        )
    }

    // -------------------------------------------------------------------------
    // Pattern Definitions
    // -------------------------------------------------------------------------

    companion object {
        val INSTANCE = ContextualSignalExtractor()

        fun extract(transcript: String): List<ScamSignal> = INSTANCE.extract(transcript)
        fun extract(transcript: String, language: String, timestampMs: Long): List<ScamSignal> =
            INSTANCE.extract(transcript, language, timestampMs)
        fun extractSignals(transcript: String): List<ScamSignal> = INSTANCE.extractSignals(transcript)

        // Credential targets
        private val cvvPattern = Regex("""\b(cvv|cvv2|security\s*code)\b""", RegexOption.IGNORE_CASE)
        private val otpPattern = Regex("""\b(otp|one[\s-]time[\s-]password|ओटीपी)\b""", RegexOption.IGNORE_CASE)
        private val pinPattern = Regex("""\b(pin|mpin|upi[\s-]pin|atm[\s-]pin|पिन)\b""", RegexOption.IGNORE_CASE)
        private val passwordPattern = Regex("""\b(password|passcode|पासवर्ड)\b""", RegexOption.IGNORE_CASE)
        private val sensitivePattern = Regex("""\b(aadhaar|pan[\s-]card|social[\s-]security|ssn|आधार|पैन)\b""", RegexOption.IGNORE_CASE)

        // Remote access
        private val remoteToolPattern = Regex("""\b(anydesk|teamviewer|quicksupport|rustdesk|zoho\s+assist)\b""", RegexOption.IGNORE_CASE)
        private val screenSharePattern = Regex("""\b(screen\s*shar(e|ing)|स्क्रीन\s*शेयर)\b""", RegexOption.IGNORE_CASE)

        // Currency amount constructions (₹, Rs, Rs., Rupees, etc.)
        private val currencyAmountPattern = Regex(
            """(₹\s*[\d,]+|\b(rs\.?|inr|rupees?|रुपये|रुपया)\s*[\d,]+|[\d,]+\s*(rupees?|rs\.?|inr|lakh|crore|रुपये|रुपया)\b)""",
            RegexOption.IGNORE_CASE
        )

        // Financial transfer keywords and verbs
        private val moneyTransferPattern = Regex(
            """\b(transfer|send|wire|pay|deposit|bhejo|daal\s+do|de\s+do)\b.*\b(money|funds|cash|amount|rupees?|rs\.?|inr|lakh|crore|पैसे|रुपये|₹\s*[\d,]+)\b|\b(money|funds|cash|amount|rupees?|rs\.?|inr|lakh|crore|पैसे|रुपये|₹\s*[\d,]+)\b.*\b(transfer|send|wire|pay|deposit|bhejo|daal\s+do|de\s+do)\b|\b(transfer|send)\s+money\b|\b(पैसे|रुपये)\s+(भेजो|ट्रांसफर|दो)\b|\bpaise\s+(bhejo|transfer|daalo|de\s+do)\b|\b(can|could|would)\s+you\s+(send|transfer|pay)\b|\bplease\s+(send|transfer|pay)\b""",
            RegexOption.IGNORE_CASE
        )

        private val moneyCommandPattern = Regex(
            """\b(turant|jaldi|immediately|now|right\s+now)\b.*\b(transfer|send|pay|bhejo)\b|\b(transfer|send|pay)\b.*\b(immediately|right\s+now|now|jaldi|turant)\b|^(transfer|send|pay)\s+(the\s+money|funds|me\s+money|₹|rs\.?)\b|\b(must\s+send|must\s+transfer|have\s+to\s+send|have\s+to\s+transfer)\b|\b(transfer\s+karo|paise\s+bhejo|bhejo|daal\s+do|pay\s+karo)\b""",
            RegexOption.IGNORE_CASE
        )

        private val moneyRequestPattern = Regex(
            """\b(can\s+you\s+send|could\s+you\s+send|would\s+you\s+send|please\s+send|kindly\s+send|please\s+transfer|kindly\s+transfer|can\s+you\s+pay|could\s+you\s+pay|please\s+pay|send\s+me|transfer\s+me)\b|\b(kripya\s+bhejein|bhejiye|de\s+dijiye|share\s+kijiye)\b""",
            RegexOption.IGNORE_CASE
        )

        // Defensive scam advisory phrasing (targeted: does NOT match bare "scam" or "scams")
        private val scamAdvisoryPattern = Regex(
            """\b(warning\s+(people|citizens|users|customers|everyone)?\s*(about)?|police\s+warn(s|ing)?|beware\s+of|scam\s+alert|fraud\s+alert|be\s+careful(\s+of)?|savdhan(\s+rahein)?|satark(\s+rahein)?|dhokhadhadi\s+se\s+bachein)\b""",
            RegexOption.IGNORE_CASE
        )

        // General advisory / defensive language
        private val warningPattern = Regex(
            """\b(never\s+share|do\s+not\s+share|don't\s+share|should\s+not\s+share|shouldn't\s+share|never\s+give|do\s+not\s+give|don't\s+give|never\s+ask|does\s+not\s+ask|doesn't\s+ask|will\s+never\s+ask|don't\s+ask\s+for|never\s+asks\s+for|never\s+send|do\s+not\s+send|don't\s+send|never\s+install|do\s+not\s+install|don't\s+install|never\s+download|do\s+not\s+download|don't\s+download|says\s+never|said\s+never|advised\s+never|beware\s+of|be\s+careful|warning|refuse|decline|kabhi\s+.*mat\s+(karna|dena|batao|batana|share)|kisi\s+ko\s+.*mat\s+(batao|batana|dena|share)|mana\s+kar\s+dein|satark|savdhan)\b""",
            RegexOption.IGNORE_CASE
        )

        // Explicit negation
        private val negationPattern = Regex(
            """\b(i\s+will\s+not|i\s+will\s+never|i\s+won't|we\s+never|i\s+refuse|no\s+bank\s+asks|main\s+kabhi\s+nahi|main\s+nahi\s+dunga)\b""",
            RegexOption.IGNORE_CASE
        )

        // Quotes and reported speech
        private val quotedSpanPattern = Regex(
            """["“][^"”]+["”]|(?<=^|\s)['‘][^'’]+['’](?=$|\s|[.,!?])"""
        )

        private val reportedSpeechPattern = Regex(
            """\b(caller|he|she|they|someone|person|scammer)\s+(said|asked|told\s+me|demanded|says)\b|\b(said|asked(\s+me)?|told\s+me|demanded)\s*[:,-]?\s*["'“‘]?\s*(give|send|tell|share|enter|type|transfer|install|download|pay)\b|\b(usne|caller\s+ne|unhone)\s+(bola|kaha|bol\s+raha\s+tha|keh\s+raha\s+tha)\b""",
            RegexOption.IGNORE_CASE
        )

        private val attributionClausePattern = Regex(
            """^(the\s+caller|she|he|they|someone|person|scammer)\s+(said|asked(\s+me)?|told\s+me|demanded)\s*[:,-]?$""",
            RegexOption.IGNORE_CASE
        )

        // Active demands: Commands vs Requests
        private val commandPattern = Regex(
            """\b(tell\s+me|give\s+me|send\s+me|share\s+with\s+me|enter|type|transfer|install|download)\b.*\b(now|immediately|right\s+now|jaldi|turant)\b|^(tell\s+me|give\s+me|send\s+me|send\s+your|give\s+your|share\s+your|enter\s+your|type\s+your|transfer\s+the|install|download)\b|\b(must\s+send|have\s+to\s+transfer|need\s+you\s+to\s+tell|need\s+you\s+to\s+send|need\s+your)\b|\b(batao|bataiye|bhejo|transfer\s+karo|install\s+karo|download\s+karo|turant\s+karein)\b""",
            RegexOption.IGNORE_CASE
        )

        private val requestPattern = Regex(
            """\b(please\s+(share|tell|send|enter|type|provide|input|give|download|install)|kindly\s+(provide|share|tell|send|enter|type|give)|can\s+you\s+(share|give|tell|send|enter|provide)|could\s+you\s+(share|give|tell|send|provide)|ask(ing)?\s+for\s+your|need\s+your|tell\s+me|give\s+me|send\s+me)\b|\b(bataiye|batao|dijiye|share\s+kijiye|kripya)\b""",
            RegexOption.IGNORE_CASE
        )

        // Credential anaphora: requires explicit referents like "yours", "it", "that code"
        private val credentialAnaphoraPattern = Regex(
            """\b(tell\s+me|give\s+me|send\s+me|share|provide|need\s+you\s+to\s+(tell|send|give|share))\s+(yours|it|mine|that\s+code|the\s+code|that\s+number|the\s+number|that\s+otp|that\s+pin)\b|\b(tell|give|send|share)\s+it\s+(to\s+me|now|immediately)\b|\b(apna\s+wala|woh\s+code|woh\s+batao|woh\s+bhejo)\b""",
            RegexOption.IGNORE_CASE
        )

        // Institution Topics
        private val authorityTopicPattern = Regex(
            """\b(police|cbi|cid|crime\s+branch|cyber\s+cell|customs|enforcement\s+directorate|ed|interpol|court|judge|inspector|magistrate|trai|पुलि[सश]|सीबीआई)\b""",
            RegexOption.IGNORE_CASE
        )

        private val bankTopicPattern = Regex(
            """\b(bank|sbi|hdfc|icici|axis|pnb|rbi|reserve\s+bank|bank\s+of\s+baroda|canara|बैंक)\b""",
            RegexOption.IGNORE_CASE
        )

        // Explicit Institution Speaker Identity Assertions
        private val activeAuthorityIdentityPatterns = listOf(
            Regex("""\b(i\s+am|i'm|this\s+is|speaking\s+from|calling\s+from|we\s+are)\b.*\b(police|cbi|cid|crime\s+branch|cyber\s+cell|customs|inspector|officer|agent|detective)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(inspector|officer|agent|detective)\s+[a-zA-Z]+\s+(from|with|at)\s+(delhi\s+police|mumbai\s+police|police|cbi|crime\s+branch)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bthis\s+is\s+(cbi|delhi\s+police|mumbai\s+police|the\s+police|crime\s+branch|cyber\s+cell)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(police|cbi|crime\s+branch)\s+se\s+(bol\s+raha|baat\s+kar\s+raha|call\s+kar\s+raha)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(main|hum)\s+.*\b(police|cbi|officer)\s+(se\s+hoon|hoon)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(police|cbi)\s+officer\s+calling\b""", RegexOption.IGNORE_CASE)
        )

        private val activeBankIdentityPatterns = listOf(
            Regex("""\b(i\s+am|i'm|this\s+is|speaking\s+from|calling\s+from|we\s+are|calling\s+on\s+behalf\s+of)\b.*\b(sbi|hdfc|icici|axis|pnb|rbi|the\s+bank|bank\s+fraud\s+department)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(bank|sbi|hdfc|icici|axis|rbi)\s+se\s+(bol\s+raha|baat\s+kar\s+raha|call\s+kar\s+raha)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(main|hum)\s+.*\b(bank|sbi|hdfc)\s+(se\s+hoon|se\s+hai)\b""", RegexOption.IGNORE_CASE)
        )

        // Urgency
        private val urgencyPattern = Regex(
            """\b(immediately|urgent|urgently|right\s+now|within\s+\d+\s+min(ute)?s?|hurry|quick|turant|jaldi|तुरंत|जल्दी)\b""",
            RegexOption.IGNORE_CASE
        )

        // Threat
        private val threatPattern = Regex(
            """\b(arrest|arrested|digital\s+arrest|warrant|jail|fir|blocked|suspended|penalt(y|ies)|गिरफ्तार|अरेस्ट|बंद\s+हो\s+जाएगा)\b""",
            RegexOption.IGNORE_CASE
        )
    }
}
