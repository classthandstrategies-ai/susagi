package com.guardian.app.voiceauth

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Simple energy-based Voice Activity Detector for filtering silence
 * before sending audio to the authenticity engine.
 *
 * Not a full-featured VAD (like WebRTC VAD) — just sufficient to avoid
 * wasting inference on silence/noise frames.
 */
class VoiceActivityDetector(
    /** RMS energy threshold below which audio is considered silence */
    private val silenceThresholdRms: Float = 150f,
    /** Minimum fraction of samples above zero-crossing threshold */
    private val minSpeechFraction: Float = 0.15f
) {

    /**
     * Returns true if the audio window likely contains speech.
     *
     * @param pcm16 PCM 16-bit signed samples
     * @param sampleRate Sample rate (used for future frame-level analysis)
     */
    fun containsSpeech(pcm16: ShortArray, sampleRate: Int = 16000): Boolean {
        if (pcm16.isEmpty()) return false

        val rms = computeRms(pcm16)
        if (rms < silenceThresholdRms) return false

        val zcr = computeZeroCrossingRate(pcm16)
        // Speech typically has moderate ZCR (0.02-0.3)
        // Pure noise tends to have very high ZCR
        if (zcr > 0.5f) return false

        // Check that enough samples have meaningful amplitude
        val threshold = silenceThresholdRms * 0.5f
        val activeSamples = pcm16.count { abs(it.toFloat()) > threshold }
        val activeFraction = activeSamples.toFloat() / pcm16.size

        return activeFraction >= minSpeechFraction
    }

    /**
     * Compute RMS energy of the audio buffer.
     */
    fun computeRms(pcm16: ShortArray): Float {
        if (pcm16.isEmpty()) return 0f
        var sumSq = 0.0
        for (s in pcm16) {
            sumSq += s.toDouble() * s.toDouble()
        }
        return sqrt(sumSq / pcm16.size).toFloat()
    }

    /**
     * Compute zero-crossing rate.
     */
    private fun computeZeroCrossingRate(pcm16: ShortArray): Float {
        if (pcm16.size < 2) return 0f
        var crossings = 0
        for (i in 1 until pcm16.size) {
            if ((pcm16[i] > 0 && pcm16[i - 1] <= 0) ||
                (pcm16[i] <= 0 && pcm16[i - 1] > 0)) {
                crossings++
            }
        }
        return crossings.toFloat() / (pcm16.size - 1)
    }
}
