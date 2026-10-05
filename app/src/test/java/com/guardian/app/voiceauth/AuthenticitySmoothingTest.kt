package com.guardian.app.voiceauth

import org.junit.Assert.*
import org.junit.Test

class AuthenticitySmoothingTest {

    private fun makeAssessment(
        synProb: Float,
        confidence: Float = 0.8f
    ) = VoiceAuthenticityAssessment(
        label = when {
            synProb >= 0.7f -> VoiceAuthenticityLabel.SYNTHETIC_LIKELY
            synProb <= 0.3f -> VoiceAuthenticityLabel.LIKELY_HUMAN
            else -> VoiceAuthenticityLabel.UNCERTAIN
        },
        syntheticProbability = synProb,
        confidence = confidence,
        analyzedDurationMs = 4000,
        modelVersion = "test-v1"
    )

    @Test
    fun `empty smoother returns UNCERTAIN`() {
        val smoother = AuthenticitySmoothing()
        val result = smoother.currentResult()
        assertEquals(VoiceAuthenticityLabel.UNCERTAIN, result.label)
        assertEquals(0, result.assessmentsUsed)
    }

    @Test
    fun `sustained high synthetic scores produce SYNTHETIC_LIKELY`() {
        val smoother = AuthenticitySmoothing(windowSize = 5, minAssessmentsForDecision = 3)
        repeat(4) {
            smoother.addAssessment(makeAssessment(0.85f))
        }
        val result = smoother.currentResult()
        assertEquals(VoiceAuthenticityLabel.SYNTHETIC_LIKELY, result.label)
        assertTrue(result.smoothedProbability >= 0.65f)
    }

    @Test
    fun `sustained low synthetic scores produce LIKELY_HUMAN`() {
        val smoother = AuthenticitySmoothing(windowSize = 5, minAssessmentsForDecision = 3)
        repeat(4) {
            smoother.addAssessment(makeAssessment(0.1f))
        }
        val result = smoother.currentResult()
        assertEquals(VoiceAuthenticityLabel.LIKELY_HUMAN, result.label)
        assertTrue(result.smoothedProbability <= 0.35f)
    }

    @Test
    fun `fewer than minAssessments returns UNCERTAIN`() {
        val smoother = AuthenticitySmoothing(minAssessmentsForDecision = 3)
        smoother.addAssessment(makeAssessment(0.9f))
        smoother.addAssessment(makeAssessment(0.9f))
        val result = smoother.currentResult()
        assertEquals(VoiceAuthenticityLabel.UNCERTAIN, result.label)
    }

    @Test
    fun `conflicting scores produce UNCERTAIN`() {
        val smoother = AuthenticitySmoothing(windowSize = 5, minAssessmentsForDecision = 3)
        smoother.addAssessment(makeAssessment(0.9f))
        smoother.addAssessment(makeAssessment(0.1f))
        smoother.addAssessment(makeAssessment(0.9f))
        smoother.addAssessment(makeAssessment(0.1f))
        smoother.addAssessment(makeAssessment(0.5f))
        val result = smoother.currentResult()
        assertEquals(VoiceAuthenticityLabel.UNCERTAIN, result.label)
    }

    @Test
    fun `reset clears all assessments`() {
        val smoother = AuthenticitySmoothing()
        repeat(5) { smoother.addAssessment(makeAssessment(0.9f)) }
        smoother.reset()
        assertEquals(0, smoother.assessmentCount)
        assertEquals(VoiceAuthenticityLabel.UNCERTAIN, smoother.currentResult().label)
    }

    @Test
    fun `window size limits retained assessments`() {
        val smoother = AuthenticitySmoothing(windowSize = 3)
        repeat(10) { smoother.addAssessment(makeAssessment(0.5f)) }
        assertEquals(3, smoother.assessmentCount)
    }

    @Test
    fun `zero confidence assessments treated as invalid`() {
        val smoother = AuthenticitySmoothing(minAssessmentsForDecision = 2)
        repeat(5) {
            smoother.addAssessment(makeAssessment(0.9f, confidence = 0f))
        }
        val result = smoother.currentResult()
        // All zero-confidence → no valid assessments → UNCERTAIN
        assertEquals(VoiceAuthenticityLabel.UNCERTAIN, result.label)
    }

    @Test
    fun `does not flip from single isolated frame`() {
        val smoother = AuthenticitySmoothing(windowSize = 5, minAssessmentsForDecision = 3)
        // Build up LIKELY_HUMAN
        repeat(4) { smoother.addAssessment(makeAssessment(0.1f)) }
        assertEquals(VoiceAuthenticityLabel.LIKELY_HUMAN, smoother.currentResult().label)

        // One high synthetic frame should not flip
        smoother.addAssessment(makeAssessment(0.95f))
        val result = smoother.currentResult()
        // The weighted average should still be low enough to not flip to SYNTHETIC
        assertNotEquals(VoiceAuthenticityLabel.SYNTHETIC_LIKELY, result.label)
    }
}
