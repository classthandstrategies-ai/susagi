package com.guardian.app.ui.guardians

import android.content.Intent

/**
 * Parsed navigation route for Phone B identity verification response flow.
 */
data class VerificationNotificationRoute(
    val sessionId: String
)

/**
 * Isolated intent parser for identity verification notifications.
 *
 * Rules:
 * - Only intent extras with source == "identity_verification" are routed.
 * - verificationSessionId must be present and non-blank.
 * - Transport hints (claimedIdentity, expiresAt, requestedAction) are never used
 *   as canonical state and are not routed into domain state.
 */
object VerificationNotificationIntentParser {
    const val EXPECTED_SOURCE = "identity_verification"
    const val EXTRA_SESSION_ID = "verificationSessionId"
    const val EXTRA_SOURCE = "source"

    fun parse(source: String?, sessionId: String?): VerificationNotificationRoute? {
        if (source != EXPECTED_SOURCE) return null
        val trimmedId = sessionId?.trim()
        if (trimmedId.isNullOrEmpty()) return null
        return VerificationNotificationRoute(sessionId = trimmedId)
    }

    fun parse(intent: Intent?): VerificationNotificationRoute? {
        if (intent == null) return null
        return parse(
            source = intent.getStringExtra(EXTRA_SOURCE),
            sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        )
    }
}
