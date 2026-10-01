package com.guardian.app.domain.risk

/**
 * Normalized semantic protective action intent emitted by the domain layer.
 *
 * Encapsulates security guidance intent independently of user-facing presentation or localization.
 */
enum class ProtectiveAction {
    CONTINUE_MONITORING,
    VERIFY_IDENTITY,
    END_CALL,
    DO_NOT_SHARE_CREDENTIALS,
    DO_NOT_SEND_MONEY,
    USE_OFFICIAL_CHANNEL,
    DO_NOT_INSTALL_REMOTE_ACCESS
}
