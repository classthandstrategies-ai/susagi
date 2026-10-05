package com.guardian.app.ui.guardians

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardian.app.network.PlatformApiClient
import com.guardian.app.verification.VerificationSession
import com.guardian.app.verification.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Controller/ViewModel managing Phone B identity verification state.
 *
 * Responsibilities:
 * - Fetches canonical VerificationSession before rendering details.
 * - Manages calm loading, loaded, submitting, error, and terminal states.
 * - Submits user decision (VERIFIED or REJECTED) with device ID from [deviceIdProvider].
 * - Enforces single in-flight submission guard (disables buttons immediately).
 * - Enforces first-terminal-state-wins by rendering the API-returned canonical session.
 * - Maps low-level exceptions to user-facing safety messages without exposing raw internals.
 */
class VerificationResponseViewModel(
    private val apiClient: PlatformApiClient = PlatformApiClient(),
    private val deviceIdProvider: () -> String,
    private val getVerificationOverride: (suspend (String) -> Result<VerificationSession>)? = null,
    private val respondToVerificationOverride: (suspend (String, VerificationStatus, String) -> Result<VerificationSession>)? = null,
    private val customScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private val scope: kotlinx.coroutines.CoroutineScope
        get() = customScope ?: viewModelScope

    private val _uiState = MutableStateFlow<VerificationResponseUiState>(VerificationResponseUiState.Loading)
    val uiState: StateFlow<VerificationResponseUiState> = _uiState.asStateFlow()

    private var currentSessionId: String? = null

    fun loadSession(sessionId: String) {
        currentSessionId = sessionId
        _uiState.value = VerificationResponseUiState.Loading

        scope.launch {
            val result = getVerificationOverride?.invoke(sessionId) ?: apiClient.getVerification(sessionId)
            result.fold(
                onSuccess = { session ->
                    _uiState.value = VerificationResponseUiState.Loaded(session)
                },
                onFailure = { error ->
                    val userMessage = mapErrorMessage(error)
                    _uiState.value = VerificationResponseUiState.Error(
                        userFacingMessage = userMessage,
                        retryAllowed = true,
                        sessionId = sessionId
                    )
                }
            )
        }
    }

    fun retry() {
        val current = _uiState.value
        when (current) {
            is VerificationResponseUiState.Error -> {
                loadSession(current.sessionId)
            }
            else -> {
                currentSessionId?.let { loadSession(it) }
            }
        }
    }

    fun respond(response: VerificationStatus) {
        // Enforce valid user responses: only VERIFIED or REJECTED
        if (response != VerificationStatus.VERIFIED && response != VerificationStatus.REJECTED) {
            return
        }

        val currentState = _uiState.value
        val session = when (currentState) {
            is VerificationResponseUiState.Loaded -> currentState.session
            else -> return // In-flight, loading, or error: block duplicate submissions
        }

        // Terminal or expired sessions cannot receive new responses
        if (session.isTerminal() || session.isExpired()) {
            return
        }

        // Immediately enter Submitting state to disable buttons and prevent double-taps
        _uiState.value = VerificationResponseUiState.Submitting(
            session = session,
            selectedResponse = response
        )

        scope.launch {
            val deviceId = try {
                deviceIdProvider()
            } catch (e: Exception) {
                _uiState.value = VerificationResponseUiState.Error(
                    userFacingMessage = "Unable to verify device identity. Please try again.",
                    retryAllowed = true,
                    sessionId = session.id
                )
                return@launch
            }

            val result = respondToVerificationOverride?.invoke(session.id, response, deviceId)
                ?: apiClient.respondToVerification(
                    sessionId = session.id,
                    response = response,
                    deviceId = deviceId
                )

            result.fold(
                onSuccess = { returnedCanonicalSession ->
                    // First-terminal-state-wins: always render canonical session returned by backend
                    _uiState.value = VerificationResponseUiState.Loaded(returnedCanonicalSession)
                },
                onFailure = { error ->
                    val userMessage = mapErrorMessage(error)
                    _uiState.value = VerificationResponseUiState.Error(
                        userFacingMessage = userMessage,
                        retryAllowed = true,
                        sessionId = session.id
                    )
                }
            )
        }
    }

    companion object {
        fun mapErrorMessage(throwable: Throwable?): String {
            val msg = throwable?.message.orEmpty()
            return when {
                msg.contains("401") || msg.contains("Authentication failed") || msg.contains("unauthenticated", ignoreCase = true) ->
                    "Your SuSagi session isn't available right now."
                msg.contains("403") || msg.contains("not authorized", ignoreCase = true) || msg.contains("forbidden", ignoreCase = true) ->
                    "You aren't authorized to respond to this identity check."
                msg.contains("404") || msg.contains("not found", ignoreCase = true) || msg.contains("no longer available", ignoreCase = true) ->
                    "This identity check is no longer available."
                throwable is UnknownHostException ||
                throwable is SocketTimeoutException ||
                throwable is ConnectException ||
                msg.contains("connection", ignoreCase = true) ||
                msg.contains("timeout", ignoreCase = true) ->
                    "We couldn't load this identity check. Check your connection and try again."
                else ->
                    "We couldn't load this identity check. Check your connection and try again."
            }
        }
    }
}
