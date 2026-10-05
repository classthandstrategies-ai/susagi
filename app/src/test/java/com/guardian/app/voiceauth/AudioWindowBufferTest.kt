package com.guardian.app.voiceauth

import org.junit.Assert.*
import org.junit.Test

class AudioWindowBufferTest {

    @Test
    fun `first window emitted after windowDuration of samples`() {
        val buffer = AudioWindowBuffer(
            sampleRate = 16000,
            windowDurationMs = 4000,
            strideDurationMs = 1000
        )
        // 4 seconds at 16kHz = 64000 samples
        // Feed 3 seconds: no window
        val chunk3s = ShortArray(48000) { 1 }
        val result1 = buffer.addSamples(chunk3s)
        assertTrue(result1.isEmpty())

        // Feed 1 more second: first window
        val chunk1s = ShortArray(16000) { 1 }
        val result2 = buffer.addSamples(chunk1s)
        assertEquals(1, result2.size)
        assertEquals(64000, result2[0].pcm16.size)
        assertEquals(0, result2[0].windowIndex)
    }

    @Test
    fun `stride windows emitted after first window`() {
        val buffer = AudioWindowBuffer(
            sampleRate = 16000,
            windowDurationMs = 4000,
            strideDurationMs = 1000
        )
        // Feed 4 seconds (first window)
        buffer.addSamples(ShortArray(64000) { 1 })

        // Feed 1 more second (stride window)
        val result = buffer.addSamples(ShortArray(16000) { 2 })
        assertEquals(1, result.size)
        // Window should be 4 seconds of data
        assertEquals(64000, result[0].pcm16.size)
    }

    @Test
    fun `reset clears buffer`() {
        val buffer = AudioWindowBuffer(sampleRate = 16000)
        buffer.addSamples(ShortArray(32000) { 1 })
        assertEquals(32000, buffer.currentSizeInSamples)

        buffer.reset()
        assertEquals(0, buffer.currentSizeInSamples)
        assertEquals(0L, buffer.currentDurationMs)
    }

    @Test
    fun `currentDurationMs reports correctly`() {
        val buffer = AudioWindowBuffer(sampleRate = 16000)
        buffer.addSamples(ShortArray(16000) { 1 }) // 1 second
        assertEquals(1000L, buffer.currentDurationMs)
    }

    @Test
    fun `no windows from small input`() {
        val buffer = AudioWindowBuffer(sampleRate = 16000, windowDurationMs = 4000)
        val result = buffer.addSamples(ShortArray(100) { 1 })
        assertTrue(result.isEmpty())
    }
}
