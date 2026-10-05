package com.guardian.app.voiceauth

/**
 * Speaker identity verification contract.
 *
 * Compares a voice sample against enrolled speaker embeddings to determine
 * whether the speaker matches a known identity.
 *
 * Implementation is deferred to checkpoint 2.
 * This output is EVIDENCE — do not call MATCH "identity verified" or MISMATCH "scam confirmed".
 *
 * Privacy requirements:
 * - No raw trusted audio permanently stored by default
 * - Embeddings require explicit user consent before storage
 * - Embeddings must be treated as biometric data
 * - Avoid unnecessary cloud retention
 */
interface SpeakerIdentityEngine {
    /**
     * Compare audio against enrolled speaker profile.
     *
     * @param pcm16 PCM 16-bit signed samples
     * @param sampleRate Sample rate in Hz
     * @param enrolledSpeakerId Identifier for the enrolled speaker profile
     * @return Verification result with similarity and confidence
     */
    suspend fun verify(
        pcm16: ShortArray,
        sampleRate: Int = 16000,
        enrolledSpeakerId: String
    ): SpeakerVerificationResult

    /**
     * Check if a speaker profile is enrolled.
     */
    suspend fun isEnrolled(speakerId: String): Boolean
}

/**
 * Speaker verification outcome labels.
 */
enum class SpeakerVerificationLabel {
    /** Voice sample matches enrolled profile within confidence threshold */
    MATCH,
    /** Voice sample does not match enrolled profile */
    MISMATCH,
    /** No enrolled profile found for the specified speaker */
    NOT_ENROLLED,
    /** Insufficient audio or conflicting indicators */
    UNCERTAIN
}

/**
 * Result from speaker identity verification.
 */
data class SpeakerVerificationResult(
    val label: SpeakerVerificationLabel,
    /** Cosine similarity or equivalent metric [0.0, 1.0] */
    val similarity: Float,
    /** Confidence in the verification [0.0, 1.0] */
    val confidence: Float,
    val enrolledSpeakerId: String,
    val modelVersion: String,
    val timestampMs: Long = System.currentTimeMillis()
)
