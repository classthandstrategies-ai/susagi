package com.guardian.app.voiceauth

import org.junit.Assert.*
import org.junit.Test

class VoiceAuthenticityAssessmentTest {

    @Test
    fun `valid assessment created successfully`() {
        val assessment = VoiceAuthenticityAssessment(
            label = VoiceAuthenticityLabel.LIKELY_HUMAN,
            syntheticProbability = 0.1f,
            confidence = 0.95f,
            analyzedDurationMs = 4000,
            modelVersion = "test-v1"
        )
        assertEquals(VoiceAuthenticityLabel.LIKELY_HUMAN, assessment.label)
        assertEquals(0.1f, assessment.syntheticProbability, 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `syntheticProbability above 1 throws`() {
        VoiceAuthenticityAssessment(
            label = VoiceAuthenticityLabel.UNCERTAIN,
            syntheticProbability = 1.5f,
            confidence = 0.5f,
            analyzedDurationMs = 1000,
            modelVersion = "test"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative confidence throws`() {
        VoiceAuthenticityAssessment(
            label = VoiceAuthenticityLabel.UNCERTAIN,
            syntheticProbability = 0.5f,
            confidence = -0.1f,
            analyzedDurationMs = 1000,
            modelVersion = "test"
        )
    }

    @Test
    fun `speaker verification labels are distinct`() {
        assertNotEquals(SpeakerVerificationLabel.MATCH, SpeakerVerificationLabel.MISMATCH)
        assertNotEquals(SpeakerVerificationLabel.NOT_ENROLLED, SpeakerVerificationLabel.UNCERTAIN)
    }

    @Test
    fun `latency metrics p50 and p95`() {
        var metrics = LatencyMetrics()
        val latencies = listOf(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L)
        for ((i, l) in latencies.withIndex()) {
            metrics = metrics.addMeasurement(l, isFirstResult = i == 0, firstResultLatencyMs = if (i == 0) 500L else null)
        }
        assertNotNull(metrics.p50InferenceMs)
        assertNotNull(metrics.p95InferenceMs)
        assertEquals(500L, metrics.firstResultLatencyMs)
        // p50 should be around 50-60 range
        assertTrue(metrics.p50InferenceMs!! in 40..70)
        // p95 should be around 90-100
        assertTrue(metrics.p95InferenceMs!! in 80..100)
    }
}
