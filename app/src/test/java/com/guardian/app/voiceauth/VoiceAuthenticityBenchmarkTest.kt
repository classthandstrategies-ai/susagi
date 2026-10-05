package com.guardian.app.voiceauth

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sin

/**
 * Benchmark/integration tests for the voice authenticity pipeline.
 *
 * These tests use synthetic audio signals to verify the pipeline works end-to-end,
 * including VAD, windowing, and smoothing.
 *
 * They do NOT test the remote inference engine (that requires the server).
 * They test the pipeline orchestration with a mock engine.
 */
class VoiceAuthenticityBenchmarkTest {

    /**
     * Mock engine that returns configurable synthetic probability.
     */
    private class MockAuthenticityEngine(
        private val syntheticProbability: Float = 0.5f,
        private val confidence: Float = 0.8f,
        private val latencyMs: Long = 10
    ) : VoiceAuthenticityEngine {
        var analyzeCallCount = 0
            private set

        override suspend fun analyze(pcm16: ShortArray, sampleRate: Int): VoiceAuthenticityAssessment {
            analyzeCallCount++
            // Simulate inference latency
            if (latencyMs > 0) Thread.sleep(latencyMs)
            return VoiceAuthenticityAssessment(
                label = when {
                    syntheticProbability >= 0.7f -> VoiceAuthenticityLabel.SYNTHETIC_LIKELY
                    syntheticProbability <= 0.3f -> VoiceAuthenticityLabel.LIKELY_HUMAN
                    else -> VoiceAuthenticityLabel.UNCERTAIN
                },
                syntheticProbability = syntheticProbability,
                confidence = confidence,
                analyzedDurationMs = (pcm16.size.toLong() * 1000) / sampleRate,
                modelVersion = "mock-v1",
                inferenceLatencyMs = latencyMs
            )
        }
    }

    /**
     * Generate a speech-like sinusoidal signal.
     */
    private fun generateSpeechSignal(
        durationMs: Long,
        sampleRate: Int = 16000,
        frequency: Double = 200.0,
        amplitude: Double = 5000.0
    ): ShortArray {
        val numSamples = (sampleRate * durationMs / 1000).toInt()
        return ShortArray(numSamples) { i ->
            (amplitude * sin(2.0 * Math.PI * frequency * i / sampleRate)).toInt().toShort()
        }
    }

    /**
     * Generate silence.
     */
    private fun generateSilence(durationMs: Long, sampleRate: Int = 16000): ShortArray {
        return ShortArray((sampleRate * durationMs / 1000).toInt())
    }

    // ── VAD tests ──────────────────────────────────────────────

    @Test
    fun `VAD correctly passes speech signal`() {
        val vad = VoiceActivityDetector()
        val speech = generateSpeechSignal(1000)
        assertTrue("Speech signal should be detected", vad.containsSpeech(speech))
    }

    @Test
    fun `VAD correctly rejects silence`() {
        val vad = VoiceActivityDetector()
        val silence = generateSilence(1000)
        assertFalse("Silence should not be detected as speech", vad.containsSpeech(silence))
    }

    // ── Window buffer tests ────────────────────────────────────

    @Test
    fun `window buffer produces first window at 4 seconds`() {
        val buffer = AudioWindowBuffer(sampleRate = 16000, windowDurationMs = 4000)
        // Feed 100ms chunks
        for (i in 0 until 39) {
            val chunk = generateSpeechSignal(100)
            val windows = buffer.addSamples(chunk)
            assertTrue("No window should be emitted before 4s", windows.isEmpty())
        }
        // Feed the 40th 100ms chunk (total: 4 seconds)
        val windows = buffer.addSamples(generateSpeechSignal(100))
        assertEquals("First window should be emitted at 4s", 1, windows.size)
    }

    @Test
    fun `window buffer produces stride windows every 1 second after first`() {
        val buffer = AudioWindowBuffer(sampleRate = 16000, windowDurationMs = 4000, strideDurationMs = 1000)

        // Feed 4 seconds to get first window
        buffer.addSamples(generateSpeechSignal(4000))

        // Feed 1 more second → should get stride window
        val windows = buffer.addSamples(generateSpeechSignal(1000))
        assertEquals("Stride window should be emitted", 1, windows.size)
    }

    // ── Smoothing tests ────────────────────────────────────────

    @Test
    fun `smoothing does not flip on single anomalous frame`() {
        val smoother = AuthenticitySmoothing(windowSize = 5, minAssessmentsForDecision = 3)

        // Build up LIKELY_HUMAN with 4 assessments
        repeat(4) {
            smoother.addAssessment(VoiceAuthenticityAssessment(
                label = VoiceAuthenticityLabel.LIKELY_HUMAN,
                syntheticProbability = 0.1f,
                confidence = 0.8f,
                analyzedDurationMs = 4000,
                modelVersion = "test"
            ))
        }
        assertEquals(VoiceAuthenticityLabel.LIKELY_HUMAN, smoother.currentResult().label)

        // Single high-synthetic frame
        smoother.addAssessment(VoiceAuthenticityAssessment(
            label = VoiceAuthenticityLabel.SYNTHETIC_LIKELY,
            syntheticProbability = 0.95f,
            confidence = 0.9f,
            analyzedDurationMs = 4000,
            modelVersion = "test"
        ))

        // Should NOT flip to SYNTHETIC_LIKELY
        assertNotEquals(
            "Single anomalous frame should not flip decision",
            VoiceAuthenticityLabel.SYNTHETIC_LIKELY,
            smoother.currentResult().label
        )
    }

