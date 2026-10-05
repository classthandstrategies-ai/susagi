package com.guardian.app.verification

import com.guardian.app.supabase.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object mapping the Supabase PostgreSQL verification_sessions schema.
 */
@Serializable
data class VerificationSessionRow(
    val id: String,
    @SerialName("protected_user_id") val protectedUserId: String,
    @SerialName("trusted_user_id") val trustedUserId: String,
    @SerialName("claimed_identity") val claimedIdentity: String,
    @SerialName("requested_action") val requestedAction: String,
    @SerialName("request_summary") val requestSummary: String,
    @SerialName("risk_score_at_creation") val riskScoreAtCreation: Int,
    val status: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("expires_at") val expiresAt: Long,
    @SerialName("responded_at") val respondedAt: Long? = null,
    @SerialName("response_device_id") val responseDeviceId: String? = null,
    val version: Int = 1
) {
    fun toDomain(): VerificationSession {
        val domainStatus = try {
            VerificationStatus.valueOf(status)
        } catch (_: Exception) {
            VerificationStatus.PENDING
        }
        return VerificationSession(
            id = id,
            protectedUserId = protectedUserId,
            trustedUserId = trustedUserId,
            claimedIdentity = claimedIdentity,
            requestedAction = requestedAction,
            requestSummary = requestSummary,
            riskScoreAtCreation = riskScoreAtCreation,
            status = domainStatus,
            createdAt = createdAt,
            expiresAt = expiresAt,
            respondedAt = respondedAt,
            responseDeviceId = responseDeviceId,
            version = version
        )
    }
}

/**
 * Supabase-backed implementation of [VerificationRepository].
 *
 * Canonical table: public.verification_sessions
 *
 * Enforces architectural boundaries:
 * - Direct client-side creation is rejected with [UnsupportedOperationException];
 *   sessions must be created authoritatively via the backend API.
 * - Realtime push observation uses Supabase Realtime Postgres Changes.
 * - Errors are propagated downstream into the Flow rather than swallowed.
 */
