package com.guardian.app.verification

import kotlinx.coroutines.flow.Flow

/**
 * Platform/domain-level abstraction for interacting with VerificationSessions.
 *
 * Rules:
 * - Does not expose raw Firestore objects (DocumentSnapshot, Task, ListenerRegistration).
 * - Exposes clean Kotlin domain types ([VerificationSession], [Flow], [Result]).
 * - Creation is marked as a backend boundary to prevent unauthenticated client-side state fabrication.
 */
interface VerificationRepository {

    /**
     * Authoritative creation boundary:
     * Verification sessions are created authoritatively by the authenticated backend.
     * Direct Android client-side writes to Firestore are prohibited to maintain security invariants.
     */
    suspend fun createVerification(session: VerificationSession): Result<VerificationSession>

    /**
     * Observe realtime state changes of a verification session.
     * Emits null if the session does not exist.
     * Completes or errors cleanly when collection ceases.
     */
    fun observeVerification(sessionId: String): Flow<VerificationSession?>

    /**
     * Fetch current one-shot snapshot of a verification session.
     * Returns Result.success(null) if document does not exist.
     */
    suspend fun getVerification(sessionId: String): Result<VerificationSession?>
}
