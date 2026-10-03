package com.guardian.app.device

import android.content.Context
import android.util.Log
import com.guardian.app.BuildConfig
import com.guardian.app.auth.SupabaseAuthManager
import com.guardian.app.network.PlatformApiClient

/**
 * Reusable coordinator to register the device and any pending FCM token once
 * an authenticated Supabase session becomes available.
 *
 * NOTE: Wiring to MainActivity or the sign-in flow requires supervisor approval.
 */
object DeviceRegistrationCoordinator {

    private const val TAG = "DeviceRegCoord"

    suspend fun registerCurrentTokenIfAvailable(
        context: Context,
        authManager: SupabaseAuthManager = SupabaseAuthManager(),
        baseUrl: String = BuildConfig.BACKEND_URL
    ): Result<Boolean> {
        val token = DeviceIdentityStore.getPendingFcmToken(context)
        if (token.isNullOrBlank()) {
            Log.d(TAG, "No pending FCM token found for registration")
            return Result.success(false)
        }

        val uid = authManager.currentUserId
        if (uid.isNullOrBlank()) {
            Log.d(TAG, "Cannot register device: No active Supabase authenticated user")
            return Result.failure(IllegalStateException("Unauthenticated: Supabase user required for device registration"))
        }

        val deviceId = DeviceIdentityStore.getOrCreateDeviceId(context)
        Log.d(TAG, "Attempting device registration for device $deviceId with backend")

        val client = PlatformApiClient(authManager = authManager, baseUrl = baseUrl)
        val result = client.registerDevice(
            deviceId = deviceId,
            fcmToken = token,
            platform = "android"
        )

        return if (result.isSuccess) {
            DeviceIdentityStore.markTokenSynced(context, token)
            Log.d(TAG, "Successfully registered device $deviceId and marked token synced")
            Result.success(true)
        } else {
            val ex = result.exceptionOrNull() ?: Exception("Device registration failed")
            Log.w(TAG, "Device registration failed: ${ex.message}")
            Result.failure(ex)
        }
    }
}
