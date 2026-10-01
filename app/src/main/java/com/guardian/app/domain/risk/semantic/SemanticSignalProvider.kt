package com.guardian.app.domain.risk.semantic

import com.guardian.app.domain.risk.ScamSignal

/**
 * Asynchronous semantic evidence provider contract for SuSagi V1.
 *
 * Extracts structured semantic [ScamSignal] evidence from speech transcripts
 * via remote foundation models (e.g., Gemini).
 *
 * Architecturally separated from the synchronous deterministic local [com.guardian.app.domain.risk.extraction.SignalExtractor].
 */
interface SemanticSignalProvider {

    /**
     * Extracts structured semantic evidence signals from the given [transcript].
     *
     * @param transcript Verbatim speech or message text.
     * @param language ISO language code (e.g., "en", "hi").
     * @param timestampMs Monotonic or wall-clock epoch timestamp associated with the transcript.
     * @return Validated, deduplicated list of semantic [ScamSignal] evidence.
     */
    suspend fun extractSignals(
        transcript: String,
        language: String = "en",
        timestampMs: Long = 0L
    ): List<ScamSignal>
}
