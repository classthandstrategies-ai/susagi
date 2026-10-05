package com.guardian.app.ui.guardians

import com.guardian.app.verification.VerificationSession
import com.guardian.app.verification.VerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException

class VerificationResponseFlowTest {

    private val testDeviceId = "hw_pixel_9_test_device"

    private fun samplePendingSession(id: String = "sess_test_100"): VerificationSession {
        return VerificationSession(
            id = id,
            protectedUserId = "user_victim_1",
            trustedUserId = "user_guardian_2",
            claimedIdentity = "State Bank Fraud Dept",
            requestedAction = "₹50,000 Safety Transfer",
            requestSummary = "Caller claimed immediate transfer is required.",
            status = VerificationStatus.PENDING,
            createdAt = 1000L,
            expiresAt = System.currentTimeMillis() + 60000L
        )
    }

    @Test
    fun testInitialState_isLoading() {
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )
        assertTrue(viewModel.uiState.value is VerificationResponseUiState.Loading)
    }

    @Test
    fun testLoadSession_success_updatesToLoaded() = runBlocking {
        val expectedSession = samplePendingSession("sess_load_success")
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = { sessionId ->
                assertEquals("sess_load_success", sessionId)
                Result.success(expectedSession)
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_load_success")

        val state = viewModel.uiState.value
        assertTrue(state is VerificationResponseUiState.Loaded)
        val loaded = state as VerificationResponseUiState.Loaded
        assertEquals("sess_load_success", loaded.session.id)
        assertEquals("State Bank Fraud Dept", loaded.session.claimedIdentity)
        assertEquals(VerificationStatus.PENDING, loaded.session.status)
    }

    @Test
    fun testLoadSession_401Error_mapsToSessionUnavailable() = runBlocking {
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = {
                Result.failure(IOException("HTTP 401: Unauthorized session"))
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_401")

        val state = viewModel.uiState.value
        assertTrue(state is VerificationResponseUiState.Error)
        val error = state as VerificationResponseUiState.Error
        assertEquals("Your SuSagi session isn't available right now.", error.userFacingMessage)
        assertTrue(error.retryAllowed)
    }

    @Test
    fun testLoadSession_403Error_mapsToNotAuthorized() = runBlocking {
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = {
                Result.failure(IOException("HTTP 403: Forbidden - not a designated guardian"))
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_403")

        val state = viewModel.uiState.value
        assertTrue(state is VerificationResponseUiState.Error)
        val error = state as VerificationResponseUiState.Error
        assertEquals("You aren't authorized to respond to this identity check.", error.userFacingMessage)
    }

    @Test
    fun testLoadSession_404Error_mapsToNoLongerAvailable() = runBlocking {
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = {
                Result.failure(IOException("HTTP 404: Not Found"))
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_404")

        val state = viewModel.uiState.value
        assertTrue(state is VerificationResponseUiState.Error)
        val error = state as VerificationResponseUiState.Error
        assertEquals("This identity check is no longer available.", error.userFacingMessage)
    }

    @Test
    fun testLoadSession_networkError_mapsToConnectionMessage() = runBlocking {
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = {
                Result.failure(UnknownHostException("Failed to resolve host"))
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_net")

        val state = viewModel.uiState.value
        assertTrue(state is VerificationResponseUiState.Error)
        val error = state as VerificationResponseUiState.Error
        assertEquals("We couldn't load this identity check. Check your connection and try again.", error.userFacingMessage)
    }

    @Test
    fun testRetry_reloadsSession() = runBlocking {
        var callCount = 0
        val session = samplePendingSession("sess_retry")

        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = {
                callCount++
                if (callCount == 1) {
                    Result.failure(IOException("Temporary failure"))
                } else {
                    Result.success(session)
                }
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_retry")
        assertTrue(viewModel.uiState.value is VerificationResponseUiState.Error)

        viewModel.retry()
        assertEquals(2, callCount)
        assertTrue(viewModel.uiState.value is VerificationResponseUiState.Loaded)
    }

    @Test
    fun testRespondVerified_submitsVerifiedWithDeviceId_andRendersCanonicalResult() = runBlocking {
        val initialSession = samplePendingSession("sess_verified_test")
        val returnedSession = initialSession.copy(
            status = VerificationStatus.VERIFIED,
            respondedAt = 2000L,
            responseDeviceId = testDeviceId
        )

        var submittedSessionId = ""
        var submittedStatus: VerificationStatus? = null
        var submittedDeviceId = ""

        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = { Result.success(initialSession) },
            respondToVerificationOverride = { id, status, devId ->
                submittedSessionId = id
                submittedStatus = status
                submittedDeviceId = devId
                Result.success(returnedSession)
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_verified_test")
        assertTrue(viewModel.uiState.value is VerificationResponseUiState.Loaded)

        viewModel.respond(VerificationStatus.VERIFIED)

        assertEquals("sess_verified_test", submittedSessionId)
        assertEquals(VerificationStatus.VERIFIED, submittedStatus)
        assertEquals(testDeviceId, submittedDeviceId)

        val finalState = viewModel.uiState.value
        assertTrue(finalState is VerificationResponseUiState.Loaded)
        val finalSession = (finalState as VerificationResponseUiState.Loaded).session
        assertEquals(VerificationStatus.VERIFIED, finalSession.status)
        assertTrue(finalSession.isTerminal())
    }

    @Test
    fun testRespondRejected_submitsRejectedWithDeviceId_andRendersCanonicalResult() = runBlocking {
        val initialSession = samplePendingSession("sess_rejected_test")
        val returnedSession = initialSession.copy(
            status = VerificationStatus.REJECTED,
            respondedAt = 2000L,
            responseDeviceId = testDeviceId
        )

        var submittedStatus: VerificationStatus? = null

        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = { Result.success(initialSession) },
            respondToVerificationOverride = { _, status, _ ->
                submittedStatus = status
                Result.success(returnedSession)
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_rejected_test")
        viewModel.respond(VerificationStatus.REJECTED)

        assertEquals(VerificationStatus.REJECTED, submittedStatus)

        val finalState = viewModel.uiState.value
        assertTrue(finalState is VerificationResponseUiState.Loaded)
        val finalSession = (finalState as VerificationResponseUiState.Loaded).session
        assertEquals(VerificationStatus.REJECTED, finalSession.status)
        assertTrue(finalSession.isTerminal())
    }

    @Test
    fun testFirstTerminalStateWins_doesNotOverrideServerReturnedCanonicalState() = runBlocking {
        // Race condition: Phone B user taps VERIFIED, but server backend returns REJECTED
        // (e.g. timeout or another guardian already responded REJECTED).
        // The UI MUST render the API-returned canonical session, NEVER forcing the local choice.
        val initialSession = samplePendingSession("sess_race")
        val serverReturnedTerminalSession = initialSession.copy(
            status = VerificationStatus.REJECTED,
            respondedAt = 1999L,
            responseDeviceId = "other_guardian_device"
        )

        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = { Result.success(initialSession) },
            respondToVerificationOverride = { _, _, _ ->
                // Server returns REJECTED
                Result.success(serverReturnedTerminalSession)
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_race")
        // User attempts to respond VERIFIED
        viewModel.respond(VerificationStatus.VERIFIED)

        val finalState = viewModel.uiState.value
        assertTrue(finalState is VerificationResponseUiState.Loaded)
        val finalSession = (finalState as VerificationResponseUiState.Loaded).session
        // Server's canonical state MUST prevail
        assertEquals(VerificationStatus.REJECTED, finalSession.status)
    }

    @Test
    fun testTerminalState_blocksSubsequentSubmissions() = runBlocking {
        val alreadyVerifiedSession = samplePendingSession("sess_already_done").copy(
            status = VerificationStatus.VERIFIED,
            respondedAt = 1500L
        )

        var submitAttempted = false
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = { Result.success(alreadyVerifiedSession) },
            respondToVerificationOverride = { _, _, _ ->
                submitAttempted = true
                Result.success(alreadyVerifiedSession)
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_already_done")
        viewModel.respond(VerificationStatus.REJECTED)

        assertFalse("Submissions must be blocked when session is already terminal", submitAttempted)
    }

    @Test
    fun testExpiredSession_blocksSubmissions() = runBlocking {
        val expiredSession = samplePendingSession("sess_expired").copy(
            status = VerificationStatus.PENDING,
            expiresAt = System.currentTimeMillis() - 10000L // Elapsed
        )

        var submitAttempted = false
        val viewModel = VerificationResponseViewModel(
            deviceIdProvider = { testDeviceId },
            getVerificationOverride = { Result.success(expiredSession) },
            respondToVerificationOverride = { _, _, _ ->
                submitAttempted = true
                Result.success(expiredSession)
            },
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )

        viewModel.loadSession("sess_expired")
        viewModel.respond(VerificationStatus.VERIFIED)

        assertFalse("Submissions must be blocked when session is expired", submitAttempted)
    }
}
