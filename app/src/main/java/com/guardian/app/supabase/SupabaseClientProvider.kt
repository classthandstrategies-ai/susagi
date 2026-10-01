package com.guardian.app.supabase

import com.guardian.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Singleton provider for the client-side [SupabaseClient].
 *
 * Configured with CLIENT-SAFE public credentials:
 * - [BuildConfig.SUPABASE_URL]
 * - [BuildConfig.SUPABASE_ANON_KEY]
 *
 * Fails clearly and explicitly when configuration is missing.
 * NEVER exposes or contains SUPABASE_SERVICE_ROLE_KEY.
 */
object SupabaseClientProvider {

    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client: SupabaseClient by lazy {
        val url = BuildConfig.SUPABASE_URL.trim()
        val anonKey = BuildConfig.SUPABASE_ANON_KEY.trim()

        if (url.isBlank() || anonKey.isBlank()) {
            throw IllegalStateException(
                "Supabase configuration is missing. Please set SUPABASE_URL and SUPABASE_ANON_KEY in local.properties or build configuration."
            )
        }

        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
