package com.guardian.app.network

import com.guardian.app.BuildConfig
import com.guardian.app.auth.SupabaseAuthManager
import com.guardian.app.verification.VerificationSession
import com.guardian.app.verification.VerificationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Authoritative HTTP API client for SuSagi Platform services.
 *
 * Responsibilities:
 * - Authenticated device registration
 * - Authoritative verification session creation
 * - Verification session retrieval with lazy expiry
 * - Trusted contact verification response submission
 *
 * Security:
 * - Automatically acquires and attaches Supabase access token via [SupabaseAuthManager].
 * - Rejects unauthenticated operations with [IllegalStateException].
 * - Never logs or exposes raw tokens.
 * - Decoupled from Android UI/Compose layers.
 */
class PlatformApiClient(
    private val authManager: SupabaseAuthManager = SupabaseAuthManager(),
    private val baseUrl: String = BuildConfig.BACKEND_URL,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Registers a device FCM token with the backend device registry.
     */
    suspend fun registerDevice(
        deviceId: String,
        fcmToken: String,
        platform: String = "android"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val tokenResult = authManager.getAccessToken()
        if (tokenResult.isFailure) {
            return@withContext Result.failure(
                tokenResult.exceptionOrNull() ?: IllegalStateException("Authentication failed: unable to obtain access token")
            )
        }
        val accessToken = tokenResult.getOrThrow()

        val payload = JSONObject().apply {
            put("deviceId", deviceId)
            put("fcmToken", fcmToken)
            put("platform", platform)
        }

        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/v1/devices/register")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        executeRequest(request) { json ->
            json.optBoolean("success", true)
        }
    }

    /**
     * Authoritatively creates a new VerificationSession.
     */
    suspend fun createVerification(
        trustedUserId: String,
        claimedIdentity: String,
        requestedAction: String,
        requestSummary: String,
        riskScoreAtCreation: Int
    ): Result<VerificationSession> = withContext(Dispatchers.IO) {
        val tokenResult = authManager.getAccessToken()
        if (tokenResult.isFailure) {
            return@withContext Result.failure(
                tokenResult.exceptionOrNull() ?: IllegalStateException("Authentication failed: unable to obtain access token")
            )
        }
        val accessToken = tokenResult.getOrThrow()

        val payload = JSONObject().apply {
            put("trustedUserId", trustedUserId)
            put("claimedIdentity", claimedIdentity)
            put("requestedAction", requestedAction)
            put("requestSummary", requestSummary)
            put("riskScoreAtCreation", riskScoreAtCreation)
        }

        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/v1/verifications")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        executeRequest(request) { json ->
            val sessionJson = json.optJSONObject("session")
                ?: throw IOException("Malformed response: missing 'session' object")
            parseVerificationSession(sessionJson)
        }
    }

    /**
     * Retrieves an authoritative VerificationSession snapshot by ID.
     */
    suspend fun getVerification(sessionId: String): Result<VerificationSession> = withContext(Dispatchers.IO) {
        val tokenResult = authManager.getAccessToken()
        if (tokenResult.isFailure) {
            return@withContext Result.failure(
                tokenResult.exceptionOrNull() ?: IllegalStateException("Authentication failed: unable to obtain access token")
            )
        }
        val accessToken = tokenResult.getOrThrow()

        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/v1/verifications/$sessionId")
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        executeRequest(request) { json ->
            val sessionJson = json.optJSONObject("session")
                ?: throw IOException("Malformed response: missing 'session' object")
            parseVerificationSession(sessionJson)
        }
    }

    /**
     * Submits an authoritative trusted-contact response (VERIFIED or REJECTED).
     */
    suspend fun respondToVerification(
        sessionId: String,
        response: VerificationStatus,
        deviceId: String
    ): Result<VerificationSession> = withContext(Dispatchers.IO) {
        if (response != VerificationStatus.VERIFIED && response != VerificationStatus.REJECTED) {
            return@withContext Result.failure(
                IllegalArgumentException("Client response must be VERIFIED or REJECTED (got $response)")
            )
        }

        val tokenResult = authManager.getAccessToken()
        if (tokenResult.isFailure) {
            return@withContext Result.failure(
                tokenResult.exceptionOrNull() ?: IllegalStateException("Authentication failed: unable to obtain access token")
            )
        }
        val accessToken = tokenResult.getOrThrow()

        val payload = JSONObject().apply {
            put("response", response.name)
            put("deviceId", deviceId)
        }

        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/v1/verifications/$sessionId/respond")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        executeRequest(request) { json ->
            val sessionJson = json.optJSONObject("session")
                ?: throw IOException("Malformed response: missing 'session' object")
            parseVerificationSession(sessionJson)
        }
    }

    private fun <T> executeRequest(request: Request, parser: (JSONObject) -> T): Result<T> {
        return try {
            val httpResponse: Response = httpClient.newCall(request).execute()
            httpResponse.use { resp ->
                val bodyString = resp.body?.string() ?: ""
                val json = if (bodyString.isNotBlank()) {
                    try {
                        JSONObject(bodyString)
                    } catch (_: Exception) {
                        JSONObject()
                    }
                } else {
                    JSONObject()
                }

                if (!resp.isSuccessful) {
                    val message = json.optString("message").ifBlank {
                        json.optString("error").ifBlank { "HTTP ${resp.code}: ${resp.message}" }
                    }
                    return Result.failure(IOException(message))
                }

                Result.success(parser(json))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        /**
         * Deserializes a backend JSON object into a domain [VerificationSession].
         */
        fun parseVerificationSession(json: JSONObject): VerificationSession {
            val rawStatus = json.optString("status", VerificationStatus.PENDING.name)
            val status = try {
                VerificationStatus.valueOf(rawStatus)
            } catch (_: Exception) {
                VerificationStatus.PENDING
            }

            val respondedAt = if (json.has("respondedAt") && !json.isNull("respondedAt")) {
                json.optLong("respondedAt")
            } else {
                null
            }

            val responseDeviceId = if (json.has("responseDeviceId") && !json.isNull("responseDeviceId")) {
                json.optString("responseDeviceId")
            } else {
                null
            }

            return VerificationSession(
                id = json.optString("id", ""),
                protectedUserId = json.optString("protectedUserId", ""),
                trustedUserId = json.optString("trustedUserId", ""),
                claimedIdentity = json.optString("claimedIdentity", ""),
                requestedAction = json.optString("requestedAction", ""),
                requestSummary = json.optString("requestSummary", ""),
                riskScoreAtCreation = json.optInt("riskScoreAtCreation", 0),
                status = status,
                createdAt = json.optLong("createdAt", 0L),
                expiresAt = json.optLong("expiresAt", 0L),
                respondedAt = respondedAt,
                responseDeviceId = responseDeviceId,
                version = json.optInt("version", 1)
            )
        }
    }
}
