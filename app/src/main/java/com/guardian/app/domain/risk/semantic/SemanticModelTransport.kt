package com.guardian.app.domain.risk.semantic

/**
 * Minimal transport abstraction for calling a semantic LLM (e.g., Gemini).
 * Decouples network/HTTP/API concerns from semantic evidence orchestration and testing.
 */
fun interface SemanticModelTransport {

    /**
     * Executes generation against the semantic model with the specified [systemPrompt] and [transcript].
     *
     * @param systemPrompt Instructions framing the model's extraction behavior.
     * @param transcript Verbatim dialogue input.
     * @return Raw text/JSON response emitted by the model.
     */
    suspend fun generate(
        systemPrompt: String,
        transcript: String
    ): String
}
