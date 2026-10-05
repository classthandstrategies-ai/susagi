package com.guardian.app.verification

/**
 * Canonical domain model representing an out-of-band verification session.
 *
 * Contract:
 * - [id]: Unique session identifier (matches Firestore document ID).
 * - [protectedUserId]: UID of the protected user experiencing suspicious activity.
 * - [trustedUserId]: UID of the trusted contact asked to verify.
 * - [claimedIdentity]: Identity claimed by the suspicious caller/actor (e.g. "Bank Manager", "Police").
 * - [requestedAction]: Action requested of the victim (e.g. "Transfer funds", "Share OTP").
 * - [requestSummary]: Brief summary of the context prompting verification.
 * - [riskScoreAtCreation]: Realtime risk score at session initiation (0-100).
 * - [status]: Current [VerificationStatus] lifecycle state.
 * - [createdAt]: Epoch timestamp (ms) when session was created.
 * - [expiresAt]: Epoch timestamp (ms) after which session automatically transitions to EXPIRED.
 * - [respondedAt]: Epoch timestamp (ms) when trusted contact submitted response (null until responded).
 * - [responseDeviceId]: Hardware/device identifier submitting response (null until responded).
 * - [version]: Schema revision for optimistic locking and backward-compatibility.
 */
data class VerificationSession(
    val id: String = "",
    val protectedUserId: String = "",
    val trustedUserId: String = "",
    val claimedIdentity: String = "",
    val requestedAction: String = "",
    val requestSummary: String = "",
    val riskScoreAtCreation: Int = 0,
    val status: VerificationStatus = VerificationStatus.PENDING,
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val respondedAt: Long? = null,
    val responseDeviceId: String? = null,
    val version: Int = 1
) {
    /**
     * Checks if this session is in a terminal status.
     */
    fun isTerminal(): Boolean = status.isTerminal

    /**
     * Checks if session should be treated as expired based on current wall clock.
     */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return status == VerificationStatus.PENDING && expiresAt > 0L && now >= expiresAt
    }
}
