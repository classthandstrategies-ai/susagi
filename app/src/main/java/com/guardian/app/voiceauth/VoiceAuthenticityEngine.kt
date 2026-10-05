package com.guardian.app.voiceauth

/**
 * Voice authenticity analysis contract.
 * 
 * Implementations analyze raw PCM audio to determine whether speech
 * is likely produced by a human or by a synthetic/voice-converted system.
 *
 * This output is EVIDENCE — it is NOT a final fraud verdict.
 * The authoritative risk decision remains with [com.guardian.app.domain.risk.RiskEngine].
 */
interface VoiceAuthenticityEngine {
    /**
     * Analyze a window of PCM-16 audio for voice authenticity.
     *
     * @param pcm16 PCM 16-bit signed samples
     * @param sampleRate Sample rate in Hz (typically 16000)
     * @return Assessment with label, probability, and confidence
     */
    suspend fun analyze(
        pcm16: ShortArray,
        sampleRate: Int = 16000
    ): VoiceAuthenticityAssessment
}

/**
 * Labels for voice authenticity classification.
 * These represent evidence states, not fraud verdicts.
 */
enum class VoiceAuthenticityLabel {
    /** Audio exhibits characteristics consistent with natural human speech */
    LIKELY_HUMAN,
    /** Audio exhibits patterns associated with synthetic or voice-converted speech */
    SYNTHETIC_LIKELY,
    /** Insufficient evidence or conflicting indicators */
    UNCERTAIN
}

/**
 * Assessment result from voice authenticity analysis of a single audio window.
 */
data class VoiceAuthenticityAssessment(
    /** Classification label */
    val label: VoiceAuthenticityLabel,
    /** Estimated probability that the audio is synthetic [0.0, 1.0] */
    val syntheticProbability: Float,
    /** Confidence in the assessment [0.0, 1.0] */
    val confidence: Float,
    /** Duration of audio actually analyzed in milliseconds */
    val analyzedDurationMs: Long,
    /** Identifier for the model/version used */
    val modelVersion: String,
    /** Inference latency in milliseconds */
    val inferenceLatencyMs: Long = 0L,
    /** Timestamp when analysis completed */
    val timestampMs: Long = System.currentTimeMillis()
) {
    init {
        require(syntheticProbability in 0f..1f) { "syntheticProbability must be in [0,1], was $syntheticProbability" }
        require(confidence in 0f..1f) { "confidence must be in [0,1], was $confidence" }
    }
}
