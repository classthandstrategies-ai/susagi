package com.guardian.app.domain.risk.semantic

import com.guardian.app.domain.risk.ScamSignal
import com.guardian.app.domain.risk.SignalContext
import com.guardian.app.domain.risk.SignalSource
import com.guardian.app.domain.risk.SignalType
import org.json.JSONObject
import java.util.Locale

/**
 * Pure JSON parser for converting structured Gemini semantic output into validated [ScamSignal] evidence.
 *
 * Implements strict zero-trust validation:
 * - Emits evidence signals only; never accepts risk scores, levels, or final verdicts.
 * - Enforces an explicit semantic allowlist on [SignalType], rejecting privileged/device types.
 * - Forces [SignalSource.SEMANTIC_MODEL] on all accepted signals, ignoring model-provided source strings.
 * - Strictly validates [SignalContext], discarding entries with invalid or fabricated context names.
 * - Enforces bounded, finite [0.0..1.0] numerical constraints on evidence strength and confidence.
 * - Grounds evidence spans against the verbatim input transcript, discarding ungrounded fabrications.
 * - Deterministically deduplicates identical signals, retaining the highest-strength observation.
 * - Safely returns [emptyList] on malformed JSON without falling back to keyword heuristics or crashing.
 */
object SemanticSignalJsonParser {

    /**
     * Authoritative allowlist of [SignalType]s permitted to be emitted by the semantic LLM.
     * Privileged system signals (e.g., trusted contact verification, reputation, synthetic voice,
     * external identity mismatch) are strictly prohibited and discarded.
     */
    val ALLOWED_SEMANTIC_SIGNAL_TYPES: Set<SignalType> = setOf(
        // Identity / Pretext
        SignalType.IDENTITY_CLAIM,
        SignalType.AUTHORITY_CLAIM,
        SignalType.BANK_COMPANY_CLAIM,
        SignalType.GOVERNMENT_LAW_ENFORCEMENT_CLAIM,
        SignalType.CONTRADICTION,

        // Requested Actions
        SignalType.MONEY_TRANSFER_REQUEST,
        SignalType.OTP_REQUEST,
        SignalType.PIN_REQUEST,
        SignalType.CVV_REQUEST,
        SignalType.PASSWORD_REQUEST,
        SignalType.SENSITIVE_INFO_REQUEST,
        SignalType.REMOTE_ACCESS_REQUEST,
        SignalType.SCREEN_SHARING_REQUEST,
        SignalType.SUSPICIOUS_APP_INSTALLATION,
        SignalType.SUSPICIOUS_LINK_ACTION,

        // Psychological Pressure
        SignalType.URGENCY,
        SignalType.FEAR,
        SignalType.THREAT,
        SignalType.AUTHORITY_PRESSURE,
        SignalType.GREED_REWARD,
        SignalType.SECRECY,
        SignalType.ISOLATION,
        SignalType.TIME_PRESSURE,

        // Supporting Semantic Evidence
        SignalType.INFORMATION_ASYMMETRY,
        SignalType.SUSPICIOUS_PHRASE_PATTERN
    )

    private data class DeduplicationKey(
        val type: SignalType,
        val context: SignalContext,
        val normalizedEvidence: String
    )

