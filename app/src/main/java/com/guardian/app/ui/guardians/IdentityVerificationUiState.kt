package com.guardian.app.ui.guardians

/* UI CONTRACT — AWAITING PLATFORM VerificationSession INTEGRATION */

/**
 * High-level lifecycle states for CP3 Out-of-Band Identity Verification.
 */
enum class VerificationStatus {
    /** Initial state before request is initiated, or if platform is not yet linked. */
    READY,
    /** Verification challenge dispatched to Guardian phone; waiting for responder decision. */
    PENDING,
    /** Guardian responded [YES, IT'S ME]. Caller identity confirmed. */
    VERIFIED,
    /** Guardian responded [NO, NOT ME]. Impersonation scam detected! */
    REJECTED,
    /** Request timed out (e.g. 45 seconds expired) without guardian response. */
    EXPIRED,
    /** Network or transport failure; guardian device unreachable or platform unavailable. */
    UNAVAILABLE
}

/**
 * UI State for Phone A (The user currently on a suspicious incoming call who initiates verification).
 */
data class RequesterVerificationUiModel(
    val callerName: String,
    val callerNumber: String,
    val guardianName: String,
    val guardianNumber: String,
    val status: VerificationStatus = VerificationStatus.READY,
    val remainingSeconds: Int = 45,
    val details: String? = null,
    val isPlatformIntegrated: Boolean = false
)

/**
 * UI State for Phone B (The trusted contact who receives the urgent out-of-band verification challenge).
 */
data class ResponderIdentityCheckUiModel(
    val requesterName: String,
    val requesterNumber: String,
    val claimDetail: String,
    val amountOrAction: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)
