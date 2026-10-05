package com.guardian.app.ui.guardians

import com.guardian.app.verification.VerificationSession
import com.guardian.app.verification.VerificationStatus

/**
 * Focused Product UI state for Phone B Verification Response flow.
 */
sealed interface VerificationResponseUiState {
    /**
     * Initial calm loading state while the canonical VerificationSession is fetched.
     */
    data object Loading : VerificationResponseUiState

    /**
     * Authoritative session state successfully loaded from backend.
     */
    data class Loaded(
        val session: VerificationSession
    ) : VerificationResponseUiState

    /**
     * In-flight submission state (Yes/No response in progress).
     * Decision buttons must be disabled immediately to prevent duplicate submissions.
     */
    data class Submitting(
        val session: VerificationSession,
        val selectedResponse: VerificationStatus
    ) : VerificationResponseUiState

    /**
     * Error state with mapped, user-facing error message and retry capability.
     */
    data class Error(
        val userFacingMessage: String,
        val retryAllowed: Boolean = true,
        val sessionId: String
    ) : VerificationResponseUiState

    /**
     * Session is definitively unavailable (e.g. 404 or destroyed).
     */
    data class Unavailable(
        val message: String = "This identity check is unavailable."
    ) : VerificationResponseUiState
}
