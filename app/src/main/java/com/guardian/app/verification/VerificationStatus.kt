package com.guardian.app.verification

/**
 * Canonical status lifecycle for a VerificationSession.
 *
 * Rules:
 * - PENDING: Initial state while awaiting verification response or timeout.
 * - VERIFIED: Explicit confirmation received from trusted contact.
 * - REJECTED: Explicit rejection received from trusted contact.
 * - EXPIRED: Window elapsed without response (Timeout is NOT rejection).
 * - UNAVAILABLE: Trusted contact unreachable or delivery failed (UNAVAILABLE is NOT rejection).
 */
enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    EXPIRED,
    UNAVAILABLE;

    val isTerminal: Boolean
        get() = this in TERMINAL_STATUSES

    companion object {
        val TERMINAL_STATUSES = setOf(
            VERIFIED,
            REJECTED,
            EXPIRED,
            UNAVAILABLE
        )
    }
}