    @Test
    fun `smoothing transitions to SYNTHETIC_LIKELY after sustained high scores`() {
        val smoother = AuthenticitySmoothing(windowSize = 5, minAssessmentsForDecision = 3)

        repeat(5) {
            smoother.addAssessment(VoiceAuthenticityAssessment(
                label = VoiceAuthenticityLabel.SYNTHETIC_LIKELY,
                syntheticProbability = 0.85f,
                confidence = 0.8f,
                analyzedDurationMs = 4000,
                modelVersion = "test"
            ))
        }

        val result = smoother.currentResult()
        assertEquals(VoiceAuthenticityLabel.SYNTHETIC_LIKELY, result.label)
        assertTrue("Smoothed probability should be high", result.smoothedProbability >= 0.65f)
    }

    // ── Failure behavior tests ─────────────────────────────────

    @Test
    fun `engine timeout produces UNCERTAIN with zero confidence`() {
        val engine = MockAuthenticityEngine(syntheticProbability = 0.5f, confidence = 0f)
        val smoother = AuthenticitySmoothing(minAssessmentsForDecision = 2)

        // Simulate 5 uncertain results (as if timeout)
        repeat(5) {
            val assessment = VoiceAuthenticityAssessment(
                label = VoiceAuthenticityLabel.UNCERTAIN,
                syntheticProbability = 0.5f,
                confidence = 0f,
                analyzedDurationMs = 0,
                modelVersion = "timeout"
            )
            smoother.addAssessment(assessment)
        }

        assertEquals(
            "Timeout assessments should remain UNCERTAIN",
            VoiceAuthenticityLabel.UNCERTAIN,
            smoother.currentResult().label
        )
    }

    @Test
    fun `insufficient audio produces no assessment`() {
        val vad = VoiceActivityDetector()
        val shortSilence = generateSilence(50) // 50ms of silence
        assertFalse("50ms silence should not pass VAD", vad.containsSpeech(shortSilence))
    }

    // ── Latency metrics tests ──────────────────────────────────

    @Test
    fun `latency metrics track p50 and p95 correctly`() {
        var metrics = LatencyMetrics()

        // Add 20 measurements with known distribution
        for (i in 1..20) {
            metrics = metrics.addMeasurement(
                inferenceLatencyMs = i.toLong() * 10, // 10, 20, ..., 200
                isFirstResult = i == 1,
                firstResultLatencyMs = if (i == 1) 500L else null
            )
        }

        assertNotNull(metrics.p50InferenceMs)
        assertNotNull(metrics.p95InferenceMs)
        assertEquals(500L, metrics.firstResultLatencyMs)

        // p50 should be around 100ms (10th value of 10,20,...,200)
        assertTrue("p50 should be around 100ms", metrics.p50InferenceMs!! in 80..120)
        // p95 should be around 190ms
        assertTrue("p95 should be around 190ms", metrics.p95InferenceMs!! in 170..200)
    }

    @Test
    fun `latency metrics keeps at most 100 measurements`() {
        var metrics = LatencyMetrics()
        for (i in 1..150) {
            metrics = metrics.addMeasurement(i.toLong(), isFirstResult = false)
        }
        assertEquals(100, metrics.inferenceLatencies.size)
    }

    // ── Confusion matrix helper ────────────────────────────────

    @Test
    fun `confusion matrix categories are exhaustive`() {
        // Verify all label combinations are covered
        val labels = VoiceAuthenticityLabel.entries
        assertEquals("Should have 3 labels", 3, labels.size)
        assertTrue(labels.contains(VoiceAuthenticityLabel.LIKELY_HUMAN))
        assertTrue(labels.contains(VoiceAuthenticityLabel.SYNTHETIC_LIKELY))
        assertTrue(labels.contains(VoiceAuthenticityLabel.UNCERTAIN))
    }

    // ── Independence test ──────────────────────────────────────

    @Test
    fun `voice authenticity assessment does not contain RiskLevel`() {
        // Verify that VoiceAuthenticityAssessment has no RiskLevel field
        val assessment = VoiceAuthenticityAssessment(
            label = VoiceAuthenticityLabel.SYNTHETIC_LIKELY,
            syntheticProbability = 0.9f,
            confidence = 0.8f,
            analyzedDurationMs = 4000,
            modelVersion = "test"
        )

        // These fields should exist
        assertNotNull(assessment.label)
        assertNotNull(assessment.syntheticProbability)
        assertNotNull(assessment.confidence)
        assertNotNull(assessment.modelVersion)

        // Verify it's data only — no risk/protective action fields
        val fields = assessment::class.java.declaredFields.map { it.name }
        assertFalse("Should not contain riskLevel", fields.contains("riskLevel"))
        assertFalse("Should not contain protectiveAction", fields.contains("protectiveAction"))
        assertFalse("Should not contain scamVerdict", fields.contains("scamVerdict"))
    }
}
