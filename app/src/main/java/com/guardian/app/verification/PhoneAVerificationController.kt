package com.guardian.app.verification

import com.guardian.app.domain.risk.RiskEngine
import com.guardian.app.network.PlatformApiClient
import com.guardian.app.ui.guardians.RequesterVerificationUiModel
import com.guardian.app.ui.guardians.VerificationStatus as UiStatus
import com.guardian.app.verification.VerificationStatus as DomainStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Presentation state for CP3 Phone A Verification runtime.
 */
data class PhoneAVerificationState(
    val isLookupLoading: Boolean = false,
    val isIdentityVerificationAvailable: Boolean = false,
    val unavailableReason: String? = null,
    val guardians: List<TrustedContactRelationship> = emptyList(),
    val selectedGuardian: TrustedContactRelationship? = null,
    val activeSession: VerificationSession? = null,
    val isCreationInFlight: Boolean = false,
    val requesterUiModel: RequesterVerificationUiModel = RequesterVerificationUiModel(
        callerName = "Current Call",
        callerNumber = "",
        guardianName = "",
        guardianNumber = "",
        status = UiStatus.READY,
        isPlatformIntegrated = false
    ),
    val activeRejection: Boolean = false,
    val activeRejectionFloor: Int = 0,
    val verificationOutcomeHeadline: String? = null,
    val verificationOutcomeDetail: String? = null,
    val verificationOutcomeStatus: UiStatus? = null
)

/**
 * Controller orchestrating Phone A verification runtime:
 * - Canonical Guardian lookup via [PlatformApiClient.getTrustedContacts]
 * - Guardian selection management (never defaulting blindly when multiple exist)
 * - Verification request creation via [PlatformApiClient.createVerification]
 * - Realtime session observation via [VerificationRepository.observeVerification]
 * - Authoritative expiry refresh via [PlatformApiClient.getVerification]
 * - Platform -> Intelligence signal adaptation via [VerificationSignalAdapter]
 * - Active rejection risk floor management (score >= 65, HIGH level persistence)
 * - Lifecycle cleanup
 */
