package com.guardian.app.voiceauth

/**
 * Rolling audio buffer that accumulates PCM samples and produces
 * overlapping analysis windows.
 *
 * Target:
 * - ~3-4 sec first meaningful window
 * - ~1 sec stride for subsequent windows
 *
 * Example window sequence:
 *   0-4 sec, 1-5 sec, 2-6 sec, 3-7 sec
 *
 * Thread safety: callers must synchronize externally or use from a single coroutine.
 */
class AudioWindowBuffer(
    /** Sample rate in Hz */
    private val sampleRate: Int = 16000,
    /** Window duration in milliseconds */
    private val windowDurationMs: Long = 4000,
    /** Stride duration in milliseconds for subsequent windows */
    private val strideDurationMs: Long = 1000
) {
    private val windowSamples = (sampleRate * windowDurationMs / 1000).toInt()
    private val strideSamples = (sampleRate * strideDurationMs / 1000).toInt()

    private val buffer = ArrayList<Short>(windowSamples * 2)
    private var samplesConsumed = 0
    private var firstWindowEmitted = false

    /**
     * Metadata about the source of audio in this buffer.
     */
    data class AudioChunkMeta(
        val speakerSource: String,
        val timestampNs: Long,
        val sampleRate: Int,
        val channels: Int
    )

    /**
     * A ready-to-analyze audio window.
     */
    data class AnalysisWindow(
        val pcm16: ShortArray,
        val sampleRate: Int,
        val windowIndex: Int,
        val startSampleOffset: Long,
        val durationMs: Long
    ) {
        override fun equals(other: Any?): Boolean = other is AnalysisWindow &&
            windowIndex == other.windowIndex && pcm16.contentEquals(other.pcm16)
        override fun hashCode(): Int = windowIndex * 31 + pcm16.contentHashCode()
    }

    /**
     * Add new PCM samples to the buffer.
     *
     * @return A list of ready analysis windows (0 or 1 typically)
     */
    fun addSamples(samples: ShortArray): List<AnalysisWindow> {
        buffer.addAll(samples.toList())

        val windows = mutableListOf<AnalysisWindow>()

        if (!firstWindowEmitted) {
            // Wait for first full window
            if (buffer.size >= windowSamples) {
                val windowData = ShortArray(windowSamples)
                for (i in 0 until windowSamples) windowData[i] = buffer[i]
                windows.add(AnalysisWindow(
                    pcm16 = windowData,
                    sampleRate = sampleRate,
                    windowIndex = 0,
                    startSampleOffset = 0,
                    durationMs = windowDurationMs
                ))
                firstWindowEmitted = true
                samplesConsumed = windowSamples
            }
        } else {
            // Emit stride-based windows
            while (buffer.size - samplesConsumed >= strideSamples &&
                   buffer.size >= windowSamples) {
                samplesConsumed += strideSamples
                val startIdx = (samplesConsumed - windowSamples).coerceAtLeast(0)
                val endIdx = (startIdx + windowSamples).coerceAtMost(buffer.size)
                val actualSize = endIdx - startIdx
                if (actualSize < windowSamples / 2) break  // Not enough data

                val windowData = ShortArray(actualSize)
                for (i in 0 until actualSize) windowData[i] = buffer[startIdx + i]

                windows.add(AnalysisWindow(
                    pcm16 = windowData,
                    sampleRate = sampleRate,
                    windowIndex = samplesConsumed / strideSamples,
                    startSampleOffset = startIdx.toLong(),
                    durationMs = (actualSize.toLong() * 1000) / sampleRate
                ))
            }

            // Trim old data to prevent unbounded growth
            val keepFrom = (samplesConsumed - windowSamples).coerceAtLeast(0)
            if (keepFrom > 0) {
                buffer.subList(0, keepFrom).clear()
                samplesConsumed -= keepFrom
            }
        }

        return windows
    }

    /**
     * Reset the buffer state.
     */
    fun reset() {
        buffer.clear()
        samplesConsumed = 0
        firstWindowEmitted = false
    }

    /**
     * Current buffer size in samples.
     */
    val currentSizeInSamples: Int get() = buffer.size

    /**
     * Current buffer duration in milliseconds.
     */
    val currentDurationMs: Long get() = (buffer.size.toLong() * 1000) / sampleRate
}
