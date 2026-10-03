package com.guardian.app.fcm

/**
 * Data contract representing an incoming high-priority identity verification push notification.
 *
 * Contract fields:
 * - type: "identity_verification"
 * - sessionId: Non-empty canonical verification session ID
 * - claimedIdentity: Non-empty identity being claimed by the caller
 * - expiresAt: Expiration epoch timestamp in milliseconds
 * - requestedAction: Optional requested action string
 */
data class VerificationPushPayload(
    val sessionId: String,
    val claimedIdentity: String,
    val expiresAt: Long,
    val requestedAction: String? = null
) {
    /**
     * Returns true if the session is past its expiration time.
     */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return now >= expiresAt
    }

    companion object {
        const val PUSH_TYPE = "identity_verification"

        /**
         * Safely parses an FCM data payload into a VerificationPushPayload.
         * Returns null if the payload is not an identity verification message or is malformed.
         */
        fun parse(data: Map<String, String>): VerificationPushPayload? {
            val type = data["type"]
            if (type != PUSH_TYPE) {
                return null
            }

            val sessionId = data["sessionId"]?.trim()
            if (sessionId.isNullOrEmpty()) {
                return null
            }

            val claimedIdentity = data["claimedIdentity"]?.trim()
            if (claimedIdentity.isNullOrEmpty()) {
                return null
            }

            val expiresAtStr = data["expiresAt"]?.trim()
            val expiresAt = expiresAtStr?.toLongOrNull()
            if (expiresAt == null || expiresAt <= 0) {
                return null
            }

            val requestedAction = data["requestedAction"]?.trim()?.ifEmpty { null }

            return VerificationPushPayload(
                sessionId = sessionId,
                claimedIdentity = claimedIdentity,
                expiresAt = expiresAt,
                requestedAction = requestedAction
            )
        }
    }
}
