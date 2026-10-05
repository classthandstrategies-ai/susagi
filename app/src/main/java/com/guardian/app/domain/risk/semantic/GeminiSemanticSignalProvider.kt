package com.guardian.app.domain.risk.semantic

import com.guardian.app.domain.risk.ScamSignal

/**
 * Implementation of [SemanticSignalProvider] that orchestrates LLM-based semantic evidence extraction.
 *
 * Uses [SemanticModelTransport] to dispatch generation requests and [SemanticSignalJsonParser]
 * to extract validated, grounded [ScamSignal] evidence.
 *
 * Guarantees zero crash and safe degradation: on any network timeout, HTTP error (including 429),
 * missing credentials, or invalid response payloads, it safely returns [emptyList].
 */
class GeminiSemanticSignalProvider(
    private val transport: SemanticModelTransport,
    private val systemPrompt: String,
    private val parser: SemanticSignalJsonParser = SemanticSignalJsonParser
) : SemanticSignalProvider {

    override suspend fun extractSignals(
        transcript: String,
        language: String,
        timestampMs: Long
    ): List<ScamSignal> {
        if (transcript.isBlank()) {
            return emptyList()
        }

        return try {
            val rawResponse = transport.generate(systemPrompt, transcript)
            if (rawResponse.isBlank()) {
                emptyList()
            } else {
                val effectiveTime = if (timestampMs != 0L) timestampMs else System.currentTimeMillis()
                parser.parse(
                    jsonStr = rawResponse,
                    transcript = transcript,
                    timestampMs = effectiveTime
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