    /**
     * Parses the given [jsonStr] into validated semantic [ScamSignal] evidence grounded in [transcript].
     *
     * @param jsonStr Raw model output string.
     * @param transcript Verbatim input transcript against which evidence must be grounded.
     * @param timestampMs Epoch timestamp for captured signals.
     * @return Validated, deduplicated list of observed semantic signals.
     */
    fun parse(
        jsonStr: String,
        transcript: String,
        timestampMs: Long = System.currentTimeMillis()
    ): List<ScamSignal> {
        if (jsonStr.isBlank() || transcript.isBlank()) {
            return emptyList()
        }

        val cleanedJson = extractJsonPayload(jsonStr) ?: return emptyList()

        return try {
            val root = JSONObject(cleanedJson)
            val signalsArray = root.optJSONArray("signals") ?: return emptyList()

            val validSignals = mutableListOf<ScamSignal>()

            for (i in 0 until signalsArray.length()) {
                val item = signalsArray.optJSONObject(i) ?: continue

                // 1. SignalType validation & allowlist check
                val typeStr = item.optString("type", "").trim()
                if (typeStr.isEmpty()) continue
                val type = try {
                    SignalType.valueOf(typeStr)
                } catch (_: IllegalArgumentException) {
                    null
                } ?: continue

                if (type !in ALLOWED_SEMANTIC_SIGNAL_TYPES) {
                    continue
                }

                // 2. SignalContext validation
                val contextStr = item.optString("context", "").trim()
                if (contextStr.isEmpty()) continue
                val context = try {
                    SignalContext.valueOf(contextStr)
                } catch (_: IllegalArgumentException) {
                    null
                } ?: continue

                // 3. Strength & Confidence validation (strictly finite, clamped to [0.0, 1.0])
                val strength = parseBoundedFloat(item, "strength") ?: continue
                val confidence = parseBoundedFloat(item, "confidence") ?: continue

                // 4. Raw evidence grounding in input transcript
                val rawEvidence = item.optString("evidence", "")
                val groundedEvidence = groundEvidence(rawEvidence, transcript) ?: continue

                // 5. Source is ALWAYS forced by code to SEMANTIC_MODEL
                val signal = ScamSignal(
                    type = type,
                    context = context,
                    source = SignalSource.SEMANTIC_MODEL,
                    strength = strength,
                    confidence = confidence,
                    rawEvidence = groundedEvidence,
                    metadata = mapOf("provider" to "gemini_semantic"),
                    timestampMs = timestampMs
                )
                validSignals.add(signal)
            }

            // 6. Deterministic deduplication
            deduplicate(validSignals)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun extractJsonPayload(raw: String): String? {
        var text = raw.trim()
        if (text.contains("```json")) {
            text = text.substringAfter("```json").substringBefore("```").trim()
        } else if (text.contains("```")) {
            text = text.substringAfter("```").substringBefore("```").trim()
        }

        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1)
        }
        return null
    }

    private fun parseBoundedFloat(item: JSONObject, key: String): Float? {
        if (!item.has(key)) return null
        val raw = item.opt(key) ?: return null
        val doubleValue = when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull()
            else -> null
        } ?: return null

        if (doubleValue.isNaN() || doubleValue.isInfinite()) {
            return null
        }
        return doubleValue.coerceIn(0.0, 1.0).toFloat()
    }

    private fun groundEvidence(rawEvidence: String, transcript: String): String? {
        if (transcript.isBlank()) return null
        var trimmed = rawEvidence.trim()
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) ||
            (trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length >= 2) ||
            (trimmed.startsWith("“") && trimmed.endsWith("”") && trimmed.length >= 2)
        ) {
            trimmed = trimmed.substring(1, trimmed.length - 1).trim()
        }
        if (trimmed.isEmpty()) return null

        // Direct case-insensitive match
        val idx = transcript.indexOf(trimmed, ignoreCase = true)
        if (idx != -1) {
            return transcript.substring(idx, idx + trimmed.length)
        }

        // Normalized quotes match
        val normTranscript = transcript.replace('“', '"').replace('”', '"').replace('‘', '\'').replace('’', '\'')
        val normEvidence = trimmed.replace('“', '"').replace('”', '"').replace('‘', '\'').replace('’', '\'')
        val normIdx = normTranscript.indexOf(normEvidence, ignoreCase = true)
        if (normIdx != -1) {
            return transcript.substring(normIdx, normIdx + normEvidence.length)
        }

        return null
    }

    private fun deduplicate(signals: List<ScamSignal>): List<ScamSignal> {
        val deduplicated = LinkedHashMap<DeduplicationKey, ScamSignal>()
        for (signal in signals) {
            val key = DeduplicationKey(
                type = signal.type,
                context = signal.context,
                normalizedEvidence = signal.rawEvidence.trim().lowercase(Locale.ROOT)
            )
            val existing = deduplicated[key]
            if (existing == null) {
                deduplicated[key] = signal
            } else {
                // Retain highest strength, break ties with highest confidence
                val shouldReplace = when {
                    signal.strength > existing.strength -> true
                    signal.strength == existing.strength && signal.confidence > existing.confidence -> true
                    else -> false
                }
                if (shouldReplace) {
                    deduplicated[key] = signal
                }
            }
        }
        return deduplicated.values.toList()
    }
}
