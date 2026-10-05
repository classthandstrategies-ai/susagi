package com.guardian.app.ui.guardians

import android.content.Context
import com.guardian.app.protect.advanced.TrustedContactManager

/**
 * Status of a trusted contact in the user's Guardian Circle.
 */
enum class GuardianStatus {
    /** Auto-alert enabled; emergency SMS dispatched on high scam risk (>75%). */
    ACTIVE,
    /** Configured in circle but emergency notifications paused by user. */
    PAUSED,
    /** Invitation or pairing pending (conceptual/preview state). */
    PENDING_SETUP
}

/**
 * UI representation of a Guardian / Trusted Contact.
 *
 * NOTE: Genuine runtime data currently stores exactly one contact in TrustedContactManager.
 */
data class GuardianUiModel(
    val id: String = "primary_contact",
    val name: String,
    val phone: String,
    val status: GuardianStatus,
    val isEnabled: Boolean,
    val relationship: String? = null
)

/**
 * State for the Guardian Circle destination.
 */
data class GuardianCircleUiState(
    val guardians: List<GuardianUiModel> = emptyList(),
    val isIdentityVerificationSupported: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasGuardians: Boolean
        get() = guardians.isNotEmpty()

    val activeGuardiansCount: Int
        get() = guardians.count { it.status == GuardianStatus.ACTIVE }

    companion object {
        /**
         * Loads genuine trusted contact from [TrustedContactManager].
         *
         * DOES NOT FABRICATE DATA:
         * Maps strictly to 0 or 1 contact based on SharedPreferences.
         * [isIdentityVerificationSupported] is false until Platform CP3 VerificationSession is implemented.
         */
        fun fromRuntime(context: Context): GuardianCircleUiState {
            val contact = TrustedContactManager.load(context)
            val guardians = if (contact != null && contact.phone.isNotBlank()) {
                listOf(
                    GuardianUiModel(
                        id = "primary_contact",
                        name = contact.name.ifBlank { "Trusted Contact" },
                        phone = contact.phone,
                        status = if (contact.enabled) GuardianStatus.ACTIVE else GuardianStatus.PAUSED,
                        isEnabled = contact.enabled,
                        relationship = "Primary Emergency Contact"
                    )
                )
            } else {
                emptyList()
            }

            return GuardianCircleUiState(
                guardians = guardians,
                isIdentityVerificationSupported = false
            )
        }
    }
}
