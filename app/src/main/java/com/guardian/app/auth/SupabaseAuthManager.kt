package com.guardian.app.auth

import com.guardian.app.supabase.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

/**
 * Abstraction layer over Supabase Authentication.
 *
 * Responsibilities:
 * - Expose authenticated Supabase UID (UUID).
 * - Ensure an authenticated session exists (via real Supabase anonymous auth).
 * - Retrieve valid Supabase JWT access tokens for authenticated backend requests.
 * - Explicitly report all authentication and configuration failures.
 *
 * Independent of Compose/UI.
 */
class SupabaseAuthManager(
    private val clientProvider: () -> SupabaseClient = { SupabaseClientProvider.client }
) {
    private val client: SupabaseClient
        get() = clientProvider()

    val currentUserId: String?
        get() = if (SupabaseClientProvider.isConfigured) {
            try {
                client.auth.currentUserOrNull()?.id
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }

    /**
     * Ensures an authenticated user exists. If already signed in, returns existing UID.
     * Otherwise, signs in anonymously.
     */
    suspend fun ensureAuthenticated(): Result<String> {
        if (!SupabaseClientProvider.isConfigured) {
            return Result.failure(
                IllegalStateException("Supabase configuration is missing. Set SUPABASE_URL and SUPABASE_ANON_KEY in local.properties.")
            )
        }
        val uid = currentUserId
        if (!uid.isNullOrBlank()) {
            return Result.success(uid)
        }
        return signInAnonymously()
    }

    /**
     * Signs in anonymously to obtain a real Supabase user UID and session.
     */
    suspend fun signInAnonymously(): Result<String> {
        if (!SupabaseClientProvider.isConfigured) {
            return Result.failure(
                IllegalStateException("Supabase configuration is missing. Set SUPABASE_URL and SUPABASE_ANON_KEY in local.properties.")
            )
        }
        return try {
            client.auth.signInAnonymously()
            val uid = currentUserId
            if (!uid.isNullOrBlank()) {
                Result.success(uid)
            } else {
                Result.failure(IllegalStateException("Supabase user ID was null after anonymous sign-in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a valid Supabase access token for authenticating against backend endpoints.
     */
    suspend fun getAccessToken(): Result<String> {
        if (!SupabaseClientProvider.isConfigured) {
            return Result.failure(
                IllegalStateException("Supabase configuration is missing. Set SUPABASE_URL and SUPABASE_ANON_KEY in local.properties.")
            )
        }
        return try {
            val token = client.auth.currentAccessTokenOrNull()
            if (!token.isNullOrBlank()) {
                Result.success(token)
            } else {
                val authResult = ensureAuthenticated()
                if (authResult.isFailure) {
                    return Result.failure(
                        authResult.exceptionOrNull() ?: IllegalStateException("Authentication failed: unable to ensure session")
                    )
                }
                val freshToken = client.auth.currentAccessTokenOrNull()
                if (!freshToken.isNullOrBlank()) {
                    Result.success(freshToken)
                } else {
                    Result.failure(IllegalStateException("Retrieved null or empty Supabase access token"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user session.
     */
    suspend fun signOut() {
        if (!SupabaseClientProvider.isConfigured) return
        try {
            client.auth.signOut()
        } catch (_: Exception) {
            // Ignored on best-effort sign out
        }
    }
}
