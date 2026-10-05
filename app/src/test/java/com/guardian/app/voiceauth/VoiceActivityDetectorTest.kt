package com.guardian.app.voiceauth

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sin

class VoiceActivityDetectorTest {

    private val vad = VoiceActivityDetector()

    @Test
    fun `silence is not speech`() {
        val silence = ShortArray(16000) { 0 }
        assertFalse(vad.containsSpeech(silence))
    }

    @Test
    fun `empty array is not speech`() {
        assertFalse(vad.containsSpeech(ShortArray(0)))
    }

    @Test
    fun `low amplitude noise is not speech`() {
        val noise = ShortArray(16000) { (Math.random() * 20 - 10).toInt().toShort() }
        assertFalse(vad.containsSpeech(noise))
    }

    @Test
    fun `moderate amplitude sinusoid detected as speech`() {
        // 200 Hz sine wave at decent amplitude
        val sampleRate = 16000
        val freq = 200.0
        val amplitude = 5000.0
        val signal = ShortArray(sampleRate) { i ->
            (amplitude * sin(2.0 * Math.PI * freq * i / sampleRate)).toInt().toShort()
        }
        assertTrue(vad.containsSpeech(signal, sampleRate))
    }

    @Test
    fun `computeRms returns zero for silence`() {
        val rms = vad.computeRms(ShortArray(100) { 0 })
        assertEquals(0f, rms, 0.001f)
    }

    @Test
    fun `computeRms returns correct value for known signal`() {
        // DC offset signal: all 100
        val dc = ShortArray(100) { 100 }
        val rms = vad.computeRms(dc)
        assertEquals(100f, rms, 1f)
    }

    @Test
    fun `high frequency pure noise rejected`() {
        // Very high ZCR random noise
        val noise = ShortArray(16000) { i ->
            if (i % 2 == 0) 1000 else (-1000).toShort()
        }
        assertFalse(vad.containsSpeech(noise))
    }
}
