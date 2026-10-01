package com.guardian.app.verification

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Await helper for Firebase Tasks ensuring clean coroutine cancellation.
 */
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        cont.cancel()
    }
}

/**
 * Firestore-backed implementation of [VerificationRepository].
 *
 * Canonical collection: verificationSessions/{sessionId}
 *
 * Enforces architectural boundaries:
 * - Direct client-side creation is rejected with [UnsupportedOperationException];
 *   sessions must be created authoritatively via the backend.
 * - Realtime push observation uses Firestore SnapshotListener with automatic lifecycle cleanup.
 * - Errors are propagated downstream into the Flow rather than swallowed.
 */
class FirestoreVerificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : VerificationRepository {

    companion object {
        const val COLLECTION_NAME = "verificationSessions"

        /**
         * Deserializes a Firestore document payload into a canonical [VerificationSession].
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
                protectedUserId = data["protectedUserId"] as? String ?: "",
                trustedUserId = data["trustedUserId"] as? String ?: "",
                claimedIdentity = data["claimedIdentity"] as? String ?: "",
                requestedAction = data["requestedAction"] as? String ?: "",
                requestSummary = data["requestSummary"] as? String ?: "",
                riskScoreAtCreation = (data["riskScoreAtCreation"] as? Number)?.toInt() ?: 0,
                status = status,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                expiresAt = (data["expiresAt"] as? Number)?.toLong() ?: 0L,
                respondedAt = (data["respondedAt"] as? Number)?.toLong(),
                responseDeviceId = data["responseDeviceId"] as? String,
                version = (data["version"] as? Number)?.toInt() ?: 1
            )
        }

        /**
         * Serializes a [VerificationSession] into a map representation for Firestore.
         */
        fun toMap(session: VerificationSession): Map<String, Any?> {
            return mapOf(
                "protectedUserId" to session.protectedUserId,
                "trustedUserId" to session.trustedUserId,
                "claimedIdentity" to session.claimedIdentity,
                "requestedAction" to session.requestedAction,
                "requestSummary" to session.requestSummary,
                "riskScoreAtCreation" to session.riskScoreAtCreation,
                "status" to session.status.name,
                "createdAt" to session.createdAt,
                "expiresAt" to session.expiresAt,
                "respondedAt" to session.respondedAt,
                "responseDeviceId" to session.responseDeviceId,
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
     * Observes real-time snapshot updates on verificationSessions/{sessionId}.
     * Emits null if the session document does not exist.
     * Cleans up the listener when Flow collection cancels.
     */
    override fun observeVerification(sessionId: String): Flow<VerificationSession?> = callbackFlow {
        if (sessionId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection(COLLECTION_NAME).document(sessionId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                trySend(null)
                return@addSnapshotListener
            }

            try {
                val session = fromMap(snapshot.id, snapshot.data ?: emptyMap())
                trySend(session)
            } catch (e: Exception) {
                close(e)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Fetches current snapshot of verificationSessions/{sessionId}.
     */
    override suspend fun getVerification(sessionId: String): Result<VerificationSession?> {
        if (sessionId.isBlank()) {
            return Result.failure(IllegalArgumentException("sessionId cannot be blank"))
        }

        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .document(sessionId)
                .get()
                .awaitTask()

            if (snapshot.exists()) {
                val session = fromMap(snapshot.id, snapshot.data ?: emptyMap())
                Result.success(session)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
