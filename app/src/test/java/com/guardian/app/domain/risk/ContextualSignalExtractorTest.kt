package com.guardian.app.domain.risk

import com.guardian.app.domain.risk.extraction.SignalExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Behavior-focused unit tests for [ContextualSignalExtractor] and [SignalExtractor].
 *
 * Verifies all 30 acceptance requirements and completion patch rules:
 * 1. QUOTE context: quoted / reported scam demands are informational, not active speaker requests.
 * 2. Defensive scam-warning context: advisory phrasing produces WARNING / LOW risk without bare "scams" poisoning.
 * 3. Credential isolation / safe anaphora: unrelated commands do NOT inherit prior credential targets.
 * 4. Exact money requests with ₹ / currency: polite requests produce active REQUEST; statements remain MENTION.
 * 5. Roman-Hinglish money demand: "Turant paise transfer karo" and "Paise bhejo" are active demands.
 * 6. SignalExtractor pure Kotlin interface contract.
 */
class ContextualSignalExtractorTest {

    private lateinit var extractor: ContextualSignalExtractor
    private lateinit var engine: RiskEngine

    @Before
    fun setUp() {
        extractor = ContextualSignalExtractor()
        engine = RiskEngine()
    }

    // -------------------------------------------------------------------------
    // 1. Mandatory Test 1: warning + active CVV request in same utterance preserves active request
    // -------------------------------------------------------------------------
    @Test
    fun test1_warningAndActiveCvvRequest_preservesActiveRequest() {
        val text = "Banks never ask for your CVV, but tell me your CVV now."
        val signals = extractor.extract(text)

        // 1. Active CVV request must be present
        val activeCvv = signals.find { it.type == SignalType.CVV_REQUEST && it.context.isActiveDemand }
        assertTrue("Active CVV request must be detected", activeCvv != null)
        assertTrue(activeCvv?.context == SignalContext.COMMAND || activeCvv?.context == SignalContext.REQUEST)

        // 2. Defensive CVV warning evidence may be preserved
        val warningCvv = signals.find { it.type == SignalType.CVV_REQUEST && it.context.isBenignOrDefensive }
        assertTrue("Defensive warning evidence should be preserved", warningCvv != null)

        // 3. RiskEngine evaluation must reflect active demand (not suppressed to 0)
        val assessment = engine.evaluate(signals)
        assertTrue("Score ${assessment.score} must reflect active credential extraction", assessment.score >= RiskPolicy.DEFAULT.credentialRequestBase)
        assertEquals(RiskLevel.MEDIUM, assessment.level)
    }

    // -------------------------------------------------------------------------
    // 2. Mandatory Test 2: word "scams" does not suppress active OTP request
    // -------------------------------------------------------------------------
    @Test
    fun test2_wordScams_doesNotSuppressActiveOtpRequest() {
        val text = "There are many scams, so send me your OTP now."
        val signals = extractor.extract(text)

        // Active OTP request must be detected
        val activeOtp = signals.find { it.type == SignalType.OTP_REQUEST && it.context.isActiveDemand }
        assertTrue("Active OTP request must be detected despite the word 'scams'", activeOtp != null)
        assertEquals(SignalContext.COMMAND, activeOtp?.context)

        // RiskEngine evaluation
        val assessment = engine.evaluate(signals)
        assertTrue("Risk score ${assessment.score} should be elevated", assessment.score >= RiskPolicy.DEFAULT.credentialRequestBase)
        assertEquals(RiskLevel.MEDIUM, assessment.level)
    }

