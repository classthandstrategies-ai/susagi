package com.guardian.app.device

import android.content.Context
import android.util.Log
import com.guardian.app.BuildConfig
import com.guardian.app.auth.SupabaseAuthManager
import com.guardian.app.network.PlatformApiClient

/**
 * Reusable coordinator to register the device and any pending FCM token once
 * an authenticated Supabase session becomes available.
 */
object DeviceRegistrationCoordinator {

    private const val TAG = "DeviceRegCoord"

    suspend fun registerCurrentTokenIfAvailable(
        context: Context,
        authManager: SupabaseAuthManager = SupabaseAuthManager(),
        baseUrl: String = BuildConfig.BACKEND_URL
    ): Result<Boolean> {
        val token = DeviceIdentityStore.getPendingFcmToken(context)
        val uid = authManager.currentUserId
        val deviceId = DeviceIdentityStore.getOrCreateDeviceId(context)

        return registerToken(
            token = token,
            userId = uid,
            deviceId = deviceId,
            registerCall = { devId, fcmTok ->
                val client = PlatformApiClient(authManager = authManager, baseUrl = baseUrl)
                client.registerDevice(
                    deviceId = devId,
                    fcmToken = fcmTok,
                    platform = "android"
                )
            },
            onSuccess = { fcmTok ->
                DeviceIdentityStore.markTokenSynced(context, fcmTok)
            }
        )
    }

    /**
     * Testable core logic for device registration decision tree.
     */
    internal suspend fun registerToken(
        token: String?,
        userId: String?,
        deviceId: String,
        registerCall: suspend (deviceId: String, token: String) -> Result<Boolean>,
        onSuccess: (token: String) -> Unit
    ): Result<Boolean> {
        if (token.isNullOrBlank()) {
            Log.d(TAG, "No pending FCM token found for registration")
            return Result.success(false)
        }

        if (userId.isNullOrBlank()) {
            Log.d(TAG, "Cannot register device: No active Supabase authenticated user")
            return Result.failure(IllegalStateException("Unauthenticated: Supabase user required for device registration"))
        }

        Log.d(TAG, "Attempting device registration for device $deviceId with backend")
        val result = registerCall(deviceId, token)

        return if (result.isSuccess) {
            onSuccess(token)
            Log.d(TAG, "Successfully registered device $deviceId and marked token synced")
            Result.success(true)
        } else {
            val ex = result.exceptionOrNull() ?: Exception("Device registration failed")
            Log.w(TAG, "Device registration failed: ${ex.message}")
            Result.failure(ex)
        }
    }
}
