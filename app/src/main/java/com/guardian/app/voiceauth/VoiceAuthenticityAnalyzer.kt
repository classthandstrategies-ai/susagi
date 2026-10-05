package com.guardian.app.voiceauth

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Orchestrates the voice authenticity analysis pipeline:
 *
 *   REMOTE PCM frames
 *   → Voice Activity Detection
 *   → rolling audio buffer
 *   → voice-authenticity inference
 *   → smoothing
 *   → SmoothedAuthenticityResult
 *
 * Design rules:
 * - Does NOT perform inference inside any audio callback
 * - Uses a background coroutine worker for inference
 * - Produces evidence only — does not calculate RiskLevel
 */
class VoiceAuthenticityAnalyzer(
    private val engine: VoiceAuthenticityEngine,
    private val scope: CoroutineScope,
    private val vad: VoiceActivityDetector = VoiceActivityDetector(),
    private val windowBuffer: AudioWindowBuffer = AudioWindowBuffer(),
    private val smoothing: AuthenticitySmoothing = AuthenticitySmoothing()
) {
    companion object {
        private const val TAG = "VoiceAuthAnalyzer"
    }

    /** Current smoothed authenticity result */
    private val _result = MutableStateFlow(SmoothedAuthenticityResult(
        label = VoiceAuthenticityLabel.UNCERTAIN,
        smoothedProbability = 0.5f,
        smoothedConfidence = 0f,
        assessmentsUsed = 0,
        rawAssessments = emptyList()
    ))
    val result: StateFlow<SmoothedAuthenticityResult> = _result.asStateFlow()

    /** Latency metrics */
    private val _latencyMetrics = MutableStateFlow(LatencyMetrics())
    val latencyMetrics: StateFlow<LatencyMetrics> = _latencyMetrics.asStateFlow()

    /** Whether the analyzer is currently active */
    private var isActive = false

    /** Channel for sending windows to the inference worker */
    private val inferenceQueue = Channel<AudioWindowBuffer.AnalysisWindow>(Channel.CONFLATED)

    private var workerJob: Job? = null
    private var firstResultEmitted = false
    private var analyzerStartTimeMs = 0L

    /**
     * Start the analysis pipeline.
     */
    fun start() {
        if (isActive) return
        isActive = true
        analyzerStartTimeMs = System.currentTimeMillis()
        firstResultEmitted = false
        smoothing.reset()
        windowBuffer.reset()

        workerJob = scope.launch(Dispatchers.Default) {
            for (window in inferenceQueue) {
                if (!isActive) break
                try {
                    val windowReadyMs = System.currentTimeMillis()
                    val inferenceStartMs = System.currentTimeMillis()
                    val assessment = engine.analyze(window.pcm16, window.sampleRate)
                    val inferenceEndMs = System.currentTimeMillis()

                    val smoothed = smoothing.addAssessment(assessment)
                    _result.value = smoothed

                    // Update latency metrics
                    val metrics = _latencyMetrics.value
                    val inferenceLatency = inferenceEndMs - inferenceStartMs
                    val updatedMetrics = metrics.addMeasurement(
                        inferenceLatencyMs = inferenceLatency,
                        isFirstResult = !firstResultEmitted,
                        firstResultLatencyMs = if (!firstResultEmitted) {
                            System.currentTimeMillis() - analyzerStartTimeMs
                        } else null
                    )
                    _latencyMetrics.value = updatedMetrics

                    if (!firstResultEmitted) {
                        firstResultEmitted = true
                        Log.i(TAG, "First authenticity result: ${smoothed.label}, " +
                            "latency=${inferenceLatency}ms, " +
                            "firstResult=${System.currentTimeMillis() - analyzerStartTimeMs}ms")
                    }

                    Log.d(TAG, "Assessment: label=${assessment.label}, " +
                        "synProb=${assessment.syntheticProbability}, " +
                        "smoothed=${smoothed.label} (${smoothed.smoothedProbability})")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Inference error: ${e.message}")
                }
            }
        }

        Log.i(TAG, "VoiceAuthenticityAnalyzer started")
    }

    /**
     * Feed remote audio PCM data. Call this from outside the Agora callback thread.
     * This does NOT block.
     */
    fun onRemoteAudio(pcm16: ShortArray, sampleRate: Int) {
        if (!isActive) return

        // VAD check
        if (!vad.containsSpeech(pcm16, sampleRate)) return

        // Add to window buffer
        val windows = windowBuffer.addSamples(pcm16)

        // Enqueue windows for inference (CONFLATED = only latest survives)
        for (window in windows) {
            inferenceQueue.trySend(window)
        }
    }

    /**
     * Stop the analyzer and reset state.
     */
    fun stop() {
        isActive = false
        workerJob?.cancel()
        workerJob = null
        inferenceQueue.cancel()
        smoothing.reset()
        windowBuffer.reset()
        _result.value = SmoothedAuthenticityResult(
            label = VoiceAuthenticityLabel.UNCERTAIN,
            smoothedProbability = 0.5f,
            smoothedConfidence = 0f,
            assessmentsUsed = 0,
            rawAssessments = emptyList()
        )
        Log.i(TAG, "VoiceAuthenticityAnalyzer stopped")
    }
}

/**
 * Latency measurements for the authenticity pipeline.
 */
data class LatencyMetrics(
    val inferenceLatencies: List<Long> = emptyList(),
    val firstResultLatencyMs: Long? = null
) {
    /** p50 inference latency in ms */
    val p50InferenceMs: Long?
        get() = if (inferenceLatencies.isNotEmpty()) {
            inferenceLatencies.sorted()[inferenceLatencies.size / 2]
        } else null

    /** p95 inference latency in ms */
    val p95InferenceMs: Long?
        get() = if (inferenceLatencies.isNotEmpty()) {
            val sorted = inferenceLatencies.sorted()
            sorted[(sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)]
        } else null

    fun addMeasurement(
        inferenceLatencyMs: Long,
        isFirstResult: Boolean,
        firstResultLatencyMs: Long? = null
    ): LatencyMetrics {
        val updated = inferenceLatencies + inferenceLatencyMs
        // Keep last 100 measurements
        val trimmed = if (updated.size > 100) updated.takeLast(100) else updated
        return copy(
            inferenceLatencies = trimmed,
            firstResultLatencyMs = if (isFirstResult && firstResultLatencyMs != null) {
                firstResultLatencyMs
            } else this.firstResultLatencyMs
        )
    }
}