class PhoneAVerificationController(
    private val apiClient: PlatformApiClient = PlatformApiClient(),
    private val repository: VerificationRepository = SupabaseVerificationRepository(),
    private val riskEngine: RiskEngine = RiskEngine(),
    private val coroutineScope: CoroutineScope,
    private val getTrustedContactsOverride: (suspend () -> Result<List<TrustedContactRelationship>>)? = null,
    private val createVerificationOverride: (suspend (String, String, String, String, Int) -> Result<VerificationSession>)? = null,
    private val getVerificationOverride: (suspend (String) -> Result<VerificationSession>)? = null,
    private val observeVerificationOverride: ((String) -> kotlinx.coroutines.flow.Flow<VerificationSession?>)? = null
) {
    private val _state = MutableStateFlow(PhoneAVerificationState())
    val state: StateFlow<PhoneAVerificationState> = _state.asStateFlow()

    private var observationJob: Job? = null
    private var countdownJob: Job? = null

    /**
     * Authoritatively loads canonical, enabled trusted contact relationships.
     */
    fun loadCanonicalGuardians() {
        coroutineScope.launch {
            _state.update { it.copy(isLookupLoading = true) }
            val result = getTrustedContactsOverride?.invoke() ?: apiClient.getTrustedContacts()
            if (result.isSuccess) {
                val contacts = result.getOrThrow().filter { it.enabled }
                when {
                    contacts.isEmpty() -> {
                        _state.update {
                            it.copy(
                                isLookupLoading = false,
                                guardians = emptyList(),
                                selectedGuardian = null,
                                isIdentityVerificationAvailable = false,
                                unavailableReason = "No connected Guardian is available for identity verification.",
                                requesterUiModel = it.requesterUiModel.copy(
                                    isPlatformIntegrated = false
                                )
                            )
                        }
                    }
                    contacts.size == 1 -> {
                        val single = contacts.first()
                        _state.update {
                            it.copy(
                                isLookupLoading = false,
                                guardians = contacts,
                                selectedGuardian = single,
                                isIdentityVerificationAvailable = true,
                                unavailableReason = null,
                                requesterUiModel = it.requesterUiModel.copy(
                                    guardianName = single.displayName,
                                    guardianNumber = single.relationship,
                                    isPlatformIntegrated = true
                                )
                            )
                        }
                    }
                    else -> {
                        _state.update {
                            it.copy(
                                isLookupLoading = false,
                                guardians = contacts,
                                selectedGuardian = null, // Requires explicit user selection
                                isIdentityVerificationAvailable = true,
                                unavailableReason = null,
                                requesterUiModel = it.requesterUiModel.copy(
                                    isPlatformIntegrated = true
                                )
                            )
                        }
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        isLookupLoading = false,
                        guardians = emptyList(),
                        selectedGuardian = null,
                        isIdentityVerificationAvailable = false,
                        unavailableReason = "Identity verification service is temporarily unavailable.",
                        requesterUiModel = it.requesterUiModel.copy(
                            isPlatformIntegrated = false
                        )
                    )
                }
            }
        }
    }

    /**
     * Explicitly selects a Guardian from multiple canonical contacts.
     */
    fun selectGuardian(guardian: TrustedContactRelationship) {
        _state.update {
            it.copy(
                selectedGuardian = guardian,
                requesterUiModel = it.requesterUiModel.copy(
                    guardianName = guardian.displayName,
                    guardianNumber = guardian.relationship,
                    isPlatformIntegrated = true
                )
            )
        }
    }

    /**
     * Updates caller identity information for the requester screen.
     */
    fun updateCallDetails(callerName: String, callerNumber: String) {
        _state.update {
            it.copy(
                requesterUiModel = it.requesterUiModel.copy(
                    callerName = callerName.ifBlank { "Current Call" },
                    callerNumber = callerNumber
                )
            )
        }
    }

    /**
     * Creates an authoritative VerificationSession through the backend API.
     */
    fun startVerificationRequest(
        currentRiskScore: Int,
        detectedAction: String? = null,
        transcriptSummary: String? = null
    ) {
        val currentState = _state.value
        if (currentState.isCreationInFlight) {
            return
        }

        // Active PENDING session reuse: do not create another session if already pending
        if (currentState.activeSession?.status == DomainStatus.PENDING) {
            _state.update {
                it.copy(
                    requesterUiModel = it.requesterUiModel.copy(
                        status = UiStatus.PENDING
                    )
                )
            }
            return
        }

        val guardian = currentState.selectedGuardian
            ?: if (currentState.guardians.size == 1) currentState.guardians.first() else null

        if (guardian == null) {
            _state.update {
                it.copy(
                    requesterUiModel = it.requesterUiModel.copy(
                        status = UiStatus.UNAVAILABLE,
                        details = "Please select a Guardian before sending verification."
                    )
                )
            }
            return
        }

        // Derive truthful, concise action and summary
        val requestedAction = if (!detectedAction.isNullOrBlank() && !detectedAction.equals("none", ignoreCase = true)) {
            detectedAction.trim().take(60)
        } else {
            "Confirm identity during active call"
        }

        val requestSummary = if (!transcriptSummary.isNullOrBlank()) {
            transcriptSummary.trim().take(120)
        } else {
            "Identity check requested during the current call."
        }

        coroutineScope.launch {
            _state.update { it.copy(isCreationInFlight = true) }
            val result = createVerificationOverride?.invoke(
                guardian.trustedUserId,
                guardian.displayName,
                requestedAction,
                requestSummary,
                currentRiskScore
            ) ?: apiClient.createVerification(
                trustedUserId = guardian.trustedUserId,
                claimedIdentity = guardian.displayName,
                requestedAction = requestedAction,
                requestSummary = requestSummary,
                riskScoreAtCreation = currentRiskScore
            )

            if (result.isSuccess) {
                val session = result.getOrThrow()
                if (session.status == DomainStatus.PENDING) {
                    val remaining = ((session.expiresAt - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                    _state.update {
                        it.copy(
                            isCreationInFlight = false,
                            activeSession = session,
                            selectedGuardian = guardian,
                            requesterUiModel = it.requesterUiModel.copy(
                                status = UiStatus.PENDING,
                                remainingSeconds = if (remaining > 0) remaining else 45,
                                isPlatformIntegrated = true
                            )
                        )
                    }
                    startCountdown(session)
                    startRealtimeObservation(session.id)
                } else {
                    _state.update {
                        it.copy(
                            isCreationInFlight = false,
                            requesterUiModel = it.requesterUiModel.copy(
                                status = UiStatus.UNAVAILABLE,
                                details = "Unexpected session status from backend."
                            )
                        )
                    }
                }
            } else {
                val err = result.exceptionOrNull()
                _state.update {
                    it.copy(
                        isCreationInFlight = false,
                        requesterUiModel = it.requesterUiModel.copy(
                            status = UiStatus.UNAVAILABLE,
                            details = err?.message ?: "Unable to connect to verification service."
                        )
                    )
                }
            }
        }
    }

    /**
     * Starts realtime observation of the canonical session.
     */
    fun startRealtimeObservation(sessionId: String) {
        observationJob?.cancel()
        val flow = observeVerificationOverride?.invoke(sessionId) ?: repository.observeVerification(sessionId)
        observationJob = coroutineScope.launch {
            flow.collect { session ->
                if (session != null) {
                    handleCanonicalSessionUpdate(session)
                }
            }
        }
    }

    /**
     * Handles canonical state transitions from realtime observation or authoritative fetch.
     */
    fun handleCanonicalSessionUpdate(session: VerificationSession) {
        _state.update { it.copy(activeSession = session) }
        val guardianName = _state.value.selectedGuardian?.displayName ?: "Guardian"

        when (session.status) {
            DomainStatus.PENDING -> {
                val remaining = ((session.expiresAt - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                _state.update {
                    it.copy(
                        requesterUiModel = it.requesterUiModel.copy(
                            status = UiStatus.PENDING,
                            remainingSeconds = remaining
                        )
                    )
                }
            }

            DomainStatus.VERIFIED -> {
                countdownJob?.cancel()
                val signal = VerificationSignalAdapter.mapToScamSignal(DomainStatus.VERIFIED)
                if (signal != null) {
                    riskEngine.evaluate(listOf(signal))
                }

                _state.update {
                    it.copy(
                        requesterUiModel = it.requesterUiModel.copy(
                            status = UiStatus.VERIFIED
                        ),
                        verificationOutcomeHeadline = "$guardianName confirmed this request",
                        verificationOutcomeDetail = "Identity attribution was confirmed. Stay cautious with financial or sensitive requests.",
                        verificationOutcomeStatus = UiStatus.VERIFIED
                    )
                }
            }

            DomainStatus.REJECTED -> {
                countdownJob?.cancel()
                val signal = VerificationSignalAdapter.mapToScamSignal(DomainStatus.REJECTED)
                val assessment = if (signal != null) riskEngine.evaluate(listOf(signal)) else null
                val floorScore = assessment?.score ?: 65

                _state.update {
                    it.copy(
                        activeRejection = true,
                        activeRejectionFloor = floorScore,
                        requesterUiModel = it.requesterUiModel.copy(
                            status = UiStatus.REJECTED
                        ),
                        verificationOutcomeHeadline = "$guardianName says this request is not from them",
                        verificationOutcomeDetail = "Caller identity verification failed. Do not send money or share credentials. End the call now.",
                        verificationOutcomeStatus = UiStatus.REJECTED
                    )
                }
            }

            DomainStatus.EXPIRED -> {
                countdownJob?.cancel()
                _state.update {
                    it.copy(
                        requesterUiModel = it.requesterUiModel.copy(
                            status = UiStatus.EXPIRED
                        ),
                        verificationOutcomeHeadline = "Identity check expired",
                        verificationOutcomeDetail = "Guardian did not respond before timeout. Proceed with caution.",
                        verificationOutcomeStatus = UiStatus.EXPIRED
                    )
                }
            }

            DomainStatus.UNAVAILABLE -> {
                countdownJob?.cancel()
                _state.update {
                    it.copy(
                        requesterUiModel = it.requesterUiModel.copy(
                            status = UiStatus.UNAVAILABLE,
                            details = "Trusted contact is currently unreachable or delivery failed."
                        ),
                        verificationOutcomeHeadline = "Verification unavailable",
                        verificationOutcomeDetail = "Trusted contact unreachable. Proceed with caution.",
                        verificationOutcomeStatus = UiStatus.UNAVAILABLE
                    )
                }
            }
        }
    }

    /**
     * Countdown timer that updates remaining seconds on PENDING sessions.
     * Reaching zero MUST NOT locally mutate to EXPIRED or REJECTED.
     * Instead, requests authoritative refresh from backend.
     */
    private fun startCountdown(session: VerificationSession) {
        countdownJob?.cancel()
        countdownJob = coroutineScope.launch {
            while (isActive) {
                val remaining = ((session.expiresAt - System.currentTimeMillis()) / 1000).toInt()
                if (remaining <= 0) {
                    _state.update {
                        it.copy(
                            requesterUiModel = it.requesterUiModel.copy(remainingSeconds = 0)
                        )
                    }
                    // Authoritative refresh at expiry deadline
                    refreshAuthoritativeSession(session.id)
                    break
                } else {
                    _state.update {
                        it.copy(
                            requesterUiModel = it.requesterUiModel.copy(remainingSeconds = remaining)
                        )
                    }
                    delay(1000)
                }
            }
        }
    }

    /**
     * Authoritatively refreshes session snapshot from backend GET /api/v1/verifications/:sessionId.
     */
    fun refreshAuthoritativeSession(sessionId: String) {
        coroutineScope.launch {
            val result = getVerificationOverride?.invoke(sessionId) ?: apiClient.getVerification(sessionId)
            if (result.isSuccess) {
                handleCanonicalSessionUpdate(result.getOrThrow())
            }
        }
    }

    /**
     * Computes the effective runtime risk score taking into account active human rejection floor.
     * Once REJECTED, subsequent lower transcript reports MUST NOT erase the HIGH risk floor (score >= 65).
     */
    fun calculateEffectiveRiskScore(legacyRiskScore: Int): Int {
        val currentState = _state.value
        return if (currentState.activeRejection) {
            maxOf(legacyRiskScore, currentState.activeRejectionFloor.coerceAtLeast(65))
        } else {
            legacyRiskScore
        }
    }

    /**
     * Cancels active coroutine jobs on activity teardown without modifying backend session state.
     */
    fun cleanup() {
        observationJob?.cancel()
        countdownJob?.cancel()
    }
}
