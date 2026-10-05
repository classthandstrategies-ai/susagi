package com.guardian.app.voiceauth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Voice authenticity engine that delegates inference to a private server endpoint.
 *
 * Architecture:
 *   Android → sends short REMOTE audio windows (base64-encoded PCM16)
 *   → private inference endpoint (AASIST model)
 *   → receives authenticity assessment
 *
 * Security:
 * - Endpoint must be authenticated (API key in local.properties, not source)
 * - Request size limits enforced
 * - Non-PCM payloads rejected server-side
 * - No raw audio logged
 */
class RemoteInferenceAuthenticityEngine(
    private val endpointUrl: String,
    private val apiKey: String = "",
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()
) : VoiceAuthenticityEngine {

    companion object {
        private const val TAG = "RemoteAuthEngine"
        private const val MAX_SAMPLES = 16000 * 6  // 6 seconds max at 16kHz
        private const val MODEL_VERSION = "aasist-remote-v1"
    }

    override suspend fun analyze(
        pcm16: ShortArray,
        sampleRate: Int
    ): VoiceAuthenticityAssessment = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()

        // Enforce size limit
        val samples = if (pcm16.size > MAX_SAMPLES) pcm16.copyOf(MAX_SAMPLES) else pcm16
        val durationMs = (samples.size.toLong() * 1000L) / sampleRate

        try {
            val pcmBytes = shortsToBytes(samples)
            val encoded = Base64.getEncoder().encodeToString(pcmBytes)

            val payload = JSONObject().apply {
                put("audio_b64", encoded)
                put("sample_rate", sampleRate)
                put("channels", 1)
                put("format", "pcm16")
            }

            val requestBuilder = Request.Builder()
                .url(endpointUrl)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val inferenceMs = System.currentTimeMillis() - startMs

            if (!response.isSuccessful) {
                Log.w(TAG, "Inference endpoint returned ${response.code}")
                return@withContext uncertainAssessment(durationMs, inferenceMs, "HTTP ${response.code}")
            }

            val body = response.body?.string() ?: return@withContext uncertainAssessment(durationMs, inferenceMs, "Empty response")
            parseResponse(body, durationMs, inferenceMs)
        } catch (e: Exception) {
            val inferenceMs = System.currentTimeMillis() - startMs
            Log.w(TAG, "Inference failed: ${e.message}")
            uncertainAssessment(durationMs, inferenceMs, e.message ?: "Unknown error")
        }
    }

    private fun parseResponse(
        json: String,
        durationMs: Long,
        inferenceMs: Long
    ): VoiceAuthenticityAssessment {
        return try {
            val obj = JSONObject(json)
            val syntheticProb = obj.optDouble("synthetic_probability", 0.5).toFloat().coerceIn(0f, 1f)
            val confidence = obj.optDouble("confidence", 0.0).toFloat().coerceIn(0f, 1f)
            val modelVer = obj.optString("model_version", MODEL_VERSION)

            val label = when {
                confidence < 0.3f -> VoiceAuthenticityLabel.UNCERTAIN
                syntheticProb >= 0.7f -> VoiceAuthenticityLabel.SYNTHETIC_LIKELY
                syntheticProb <= 0.3f -> VoiceAuthenticityLabel.LIKELY_HUMAN
                else -> VoiceAuthenticityLabel.UNCERTAIN
            }

            VoiceAuthenticityAssessment(
                label = label,
                syntheticProbability = syntheticProb,
                confidence = confidence,
                analyzedDurationMs = durationMs,
                modelVersion = modelVer,
                inferenceLatencyMs = inferenceMs
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse response: ${e.message}")
            uncertainAssessment(durationMs, inferenceMs, "Parse error")
        }
    }

    private fun uncertainAssessment(
        durationMs: Long,
        inferenceMs: Long,
        reason: String
    ) = VoiceAuthenticityAssessment(
        label = VoiceAuthenticityLabel.UNCERTAIN,
        syntheticProbability = 0.5f,
        confidence = 0f,
        analyzedDurationMs = durationMs,
        modelVersion = MODEL_VERSION,
        inferenceLatencyMs = inferenceMs
    )

    private fun shortsToBytes(shorts: ShortArray): ByteArray {
        val baos = ByteArrayOutputStream(shorts.size * 2)
        val dos = DataOutputStream(baos)
        for (s in shorts) {
            // Little-endian PCM16
            dos.writeByte(s.toInt() and 0xFF)
            dos.writeByte((s.toInt() shr 8) and 0xFF)
        }
        dos.flush()
        return baos.toByteArray()
    }
}
