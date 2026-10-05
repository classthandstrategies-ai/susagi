package com.guardian.app.voiceauth

/**
 * Rolling decision policy for smoothing voice authenticity assessments.
 *
 * Prevents flipping states from one isolated frame.
 * Maintains a window of recent valid assessments and produces a smoothed decision.
 *
 * Thresholds are configurable and NOT presented as calibrated real-world certainty.
 */
class AuthenticitySmoothing(
    /** Number of recent assessments to consider */
    private val windowSize: Int = 5,
    /** Threshold above which sustained probability → SYNTHETIC_LIKELY */
    private val syntheticThreshold: Float = 0.65f,
    /** Threshold below which sustained probability → LIKELY_HUMAN */
    private val humanThreshold: Float = 0.35f,
    /** Minimum assessments needed before making a non-UNCERTAIN decision */
    private val minAssessmentsForDecision: Int = 3
) {
    private val recentAssessments = ArrayDeque<VoiceAuthenticityAssessment>()

    /**
     * Add a new assessment and return the smoothed decision.
     */
    fun addAssessment(assessment: VoiceAuthenticityAssessment): SmoothedAuthenticityResult {
        recentAssessments.addLast(assessment)
        while (recentAssessments.size > windowSize) {
            recentAssessments.removeFirst()
        }
        return computeSmoothed()
    }

    /**
     * Get the current smoothed result without adding a new assessment.
     */
    fun currentResult(): SmoothedAuthenticityResult = computeSmoothed()

    /**
     * Reset all accumulated assessments.
     */
    fun reset() {
        recentAssessments.clear()
    }

    /**
     * Number of assessments currently in the window.
     */
    val assessmentCount: Int get() = recentAssessments.size

    private fun computeSmoothed(): SmoothedAuthenticityResult {
        if (recentAssessments.isEmpty()) {
            return SmoothedAuthenticityResult(
                label = VoiceAuthenticityLabel.UNCERTAIN,
                smoothedProbability = 0.5f,
                smoothedConfidence = 0f,
                assessmentsUsed = 0,
                rawAssessments = emptyList()
            )
        }

        // Weighted mean: weight by confidence
        val validAssessments = recentAssessments.filter { it.confidence > 0f }
        if (validAssessments.isEmpty()) {
            return SmoothedAuthenticityResult(
                label = VoiceAuthenticityLabel.UNCERTAIN,
                smoothedProbability = 0.5f,
                smoothedConfidence = 0f,
                assessmentsUsed = recentAssessments.size,
                rawAssessments = recentAssessments.toList()
            )
        }

        val totalWeight = validAssessments.sumOf { it.confidence.toDouble() }
        val weightedProb = if (totalWeight > 0) {
            validAssessments.sumOf {
                (it.syntheticProbability * it.confidence).toDouble()
            } / totalWeight
        } else {
            0.5
        }

        val smoothedProb = weightedProb.toFloat().coerceIn(0f, 1f)
        val avgConfidence = validAssessments.map { it.confidence }.average().toFloat()

        val label = when {
            validAssessments.size < minAssessmentsForDecision -> VoiceAuthenticityLabel.UNCERTAIN
            smoothedProb >= syntheticThreshold -> VoiceAuthenticityLabel.SYNTHETIC_LIKELY
            smoothedProb <= humanThreshold -> VoiceAuthenticityLabel.LIKELY_HUMAN
            else -> VoiceAuthenticityLabel.UNCERTAIN
        }

        return SmoothedAuthenticityResult(
            label = label,
            smoothedProbability = smoothedProb,
            smoothedConfidence = avgConfidence,
            assessmentsUsed = validAssessments.size,
            rawAssessments = recentAssessments.toList()
        )
    }
}

/**
 * Smoothed authenticity result from the rolling decision policy.
 */
data class SmoothedAuthenticityResult(
    val label: VoiceAuthenticityLabel,
    /** Weighted mean synthetic probability across recent windows */
    val smoothedProbability: Float,
    /** Average confidence across recent windows */
    val smoothedConfidence: Float,
    /** Number of assessments used in the smoothing */
    val assessmentsUsed: Int,
    /** Raw assessments in the window for debugging */
    val rawAssessments: List<VoiceAuthenticityAssessment>
)
