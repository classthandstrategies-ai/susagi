package com.guardian.app.voiceauth

import android.util.Log
import com.guardian.app.auth.SupabaseAuthManager
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
 * Voice authenticity engine that delegates inference through the authenticated
 * SuSagi backend.
 *
 * Architecture:
 *   Android -> Supabase-authenticated SuSagi backend
 *   -> private AASIST service
 *   -> authenticity assessment
 *
 * Security:
 * - Android sends only its short-lived Supabase access JWT.
 * - The long-lived model-service credential stays server-side.
 * - No raw audio is logged or persisted by this client.
 */
class RemoteInferenceAuthenticityEngine(
    private val endpointUrl: String,
    private val authManager: SupabaseAuthManager = SupabaseAuthManager(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()
) : VoiceAuthenticityEngine {

    companion object {
        private const val TAG = "RemoteAuthEngine"
        private const val REQUIRED_SAMPLE_RATE = 16000
        private const val MAX_SAMPLES = REQUIRED_SAMPLE_RATE * 6
        private const val MODEL_VERSION = "aasist-remote-v1"
    }

    override suspend fun analyze(
        pcm16: ShortArray,
        sampleRate: Int
    ): VoiceAuthenticityAssessment = withContext(Dispatchers.IO) {
        val callStartMs = System.currentTimeMillis()
        if (sampleRate != REQUIRED_SAMPLE_RATE) {
            return@withContext uncertainAssessment(
                durationMs = 0L,
                inferenceMs = 0L,
                reason = "Unsupported sample rate $sampleRate"
            )
        }

        val samples = if (pcm16.size > MAX_SAMPLES) pcm16.copyOf(MAX_SAMPLES) else pcm16
        val durationMs = (samples.size.toLong() * 1000L) / sampleRate

        val tokenResult = authManager.getAccessToken()
        if (tokenResult.isFailure) {
            Log.w(TAG, "Authentication unavailable for voice inference")
            return@withContext uncertainAssessment(
                durationMs,
                System.currentTimeMillis() - callStartMs,
                "Authentication unavailable"
            )
        }
        val accessToken = tokenResult.getOrThrow()

        try {
            val pcmBytes = shortsToBytes(samples)
            val encoded = Base64.getEncoder().encodeToString(pcmBytes)

            val payload = JSONObject().apply {
                put("audio_b64", encoded)
                put("sample_rate", sampleRate)
                put("channels", 1)
                put("format", "pcm16")
            }

            val requestStartMs = System.currentTimeMillis()
            Log.i(TAG, "VOICE_AUTH_TIMING network_request_start_ms=$requestStartMs")

            val request = Request.Builder()
                .url(endpointUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseReceivedMs = System.currentTimeMillis()
                val roundTripMs = responseReceivedMs - requestStartMs
                Log.i(
                    TAG,
                    "VOICE_AUTH_TIMING response_received_ms=$responseReceivedMs " +
                        "network_round_trip_ms=$roundTripMs http_status=${response.code}"
                )

                if (!response.isSuccessful) {
                    return@withContext uncertainAssessment(
                        durationMs,
                        roundTripMs,
                        "HTTP ${response.code}"
                    )
                }

                val body = response.body?.string()
                    ?: return@withContext uncertainAssessment(durationMs, roundTripMs, "Empty response")
                parseResponse(body, durationMs, roundTripMs)
            }
        } catch (e: Exception) {
            val elapsedMs = System.currentTimeMillis() - callStartMs
            Log.w(TAG, "Inference failed: ${e.message}")
            uncertainAssessment(durationMs, elapsedMs, e.message ?: "Unknown error")
        }
    }

    private fun parseResponse(
        json: String,
        durationMs: Long,
        roundTripMs: Long
    ): VoiceAuthenticityAssessment {
        return try {
            val obj = JSONObject(json)
            val syntheticProb = obj.optDouble("synthetic_probability", 0.5)
                .toFloat()
                .coerceIn(0f, 1f)
            val confidence = obj.optDouble("confidence", 0.0)
                .toFloat()
                .coerceIn(0f, 1f)
            val modelVer = obj.optString("model_version", MODEL_VERSION)
            val serverInferenceMs = obj.optLong("inference_ms", -1L)
            val inferenceStartedAtMs = obj.optLong("inference_started_at_ms", -1L)
            val inferenceFinishedAtMs = obj.optLong("inference_finished_at_ms", -1L)

            Log.i(
                TAG,
                "VOICE_AUTH_TIMING server_inference_start_ms=$inferenceStartedAtMs " +
                    "server_inference_end_ms=$inferenceFinishedAtMs " +
                    "server_inference_ms=$serverInferenceMs"
            )

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
                inferenceLatencyMs = roundTripMs
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse response: ${e.message}")
            uncertainAssessment(durationMs, roundTripMs, "Parse error")
        }
    }

    private fun uncertainAssessment(
        durationMs: Long,
        inferenceMs: Long,
        reason: String
    ): VoiceAuthenticityAssessment {
        Log.w(TAG, "Returning UNCERTAIN: $reason")
        return VoiceAuthenticityAssessment(
            label = VoiceAuthenticityLabel.UNCERTAIN,
            syntheticProbability = 0.5f,
            confidence = 0f,
            analyzedDurationMs = durationMs,
            modelVersion = MODEL_VERSION,
            inferenceLatencyMs = inferenceMs
        )
    }

    private fun shortsToBytes(shorts: ShortArray): ByteArray {
        val baos = ByteArrayOutputStream(shorts.size * 2)
        val dos = DataOutputStream(baos)
        for (sample in shorts) {
            dos.writeByte(sample.toInt() and 0xFF)
            dos.writeByte((sample.toInt() shr 8) and 0xFF)
        }
        dos.flush()
        return baos.toByteArray()
    }
}
