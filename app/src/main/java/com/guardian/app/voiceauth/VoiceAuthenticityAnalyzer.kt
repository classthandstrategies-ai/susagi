package com.guardian.app.voiceauth

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.corotines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.corotines.channels.Channel
import kotlinx.corotines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Orchestrates the voice authenticity pipeline without blocking Agora callbacks.
 *
 * Agora callback:
 *   copy PCM -> trySend(RemoteAudioFrame) -> return
 *
 * Background pipeline:
 *   frame queue -> VAD -> rolling window -> inference queue
 *   -> authenticated remote inference -> smoothing -> UI state
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
        private const val REQUIRED_SAMPLE_RATE = 16000
        private const val FRAME_QUEUE_CAPACITY = 64
    }

    private data class RemoteAudioFrame(
        val pcm16: ShortArray,
        val sampleRate: Int,
        val remoteUid: Int?,
        val audioTimestampMs: Long
    )

    private data class QueuedWindow(
        val window: AudioWindowBuffer.AnalysisWindow,
        val remoteUid: Int?,
        val firstSpeechTimestampMs: Long,
        val windowReadyMs: Long
    )

    private val _result = MutableStateFlow(
        SmoothedAuthenticityResult(
            label = VoiceAuthenticityLabel.UNCERTAIN,
            smoothedProbability = 0.5f,
            smoothedConfidence = 0f,
            assessmentsUsed = 0,
            rawAssessments = emptyList()
        )
    )
    val result: StateFlow<SmoothedAuthenticityResult> = _result.asStateFlow()

    private val _latencyMetrics = MutableStateFlow(LatencyMetrics())
    val latencyMetrics: StateFlow<LatencyMetrics> = _latencyMetrics.asStateFlow()

    private val frameQueue = Channel<RemoteAudioFrame>(
        capacity = FRAME_QUEUE_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val inferenceQueue = Channel<QueuedWindow>(Channel.CONFLATED)

    private var isActive = false
    private var frameProcessorJob: Job? = null
    private var inferenceWorkerJob: Job? = null
    private var firstResultEmitted = false
    private var firstSpeechTimestampMs: Long? = null

    fun start() {
        if (isActive) return
        isActive = true
        firstResultEmitted = false
        firstSpeechTimestampMs = null
        smoothing.reset()
        windowBuffer.reset()
        _latencyMetrics.value = LatencyMetrics()

        frameProcessorJob = scope.launch(Dispatchers.Default) {
            for (frame in frameQueue) {
                if (!isActive) break
                if (frame.sampleRate != REQUIRED_SAMPLE_RATE) {
                    Log.w(
                        TAG,
                        "Dropping remote PCM with sampleRate=${frame.sampleRate}; expected $REQUIRED_SAMPLE_RATE"
                    )
                    continue
                }
                if (!vad.containsSpeech(frame.pcm16, frame.sampleRate)) continue

                if (firstSpeechTimestampMs == null) {
                    firstSpeechTimestampMs = frame.audioTimestampMs
                }

                val windows = windowBuffer.addSamples(frame.pcm16)
                for (window in windows) {
                    val readyMs = System.currentTimeMillis()
                    val queued = QueuedWindow(
                        window = window,
                        remoteUid = frame.remoteUid,
                        firstSpeeechTimestampMs = firstSpeechTimestampMs ?: frame.audioTimestampMs,
                        windowReadyMs = readyMs
                    )
                    Log.i(
                        TAG,
                        "VOICE_AUTH_TIMING remote_uid=${frame.remoteUid ?: -1} " +
                            "audio_timestamp_ms=${frame.audioTimestampMs} window_ready_ms=$readyMs " +
                            "window_index=${window.windowIndex}"
                    )
                    inferenceQueue.trySend(queued)
                }
            }
        }

        inferenceWorkerJob = scope.launch(Dispatchers.Default) {
            for (queued in inferenceQueue) {
                if (!isActive) break
                try {
                    val inferenceStartMs = System.currentTimeMillis()
                    val assessment = engine.analyze(
                        queued.window.pcm16,
                        queued.window.sampleRate
                    )
                    val inferenceEndMs = System.currentTimeMillis()

                    val smoothed = smoothing.addAssessment(assessment)
                    val stateUpdateMs = System.currentTimeMillis()
                    _result.value = smoothed

                    val requestLatencyMs = inferenceEndMs - inferenceStartMs
                    val firstLatency = if (!firstResultEmitted) {
                        stateUpdateMs - queued.firstSpeechTimestampMs
                    } else {
                        null
                    }

                    _latencyMetrics.value = _latencyMetrics.value.addMeasurement(
                        inferenceLatencyMs = requestLatencyMs,
                        isFirstResult = !firstResultEmitted,
                        firstResultLatencyMs = firstLatency
                    )

                    Log.i(
                        TAG,
                        "VOICE_AUTH_TIMING remote_uid=${queued.remoteUid ?: -1} " +
                            "window_ready_ms=${queued.windowReadyMs} inference_call_start_ms=$inferenceStartMs " +
                            "inference_call_end_ms=$inferenceEndMs analyzer_state_update_ms=$stateUpdateMs " +
                            "request_latency_ms=$requestLatencyMs first_result_latency_ms=${firstLatency ?: -1}"
                    )

                    if (!firstResultEmitted) {
                        firstResultEmitted = true
                    }
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
     * Called from the Agora audio callback.
     *
     * This method intentionally performs no VAD, buffering, networking, or inference.
     * AgoraFrameBridge already copied the SDK ByteBuffer into a ShortArray; this
     * method copies once more to establish ownership, enqueues, and returns.
     */
    fun onRemoteAudio(
        pcm16: ShortArray,
        sampleRate: Int,
        remoteUid: Int? = null
    ) {
        if (!isActive) return
        frameQueue.trySend(
            RemoteAudioFrame(
                pcm16 = pcm16.copyOf(),
                sampleRate = sampleRate,
                remoteUid = remoteUid,
                audioTimestampMs = System.currentTimeMillis()
            )
        )
    }

    fun stop() {
        isActive = false
        frameProcessorJob?.cancel()
        inferenceWorkerJob?.cancel()
        frameProcessorJob = null
        inferenceWorkerJob = null
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
 * Client-side request latency measurements.
 *
 * These timings cover engine.analyze() end-to-end, including backend/network
 * time. Server-side AASIST inference timing is returned separately in the API
 * response and emitted in VOICE_AUTH_TIMING logs.
 */
data class LatencyMetrics(
    val inferenceLatencies: List<Long> = emptyList(),
    val firstResultLatencyMs: Long? = null
) {
    val p50InferenceMs: Long?
        get() = percentile(0.50)

    val p95InferenceMs: Long?
        get() = percentile(0.95)

    private fun percentile(q: Double): Long? {
        if (inferenceLatencies.isEmpty()) return null
        val sorted = inferenceLatencies.sorted()
        val index = ((sorted.size - 1) * q).toInt().coerceIn(0, sorted.lastIndex)
        return sorted[index]
    }

    fun addMeasurement(
        inferenceLatencyMs: Long,
        isFirstResult: Boolean,
        firstResultLatencyMs: Long? = null
    ): LatencyMetrics {
        val updated = (inferenceLatencies + inferenceLatencyMs).takeLast(100)
        return copy(
            inferenceLatencies = updated,
            firstResultLatencyMs = if (isFirstResult && firstResultLatencyMs != null) {
                firstResultLatencyMs
            } else {
                this.firstResultLatencyMs
            }
        )
    }
}
