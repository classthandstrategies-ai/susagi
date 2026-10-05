package com.guardian.app.verification

/**
 * Canonical platform domain model representing an authorized trusted contact relationship.
 *
 * Contract:
 * - [trustedUserId]: UID of the trusted contact who can verify alerts for the protected user.
 * - [displayName]: Human-readable display name assigned to the trusted contact.
 * - [relationship]: Categorization of relationship (e.g. "Family", "Friend").
 * - [enabled]: Whether this verification relationship is currently authorized and active.
 */
data class TrustedContactRelationship(
    val trustedUserId: String = "",
    val displayName: String = "",
    val relationship: String = "",
    val enabled: Boolean = true
)