class SupabaseVerificationRepository(
    private val clientProvider: () -> SupabaseClient = { SupabaseClientProvider.client }
) : VerificationRepository {
    private val client: SupabaseClient
        get() = clientProvider()

    companion object {
        const val TABLE_NAME = "verification_sessions"

        /**
         * Deserializes a map representation (or PostgREST row) into a canonical [VerificationSession].
         * Supports both snake_case (PostgreSQL) and camelCase keys.
         */
        fun fromMap(id: String, data: Map<String, Any?>): VerificationSession {
            val rawStatus = data["status"] as? String ?: VerificationStatus.PENDING.name
            val status = try {
                VerificationStatus.valueOf(rawStatus)
            } catch (_: Exception) {
                VerificationStatus.PENDING
            }

            return VerificationSession(
                id = id,
                protectedUserId = data["protected_user_id"] as? String
                    ?: data["protectedUserId"] as? String ?: "",
                trustedUserId = data["trusted_user_id"] as? String
                    ?: data["trustedUserId"] as? String ?: "",
                claimedIdentity = data["claimed_identity"] as? String
                    ?: data["claimedIdentity"] as? String ?: "",
                requestedAction = data["requested_action"] as? String
                    ?: data["requestedAction"] as? String ?: "",
                requestSummary = data["request_summary"] as? String
                    ?: data["requestSummary"] as? String ?: "",
                riskScoreAtCreation = (data["risk_score_at_creation"] as? Number
                    ?: data["riskScoreAtCreation"] as? Number)?.toInt() ?: 0,
                status = status,
                createdAt = (data["created_at"] as? Number
                    ?: data["createdAt"] as? Number)?.toLong() ?: 0L,
                expiresAt = (data["expires_at"] as? Number
                    ?: data["expiresAt"] as? Number)?.toLong() ?: 0L,
                respondedAt = (data["responded_at"] as? Number
                    ?: data["respondedAt"] as? Number)?.toLong(),
                responseDeviceId = data["response_device_id"] as? String
                    ?: data["responseDeviceId"] as? String,
                version = (data["version"] as? Number
                    ?: data["version"] as? Number)?.toInt() ?: 1
            )
        }

        /**
         * Serializes a [VerificationSession] into a map representation for PostgreSQL/Supabase.
         */
        fun toMap(session: VerificationSession): Map<String, Any?> {
            return mapOf(
                "id" to session.id,
                "protected_user_id" to session.protectedUserId,
                "trusted_user_id" to session.trustedUserId,
                "claimed_identity" to session.claimedIdentity,
                "requested_action" to session.requestedAction,
                "request_summary" to session.requestSummary,
                "risk_score_at_creation" to session.riskScoreAtCreation,
                "status" to session.status.name,
                "created_at" to session.createdAt,
                "expires_at" to session.expiresAt,
                "responded_at" to session.respondedAt,
                "response_device_id" to session.responseDeviceId,
                "version" to session.version
            )
        }
    }

    /**
     * Rejects direct client-side session writes to enforce the backend authoritative boundary.
     */
    override suspend fun createVerification(session: VerificationSession): Result<VerificationSession> {
        return Result.failure(
            UnsupportedOperationException(
                "Authoritative VerificationSession creation must occur through the authenticated backend API. Direct client-side creation is prohibited."
            )
        )
    }

    /**
     * Observes real-time snapshot updates on verification_sessions via Supabase Realtime.
     * Emits null if the session does not exist.
     * Cleans up channel subscription when Flow collection cancels.
     */
    override fun observeVerification(sessionId: String): Flow<VerificationSession?> = callbackFlow {
        if (!SupabaseClientProvider.isConfigured) {
            close(IllegalStateException("Supabase configuration is missing. Set SUPABASE_URL and SUPABASE_ANON_KEY in local.properties."))
            return@callbackFlow
        }

        if (sessionId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        // 1. Fetch initial snapshot
        try {
            val initial = getVerification(sessionId).getOrNull()
            trySend(initial)
        } catch (e: Exception) {
            // Non-fatal for realtime observer start
        }

        // 2. Setup Realtime Postgres changes channel
        val channel = client.realtime.channel("public:verification_sessions:$sessionId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLE_NAME
            filter("id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, sessionId)
        }

        val listenerJob = launch {
            changeFlow.collect { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        try {
                            val row = action.decodeRecord<VerificationSessionRow>()
                            trySend(row.toDomain())
                        } catch (e: Exception) {
                            close(e)
                        }
                    }
                    is PostgresAction.Update -> {
                        try {
                            val row = action.decodeRecord<VerificationSessionRow>()
                            trySend(row.toDomain())
                        } catch (e: Exception) {
                            close(e)
                        }
                    }
                    is PostgresAction.Delete -> {
                        trySend(null)
                    }
                    else -> {
                        // Refresh snapshot for any other event
                        val refreshed = getVerification(sessionId).getOrNull()
                        trySend(refreshed)
                    }
                }
            }
        }

        try {
            channel.subscribe()
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            listenerJob.cancel()
            launch {
                try {
                    channel.unsubscribe()
                    client.realtime.removeChannel(channel)
                } catch (_: Exception) {
                    // Best effort cleanup
                }
            }
        }
    }

    /**
     * Fetches current snapshot of verification_sessions where id = sessionId.
     */
    override suspend fun getVerification(sessionId: String): Result<VerificationSession?> {
        if (!SupabaseClientProvider.isConfigured) {
            return Result.failure(
                IllegalStateException("Supabase configuration is missing. Set SUPABASE_URL and SUPABASE_ANON_KEY in local.properties.")
            )
        }

        if (sessionId.isBlank()) {
            return Result.failure(IllegalArgumentException("sessionId cannot be blank"))
        }

        return try {
            val row = client.from(TABLE_NAME)
                .select {
                    filter {
                        eq("id", sessionId)
                    }
                }
                .decodeSingleOrNull<VerificationSessionRow>()

            Result.success(row?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
