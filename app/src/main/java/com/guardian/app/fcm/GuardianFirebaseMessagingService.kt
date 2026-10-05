package com.guardian.app.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.guardian.app.BuildConfig
import com.guardian.app.device.DeviceIdentityStore
import com.guardian.app.network.PlatformApiClient
import com.guardian.app.supabase.SupabaseClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GuardianFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("GuardianFCM", "New FCM token received")
        DeviceIdentityStore.savePendingFcmToken(applicationContext, token)
        syncDeviceToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("GuardianFCM", "Message received from: ${remoteMessage.from}")

        // Handle data payload
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val messageType = data["type"]

            if (messageType == VerificationPushPayload.PUSH_TYPE) {
                handleVerificationPush(data)
            } else if (data.containsKey("alert_type") || data.containsKey("protected_user")) {
                handleFamilyAlert(data)
            } else {
                Log.d("GuardianFCM", "Unrecognized data payload received")
            }
        }

        // Handle notification payload (if present)
        remoteMessage.notification?.let {
            Log.d("GuardianFCM", "Notification: ${it.title} - ${it.body}")
        }
    }

    private fun handleVerificationPush(data: Map<String, String>) {
        val payload = VerificationPushPayload.parse(data)
        if (payload == null) {
            Log.w("GuardianFCM", "Ignored malformed or invalid identity_verification push payload")
            return
        }

        if (payload.isExpired()) {
            Log.d("GuardianFCM", "Ignored already expired verification session push: ${payload.sessionId}")
            return
        }

        Log.d("GuardianFCM", "Rendering verification notification for session: ${payload.sessionId}")
        GuardianNotificationHelper.showVerificationAlert(applicationContext, payload)
    }

    private fun handleFamilyAlert(data: Map<String, String>) {
        val alertType = data["alert_type"] ?: "scam_detected"
        val protectedUserName = data["protected_user"] ?: "Family member"
        val riskScore = data["risk_score"]?.toIntOrNull() ?: 0
        val scamType = data["scam_type"] ?: "Unknown"
        val callerNumber = data["caller_number"] ?: "Unknown"
        val transcriptSummary = data["transcript_summary"] ?: ""

        // Show a local notification with detailed info
        GuardianNotificationHelper.showFamilyAlert(
            context = this,
            alertType = alertType,
            protectedUserName = protectedUserName,
            riskScore = riskScore,
            scamType = scamType,
            callerNumber = callerNumber,
            transcriptSummary = transcriptSummary
        )

        // Save to local database for the in-app report
        CoroutineScope(Dispatchers.IO).launch {
            FamilyAlertStore.save(
                context = applicationContext,
                alertId = data["alert_id"] ?: System.currentTimeMillis().toString(),
                protectedUserName = protectedUserName,
                riskScore = riskScore,
                scamType = scamType,
                callerNumber = callerNumber,
                transcriptSummary = transcriptSummary,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Registers the device and FCM token authoritatively with the backend using Supabase Auth.
     */
    fun syncDeviceToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val deviceId = DeviceIdentityStore.getOrCreateDeviceId(applicationContext)

            // 1. Authenticated registration with Node backend -> Supabase devices table
            try {
                val authManager = com.guardian.app.auth.SupabaseAuthManager()
                val platformClient = PlatformApiClient(
                    baseUrl = BuildConfig.BACKEND_URL,
                    authManager = authManager
                )

                val result = platformClient.registerDevice(
                    deviceId = deviceId,
                    fcmToken = token,
                    platform = "android"
                )

                if (result.isSuccess) {
                    DeviceIdentityStore.markTokenSynced(applicationContext, token)
                    Log.d("GuardianFCM", "Device $deviceId successfully registered with backend")
                } else {
                    Log.w("GuardianFCM", "Backend device registration deferred: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.w("GuardianFCM", "Backend device registration attempt failed: ${e.message}")
            }

            // 2. Legacy fallback to /family/register to preserve family alert compatibility
            try {
                val legacyPayload = JSONObject().apply {
                    put("fcmToken", token)
                    put("userId", getUserId())
                    put("deviceId", deviceId)
                    put("timestamp", System.currentTimeMillis())
                }

                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_URL}/family/register")
                    .post(legacyPayload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                OkHttpClient().newCall(request).execute()
            } catch (_: Exception) {
                // Ignore legacy errors
            }
        }
    }

    fun sendTokenToBackend(token: String) {
        DeviceIdentityStore.savePendingFcmToken(applicationContext, token)
        syncDeviceToken(token)
    }

    private fun getUserId(): String {
        return getSharedPreferences("guardian_secure_prefs", MODE_PRIVATE)
            .getString("user_id", "anonymous") ?: "anonymous"
    }
}
