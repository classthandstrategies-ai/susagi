package com.guardian.app.verification

import com.guardian.app.domain.risk.ScamSignal
import com.guardian.app.domain.risk.SignalContext
import com.guardian.app.domain.risk.SignalSource
import com.guardian.app.domain.risk.SignalType
import com.guardian.app.verification.VerificationStatus as DomainStatus
import com.guardian.app.ui.guardians.VerificationStatus as UiStatus

/**
 * Authoritative adapter mapping canonical verification lifecycle states into
 * domain [ScamSignal] instances for evaluation by [com.guardian.app.domain.risk.RiskEngine].
 *
 * Mapping Contract (per CP3 specification):
 * - PENDING     -> null (no signal)
 * - VERIFIED    -> ScamSignal(TRUSTED_CONTACT_VERIFICATION_CONFIRMED, HUMAN_VERIFICATION, UNKNOWN)
 * - REJECTED    -> ScamSignal(TRUSTED_CONTACT_VERIFICATION_REJECTED, HUMAN_VERIFICATION, UNKNOWN)
 * - EXPIRED     -> null (no rejection signal)
 * - UNAVAILABLE -> null (no rejection signal)
 * - READY/null  -> null
 */
object VerificationSignalAdapter {

    fun mapToScamSignal(
        status: DomainStatus?,
        rawEvidence: String = ""
    ): ScamSignal? {
        return when (status) {
            DomainStatus.VERIFIED -> ScamSignal(
                type = SignalType.TRUSTED_CONTACT_VERIFICATION_CONFIRMED,
                source = SignalSource.HUMAN_VERIFICATION,
                context = SignalContext.UNKNOWN,
                rawEvidence = rawEvidence.ifBlank { "Trusted contact verified caller identity." }
            )
            DomainStatus.REJECTED -> ScamSignal(
                type = SignalType.TRUSTED_CONTACT_VERIFICATION_REJECTED,
                source = SignalSource.HUMAN_VERIFICATION,
                context = SignalContext.UNKNOWN,
                rawEvidence = rawEvidence.ifBlank { "Trusted contact rejected caller identity. Caller is not recognized." }
            )
            DomainStatus.PENDING,
            DomainStatus.EXPIRED,
            DomainStatus.UNAVAILABLE,
            null -> null
        }
    }

    fun mapToScamSignal(
        status: UiStatus?,
        rawEvidence: String = ""
    ): ScamSignal? {
        return when (status) {
            UiStatus.VERIFIED -> ScamSignal(
                type = SignalType.TRUSTED_CONTACT_VERIFICATION_CONFIRMED,
                source = SignalSource.HUMAN_VERIFICATION,
                context = SignalContext.UNKNOWN,
                rawEvidence = rawEvidence.ifBlank { "Trusted contact verified caller identity." }
            )
            UiStatus.REJECTED -> ScamSignal(
                type = SignalType.TRUSTED_CONTACT_VERIFICATION_REJECTED,
                source = SignalSource.HUMAN_VERIFICATION,
                context = SignalContext.UNKNOWN,
                rawEvidence = rawEvidence.ifBlank { "Trusted contact rejected caller identity. Caller is not recognized." }
            )
            UiStatus.PENDING,
            UiStatus.EXPIRED,
            UiStatus.UNAVAILABLE,
            UiStatus.READY,
            null -> null
        }
    }
}
