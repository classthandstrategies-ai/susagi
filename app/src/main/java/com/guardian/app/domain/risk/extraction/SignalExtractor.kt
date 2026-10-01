package com.guardian.app.domain.risk.extraction

import com.guardian.app.domain.risk.ScamSignal

/**
 * Minimal pure-Kotlin contract for scam signal extraction in SuSagi V1.
 *
 * Implemented deterministically by local heuristic extractors (Task 2)
 * and by semantic / model-based extractors (Task 3).
 */
interface SignalExtractor {

    /**
     * Extracts categorized [ScamSignal] evidence from the given [transcript].
     *
     * @param transcript Verbatim speech or message text.
     * @param language ISO language code (e.g., "en", "hi").
     * @param timestampMs Monotonic or wall-clock epoch timestamp associated with the transcript.
     * @return List of unique, contextualized observed signals.
     */
    fun extract(
        transcript: String,
        language: String = "en",
        timestampMs: Long = 0L
    ): List<ScamSignal>
}