    // -------------------------------------------------------------------------
    // 3. Mandatory Test 3: genuine OTP warning remains defensive
    // -------------------------------------------------------------------------
    @Test
    fun test3_genuineOtpWarning_remainsDefensiveWithoutActiveDemand() {
        val text = "Never share your OTP; if anyone asks, refuse."
        val signals = extractor.extract(text)

        // Must produce warning / defensive evidence
        val warningOtp = signals.find { it.type == SignalType.OTP_REQUEST && it.context.isBenignOrDefensive }
        assertTrue("Warning evidence must be detected", warningOtp != null)
        assertEquals(SignalContext.WARNING, warningOtp?.context)

        // Must NOT produce active demand
        val activeOtp = signals.find { it.type == SignalType.OTP_REQUEST && it.context.isActiveDemand }
        assertTrue("Must NOT detect any active OTP demand", activeOtp == null)

        // RiskEngine evaluation: Produces 0 score
        val assessment = engine.evaluate(signals)
        assertEquals(0, assessment.score)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    // -------------------------------------------------------------------------
    // 4. Mandatory Test 4: "Call the police now" does NOT become active law-enforcement identity claim
    // -------------------------------------------------------------------------
    @Test
    fun test4_callThePoliceNow_doesNotBecomeActiveAuthorityClaim() {
        val text = "Call the police now."
        val signals = extractor.extract(text)

        // Must NOT be an active identity claim
        val activeAuthority = signals.find { it.type.isIdentityOrImpersonation && it.context.isActiveDemand }
        assertTrue("Institutional reference must NOT become active identity claim", activeAuthority == null)

        // If authority signal emitted, it must be MENTION
        val authoritySignal = signals.find { it.type == SignalType.AUTHORITY_CLAIM }
        if (authoritySignal != null) {
            assertEquals(SignalContext.MENTION, authoritySignal.context)
        }

        // RiskEngine evaluation remains LOW
        val assessment = engine.evaluate(signals)
        assertTrue("Score ${assessment.score} must remain low", assessment.score <= RiskLevel.LOW_MAX)
    }

    // -------------------------------------------------------------------------
    // 5. Mandatory Test 5: "I am Inspector Sharma from Delhi Police" DOES become active claim
    // -------------------------------------------------------------------------
    @Test
    fun test5_inspectorSharmaFromDelhiPolice_becomesActiveAuthorityClaim() {
        val text = "I am Inspector Sharma from Delhi Police."
        val signals = extractor.extract(text)

        val activeAuthority = signals.find { it.type == SignalType.AUTHORITY_CLAIM && it.context.isActiveDemand }
        assertTrue("Explicit speaker identity assertion must become active claim", activeAuthority != null)
        assertTrue(activeAuthority?.context == SignalContext.COMMAND || activeAuthority?.context == SignalContext.REQUEST)
    }

    // -------------------------------------------------------------------------
    // 6. Mandatory Test 6: "Send the bank statement" does NOT become active bank identity claim
    // -------------------------------------------------------------------------
    @Test
    fun test6_sendTheBankStatement_doesNotBecomeActiveBankClaim() {
        val text = "Send the bank statement."
        val signals = extractor.extract(text)

        val activeBank = signals.find { it.type == SignalType.BANK_COMPANY_CLAIM && it.context.isActiveDemand }
        assertTrue("'bank statement' topic must NOT become an active bank identity claim", activeBank == null)

        val bankSignal = signals.find { it.type == SignalType.BANK_COMPANY_CLAIM }
        if (bankSignal != null) {
            assertEquals(SignalContext.MENTION, bankSignal.context)
        }

        val assessment = engine.evaluate(signals)
        assertTrue("Score ${assessment.score} must remain low", assessment.score <= RiskLevel.LOW_MAX)
    }

    // -------------------------------------------------------------------------
    // 7. Mandatory Test 7: "I am calling from SBI fraud department" DOES become active bank claim
    // -------------------------------------------------------------------------
    @Test
    fun test7_callingFromSbiFraudDepartment_becomesActiveBankClaim() {
        val text = "I am calling from SBI fraud department."
        val signals = extractor.extract(text)

        val activeBank = signals.find { it.type == SignalType.BANK_COMPANY_CLAIM && it.context.isActiveDemand }
        assertTrue("Explicit caller identity assertion from bank must become active claim", activeBank != null)
        assertTrue(activeBank?.context == SignalContext.COMMAND || activeBank?.context == SignalContext.REQUEST)
    }

    // -------------------------------------------------------------------------
    // 8. Mandatory Test 8: "Install AnyDesk so I can fix your bank account"
    // keeps remote access active but does not fabricate an active bank identity claim
    // -------------------------------------------------------------------------
    @Test
    fun test8_installAnyDeskFixBankAccount_remoteAccessActive_bankNotActiveClaim() {
        val text = "Install AnyDesk so I can fix your bank account."
        val signals = extractor.extract(text)

        // 1. Remote access request must be active
        val remoteAccess = signals.find { it.type == SignalType.REMOTE_ACCESS_REQUEST && it.context.isActiveDemand }
        assertTrue("Remote access demand must be active", remoteAccess != null)

        // 2. Bank reference must NOT be fabricated into an active identity claim
        val activeBank = signals.find { it.type == SignalType.BANK_COMPANY_CLAIM && it.context.isActiveDemand }
        assertTrue("Bank account reference must NOT become active bank claim", activeBank == null)

        val bankSignal = signals.find { it.type == SignalType.BANK_COMPANY_CLAIM }
        if (bankSignal != null) {
            assertEquals(SignalContext.MENTION, bankSignal.context)
        }

        // 3. RiskEngine evaluation: remote access is scored, without fake bank impersonation
        val assessment = engine.evaluate(signals)
        assertTrue("Score ${assessment.score} should reflect remote access takeover", assessment.score >= RiskPolicy.DEFAULT.remoteAccessRequestBase)
    }

    // -------------------------------------------------------------------------
    // 9. Mandatory Test 9: mixed warning + active request preserves both evidence contexts where applicable
    // -------------------------------------------------------------------------
    @Test
    fun test9_mixedWarningAndActiveRequest_preservesBothEvidenceContexts() {
        val text = "The bank says never share OTP, but I need you to tell me yours now."
        val signals = extractor.extract(text)

        // 1. Warning evidence is preserved
        val warningOtp = signals.find { it.type == SignalType.OTP_REQUEST && it.context.isBenignOrDefensive }
        assertTrue("Warning evidence must be preserved", warningOtp != null)

        // 2. Active request is preserved
        val activeOtp = signals.find { it.type == SignalType.OTP_REQUEST && it.context.isActiveDemand }
        assertTrue("Active OTP demand must survive and be detected", activeOtp != null)

        // 3. RiskEngine confirms active request is not suppressed
        val assessment = engine.evaluate(signals)
        assertTrue("Active demand should produce elevated threat", assessment.score >= RiskPolicy.DEFAULT.credentialRequestBase)
        assertEquals(RiskLevel.MEDIUM, assessment.level)
    }

    // -------------------------------------------------------------------------
    // COMPLETION PATCH 1: Restore QUOTE context
    // -------------------------------------------------------------------------

    @Test
    fun testQuote_doubleQuotes_theCallerSaidGiveMeYourOtp() {
        val text = "The caller said \"give me your OTP\"."
        val signals = extractor.extract(text)

        // Relevant evidence signal exists
        val otpSignal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue("OTP signal must be detected", otpSignal != null)

        // Context must be QUOTE
        assertEquals(SignalContext.QUOTE, otpSignal?.context)

        // Quoted demand must NOT be treated as active demand
        assertFalse("Quoted demand must not be active", otpSignal?.context?.isActiveDemand ?: true)

        // RiskEngine evaluates to 0 (LOW)
        val assessment = engine.evaluate(signals)
        assertTrue("Quoted demand must produce low risk", assessment.score <= RiskLevel.LOW_MAX)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testQuote_singleQuotes_theCallerSaidGiveMeYourOtp() {
        val text = "The caller said 'give me your OTP'."
        val signals = extractor.extract(text)

        val otpSignal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue("OTP signal must be detected", otpSignal != null)
        assertEquals(SignalContext.QUOTE, otpSignal?.context)
        assertFalse(otpSignal?.context?.isActiveDemand ?: true)

        val assessment = engine.evaluate(signals)
        assertTrue("Quoted demand must produce low risk", assessment.score <= RiskLevel.LOW_MAX)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testQuote_sheAskedMeSendTheMoneyNow() {
        val text = "She asked me, \"send the money now\"."
        val signals = extractor.extract(text)

        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue("Money transfer signal must be detected", moneySignal != null)
        assertEquals(SignalContext.QUOTE, moneySignal?.context)
        assertFalse(moneySignal?.context?.isActiveDemand ?: true)

        val assessment = engine.evaluate(signals)
        assertTrue("Quoted demand must produce low risk", assessment.score <= RiskLevel.LOW_MAX)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testQuote_reportedSpeech_theCallerSaidGiveMeYourOtp() {
        val text = "The caller said give me your OTP."
        val signals = extractor.extract(text)

        val otpSignal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue("OTP signal must be detected", otpSignal != null)
        assertEquals(SignalContext.QUOTE, otpSignal?.context)
        assertFalse(otpSignal?.context?.isActiveDemand ?: true)

        val assessment = engine.evaluate(signals)
        assertTrue("Quoted demand must produce low risk", assessment.score <= RiskLevel.LOW_MAX)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    // -------------------------------------------------------------------------
    // COMPLETION PATCH 2: Restore defensive scam-warning context
    // -------------------------------------------------------------------------

    @Test
    fun testDefensiveScamWarning_policeWarningAboutDigitalArrestScams() {
        val text = "Police are warning people about digital arrest scams."
        val signals = extractor.extract(text)

        // Must produce defensive / warning evidence
        val warningSignals = signals.filter { it.context.isBenignOrDefensive }
        assertTrue("Must contain warning context signals", warningSignals.isNotEmpty())

        val authoritySignal = signals.find { it.type == SignalType.AUTHORITY_CLAIM }
        if (authoritySignal != null) {
            assertEquals(SignalContext.WARNING, authoritySignal.context)
        }

        val patternSignal = signals.find { it.type == SignalType.SUSPICIOUS_PHRASE_PATTERN }
        if (patternSignal != null) {
            assertEquals(SignalContext.WARNING, patternSignal.context)
        }

        // Must NOT produce active demand
        val activeDemand = signals.find { it.context.isActiveDemand }
        assertTrue("Must NOT have active demands", activeDemand == null)

        // RiskEngine MUST evaluate scenario as LOW / defensive (0 score)
        val assessment = engine.evaluate(signals)
        assertEquals(0, assessment.score)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testScamWarning_bewareOfScams_savdhanRahein() {
        val texts = listOf(
            "Beware of fake bank calls.",
            "Savdhan rahein kisi ko OTP mat dena."
        )

        for (text in texts) {
            val signals = extractor.extract(text)
            val assessment = engine.evaluate(signals)
            assertEquals("Text '$text' must produce LOW risk", RiskLevel.LOW, assessment.level)
            assertEquals("Text '$text' must produce score 0", 0, assessment.score)
        }
    }

    // -------------------------------------------------------------------------
    // COMPLETION PATCH 3: Fix unsafe inheritedCredentialTarget behavior
    // -------------------------------------------------------------------------

    @Test
    fun testCredentialIsolation_iReceivedAnOtpYesterday_sendMeMoneyNow() {
        val text = "I received an OTP yesterday. Send me money now."
        val signals = extractor.extract(text)

        // 1. Passive OTP mention
        val otpSignal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue("OTP signal should exist", otpSignal != null)
        assertEquals(SignalContext.MENTION, otpSignal?.context)

        // 2. Active money request
        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST && it.context.isActiveDemand }
        assertTrue("Active money request must exist", moneySignal != null)

        // 3. NO active OTP request!
        val activeOtp = signals.find { it.type == SignalType.OTP_REQUEST && it.context.isActiveDemand }
        assertTrue("Must NOT inherit OTP into money command", activeOtp == null)
    }

    @Test
    fun testCredentialIsolation_neverShareCvv_transferTheMoneyNow() {
        val text = "Never share your CVV. Transfer the money now."
        val signals = extractor.extract(text)

        // 1. CVV warning
        val cvvSignal = signals.find { it.type == SignalType.CVV_REQUEST }
        assertTrue("CVV signal must exist", cvvSignal != null)
        assertEquals(SignalContext.WARNING, cvvSignal?.context)

        // 2. Active money request
        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST && it.context.isActiveDemand }
        assertTrue("Active money request must exist", moneySignal != null)

        // 3. NO active CVV request!
        val activeCvv = signals.find { it.type == SignalType.CVV_REQUEST && it.context.isActiveDemand }
        assertTrue("Must NOT inherit CVV into money command", activeCvv == null)
    }

    @Test
    fun testCredentialAnaphora_explicitReferent_inheritsCredential() {
        val text = "The bank says never share OTP, but I need you to tell me yours now."
        val signals = extractor.extract(text)

        // Explicit anaphora "yours" references OTP:
        val activeOtp = signals.find { it.type == SignalType.OTP_REQUEST && it.context.isActiveDemand }
        assertTrue("Explicit referent 'yours' must inherit OTP target", activeOtp != null)
    }

    // -------------------------------------------------------------------------
    // COMPLETION PATCH 4: Exact money-request acceptance cases (₹, Rs, Rs., Rupees)
    // -------------------------------------------------------------------------

    @Test
    fun testMoneyRequest_canYouSendMeRupees2000_isActiveRequest() {
        val text = "Can you send me ₹2,000?"
        val signals = extractor.extract(text)

        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue("Money transfer signal must be detected", moneySignal != null)
        assertEquals(SignalContext.REQUEST, moneySignal?.context)
        assertTrue(moneySignal?.context?.isActiveDemand ?: false)

        val assessment = engine.evaluate(signals)
        assertTrue("Assessment score ${assessment.score} should reflect money request", assessment.score >= RiskPolicy.DEFAULT.moneyTransferRequestBase)
    }

    @Test
    fun testMoneyMention_iPaidRupees2000Yesterday_isMention() {
        val text = "I paid ₹2,000 yesterday."
        val signals = extractor.extract(text)

        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue("Money signal detected", moneySignal != null)
        assertEquals(SignalContext.MENTION, moneySignal?.context)
        assertFalse(moneySignal?.context?.isActiveDemand ?: true)

        val assessment = engine.evaluate(signals)
        assertTrue("Money mention must produce low risk", assessment.score <= RiskLevel.LOW_MAX)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testMoneyCurrencyVariants_rsAndRupees() {
        val politeRs = "Could you please send Rs 5,000?"
        val signals = extractor.extract(politeRs)
        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue(moneySignal != null)
        assertEquals(SignalContext.REQUEST, moneySignal?.context)

        val statement = "I received Rs. 500 cashback."
        val mentionSignals = extractor.extract(statement)
        val mentionMoney = mentionSignals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue(mentionMoney != null)
        assertEquals(SignalContext.MENTION, mentionMoney?.context)
    }

    // -------------------------------------------------------------------------
    // COMPLETION PATCH 5: Roman-Hinglish money demand
    // -------------------------------------------------------------------------

    @Test
    fun testHinglishMoneyDemand_turantPaiseTransferKaro() {
        val text = "Turant paise transfer karo"
        val signals = extractor.extract(text)

        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue("Money transfer request must be detected", moneySignal != null)
        assertTrue("Context must be active demand", moneySignal?.context?.isActiveDemand ?: false)
        assertEquals(SignalContext.COMMAND, moneySignal?.context)

        val urgencySignal = signals.find { it.type == SignalType.URGENCY }
        assertTrue("Urgency signal must be detected", urgencySignal != null)
        assertEquals(SignalContext.COMMAND, urgencySignal?.context)

        val assessment = engine.evaluate(signals)
        assertTrue("Assessment score ${assessment.score} should be elevated", assessment.score >= RiskPolicy.DEFAULT.moneyTransferRequestBase)
    }

    @Test
    fun testHinglishMoneyDemand_paiseBhejo() {
        val text = "Paise bhejo"
        val signals = extractor.extract(text)

        val moneySignal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue("Money transfer request must be detected", moneySignal != null)
        assertTrue("Context must be active demand", moneySignal?.context?.isActiveDemand ?: false)
        assertEquals(SignalContext.COMMAND, moneySignal?.context)
    }

    // -------------------------------------------------------------------------
    // COMPLETION PATCH 6: SignalExtractor interface contract
    // -------------------------------------------------------------------------

    @Test
    fun testSignalExtractorInterfaceContract() {
        val extractorInterface: SignalExtractor = extractor
        val timestamp = 1727769600000L
        val signals = extractorInterface.extract("Give me your OTP immediately", language = "en", timestampMs = timestamp)

        assertFalse("Signals list should not be empty", signals.isEmpty())
        val otpSignal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(otpSignal != null)
        assertEquals(SignalContext.COMMAND, otpSignal?.context)
        assertEquals(timestamp, otpSignal?.timestampMs)
    }

    // -------------------------------------------------------------------------
    // 30 Acceptance Scope Items Exhaustive Verification
    // -------------------------------------------------------------------------

    @Test
    fun testScopeItem1_otpMention() {
        val signals = extractor.extract("I received an OTP for my online order.")
        val signal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.MENTION, signal?.context)
    }

    @Test
    fun testScopeItem2_otpWarning() {
        val signals = extractor.extract("Never share your OTP with anyone.")
        val signal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.WARNING, signal?.context)
    }

    @Test
    fun testScopeItem3_otpNegationRefusal() {
        val signals = extractor.extract("I will not share my OTP with you.")
        val signal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.NEGATION, signal?.context)
    }

    @Test
    fun testScopeItem4_otpDirectRequest() {
        val signals = extractor.extract("Give me your OTP now.")
        val signal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.COMMAND, signal?.context)
    }

    @Test
    fun testScopeItem5_otpRequestPlusUrgency() {
        val signals = extractor.extract("Send your OTP immediately!")
        val otp = signals.find { it.type == SignalType.OTP_REQUEST }
        val urgency = signals.find { it.type == SignalType.URGENCY }
        assertTrue(otp != null && otp.context.isActiveDemand)
        assertTrue(urgency != null)
    }

    @Test
    fun testScopeItem6_moneyMention() {
        val signals = extractor.extract("The total amount was ₹2,000 yesterday.")
        val signal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.MENTION, signal?.context)
    }

