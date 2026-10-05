package com.guardian.app.device

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Manages stable, persistent, privacy-preserving device identity and FCM token sync state.
 *
 * Invariants:
 * - App-scoped random UUID generated exactly once per install.
 * - Reused across all app launches and services.
 * - Never uses IMEI, phone number, MAC address, hardware serial, or advertising ID.
 * - Caches pending FCM tokens when offline or unauthenticated.
 * - Idempotently tracks synced tokens to avoid redundant registrations.
 */
object DeviceIdentityStore {

    private const val PREFS_NAME = "guardian_device_identity"
    private const val KEY_DEVICE_ID = "app_scoped_device_id"
    private const val KEY_PENDING_FCM_TOKEN = "pending_fcm_token"
    private const val KEY_SYNCED_FCM_TOKEN = "synced_fcm_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves the stable app-scoped device ID, generating and persisting a random UUID if not present.
     */
    @Synchronized
    fun getOrCreateDeviceId(context: Context): String {
        val prefs = getPrefs(context)
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }

        val newDeviceId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, newDeviceId).apply()
        return newDeviceId
    }

    /**
     * Saves an FCM token that needs to be registered with the backend.
     */
    fun savePendingFcmToken(context: Context, token: String) {
        if (token.isBlank()) return
        getPrefs(context).edit().putString(KEY_PENDING_FCM_TOKEN, token).apply()
    }

    /**
     * Retrieves the current pending FCM token waiting to be registered.
     */
    fun getPendingFcmToken(context: Context): String? {
        return getPrefs(context).getString(KEY_PENDING_FCM_TOKEN, null)
    }

    /**
     * Clears the pending token once registration succeeds.
     */
    fun clearPendingFcmToken(context: Context) {
        getPrefs(context).edit().remove(KEY_PENDING_FCM_TOKEN).apply()
    }

    /**
     * Records that an FCM token has been successfully registered with the backend.
     */
    fun markTokenSynced(context: Context, token: String) {
        getPrefs(context).edit()
            .putString(KEY_SYNCED_FCM_TOKEN, token)
            .remove(KEY_PENDING_FCM_TOKEN)
            .apply()
    }

    /**
     * Checks if the given token is already synced with the backend for this device.
     */
    fun isTokenSynced(context: Context, token: String): Boolean {
        val synced = getPrefs(context).getString(KEY_SYNCED_FCM_TOKEN, null)
        return synced == token
    }
}