    @Test
    fun testScopeItem7_moneyRequest() {
        val signals = extractor.extract("Can you send me ₹2,000?")
        val signal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.REQUEST, signal?.context)
    }

    @Test
    fun testScopeItem8_moneyRequestPlusUrgency() {
        val signals = extractor.extract("Transfer the money immediately right now.")
        val money = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        val urgency = signals.find { it.type == SignalType.URGENCY }
        assertTrue(money != null && money.context.isActiveDemand)
        assertTrue(urgency != null)
    }

    @Test
    fun testScopeItem9_policeWarningAboutScams() {
        val signals = extractor.extract("Police are warning people about digital arrest scams.")
        val assessment = engine.evaluate(signals)
        assertEquals(0, assessment.score)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testScopeItem10_activePoliceLawEnforcementIdentityClaim() {
        val signals = extractor.extract("I am Inspector Sharma from Delhi Police.")
        val claim = signals.find { it.type == SignalType.AUTHORITY_CLAIM }
        assertTrue(claim != null)
        assertTrue(claim?.context?.isActiveDemand ?: false)
    }

    @Test
    fun testScopeItem11_digitalArrestPlusMoneyDemand() {
        val signals = extractor.extract("You are under digital arrest, transfer ₹50,000 immediately.")
        val threat = signals.find { it.type == SignalType.THREAT }
        val money = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        val urgency = signals.find { it.type == SignalType.URGENCY }
        assertTrue(threat != null)
        assertTrue(money != null && money.context.isActiveDemand)
        assertTrue(urgency != null)
        val assessment = engine.evaluate(signals)
        assertTrue("Digital arrest demand must be high risk", assessment.score >= 50)
    }

    @Test
    fun testScopeItem12_anydeskPassiveMention() {
        val signals = extractor.extract("My company uses AnyDesk for IT support.")
        val signal = signals.find { it.type == SignalType.REMOTE_ACCESS_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.MENTION, signal?.context)
    }

    @Test
    fun testScopeItem13_anydeskWarning() {
        val signals = extractor.extract("Never install AnyDesk for strangers.")
        val signal = signals.find { it.type == SignalType.REMOTE_ACCESS_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.WARNING, signal?.context)
    }

    @Test
    fun testScopeItem14_anydeskActiveRequest() {
        val signals = extractor.extract("Please download AnyDesk now.")
        val signal = signals.find { it.type == SignalType.REMOTE_ACCESS_REQUEST }
        assertTrue(signal != null)
        assertTrue(signal?.context?.isActiveDemand ?: false)
    }

    @Test
    fun testScopeItem15_bankReferencePlusRemoteAccessRequest() {
        val signals = extractor.extract("Install AnyDesk so I can fix your bank account.")
        val remote = signals.find { it.type == SignalType.REMOTE_ACCESS_REQUEST }
        val bank = signals.find { it.type == SignalType.BANK_COMPANY_CLAIM }
        assertTrue(remote != null && remote.context.isActiveDemand)
        assertTrue(bank != null)
        assertEquals(SignalContext.MENTION, bank?.context)
    }

    @Test
    fun testScopeItem16_cvvRequest() {
        val signals = extractor.extract("Tell me your CVV now.")
        val signal = signals.find { it.type == SignalType.CVV_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.COMMAND, signal?.context)
    }

    @Test
    fun testScopeItem17_cvvWarning() {
        val signals = extractor.extract("Banks will never ask for your CVV.")
        val signal = signals.find { it.type == SignalType.CVV_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.WARNING, signal?.context)
    }

    @Test
    fun testScopeItem18_passwordRequest() {
        val signals = extractor.extract("Please enter your password to proceed.")
        val signal = signals.find { it.type == SignalType.PASSWORD_REQUEST }
        assertTrue(signal != null)
        assertTrue(signal?.context?.isActiveDemand ?: false)
    }

    @Test
    fun testScopeItem19_aadhaarPanPassiveMention() {
        val signals = extractor.extract("I submitted my Aadhaar and PAN card yesterday.")
        val signal = signals.find { it.type == SignalType.SENSITIVE_INFO_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.MENTION, signal?.context)
    }

    @Test
    fun testScopeItem20_aadhaarPanRequest() {
        val signals = extractor.extract("Send me your Aadhaar card and PAN card details now.")
        val signal = signals.find { it.type == SignalType.SENSITIVE_INFO_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.COMMAND, signal?.context)
    }

    @Test
    fun testScopeItem21_quotedScamPhrase() {
        val signals = extractor.extract("The caller said 'give me your OTP'.")
        val signal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.QUOTE, signal?.context)
    }

    @Test
    fun testScopeItem22_multipleBenignMentions() {
        val signals = extractor.extract("I went to SBI bank and updated my PAN card details.")
        for (sig in signals) {
            assertEquals(SignalContext.MENTION, sig.context)
        }
        val assessment = engine.evaluate(signals)
        assertTrue("Benign mentions must produce low risk", assessment.score <= RiskLevel.LOW_MAX)
        assertEquals(RiskLevel.LOW, assessment.level)
    }

    @Test
    fun testScopeItem23_hinglishOtpRequest() {
        val signals = extractor.extract("Apna OTP batao jaldi.")
        val signal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(signal != null)
        assertTrue(signal?.context?.isActiveDemand ?: false)
    }

    @Test
    fun testScopeItem24_hinglishMoneyRequest() {
        val signals = extractor.extract("Turant paise transfer karo.")
        val signal = signals.find { it.type == SignalType.MONEY_TRANSFER_REQUEST }
        assertTrue(signal != null)
        assertTrue(signal?.context?.isActiveDemand ?: false)
    }

    @Test
    fun testScopeItem25_hinglishScamWarningPhrase() {
        val signals = extractor.extract("Savdhan rahein kisi ko OTP mat batana.")
        val signal = signals.find { it.type == SignalType.OTP_REQUEST }
        assertTrue(signal != null)
        assertEquals(SignalContext.WARNING, signal?.context)
    }

    @Test
    fun testScopeItem26_deduplication() {
        val text = "Give me your OTP. Give me your OTP. Give me your OTP."
        val signals = extractor.extract(text)
        val otpSignals = signals.filter { it.type == SignalType.OTP_REQUEST && it.context == SignalContext.COMMAND }
        assertEquals("Signals of same type and context must be deduplicated", 1, otpSignals.size)
    }

    @Test
    fun testScopeItem27_deterministicOutput() {
        val text = "The caller said 'give me your OTP', but I will never share my PIN. Turant paise transfer karo."
        val run1 = extractor.extract(text)
        val run2 = extractor.extract(text)
        assertEquals("Signal count must match across runs", run1.size, run2.size)
        for (i in run1.indices) {
            assertEquals("Signal $i type must match", run1[i].type, run2[i].type)
            assertEquals("Signal $i context must match", run1[i].context, run2[i].context)
        }
    }

    @Test
    fun testScopeItem28_warningAndActiveRequestInSameUtterance() {
        val text = "Banks never ask for your CVV, but tell me your CVV now."
        val signals = extractor.extract(text)
        assertTrue(signals.any { it.type == SignalType.CVV_REQUEST && it.context == SignalContext.WARNING })
        assertTrue(signals.any { it.type == SignalType.CVV_REQUEST && it.context.isActiveDemand })
    }

    @Test
    fun testScopeItem29_institutionMentionIsNotImpersonation() {
        val s1 = extractor.extract("Call the police now.")
        val auth1 = s1.find { it.type == SignalType.AUTHORITY_CLAIM }
        if (auth1 != null) assertEquals(SignalContext.MENTION, auth1.context)

        val s2 = extractor.extract("Send the bank statement.")
        val bank2 = s2.find { it.type == SignalType.BANK_COMPANY_CLAIM }
        if (bank2 != null) assertEquals(SignalContext.MENTION, bank2.context)
    }

    @Test
    fun testScopeItem30_explicitInstitutionIdentityAssertionIsActive() {
        val s1 = extractor.extract("This is CBI Crime Branch.")
        val auth = s1.find { it.type == SignalType.AUTHORITY_CLAIM }
        assertTrue(auth != null && auth.context.isActiveDemand)

        val s2 = extractor.extract("I am calling from SBI fraud department.")
        val bank = s2.find { it.type == SignalType.BANK_COMPANY_CLAIM }
        assertTrue(bank != null && bank.context.isActiveDemand)

        val s3 = extractor.extract("Bank se bol raha hoon.")
        val bankHindi = s3.find { it.type == SignalType.BANK_COMPANY_CLAIM }
        assertTrue(bankHindi != null && bankHindi.context.isActiveDemand)
    }

    @Test
    fun testEmptyAndBlankInputs() {
        assertTrue(extractor.extract("").isEmpty())
        assertTrue(extractor.extract("   \n\t  ").isEmpty())
        assertTrue(extractor.extract("Hello, how are you today?").isEmpty())
    }
}
