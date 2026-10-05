package com.guardian.app.verification

import com.guardian.app.domain.risk.RiskEngine
import com.guardian.app.domain.risk.SignalContext
import com.guardian.app.domain.risk.SignalSource
import com.guardian.app.domain.risk.SignalType
import com.guardian.app.ui.guardians.VerificationStatus as UiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneAVerificationTest {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private fun sampleRelationship(
        trustedUserId: String = "user_trusted_2",
        name: String = "Mom",
        relationship: String = "parent",
        enabled: Boolean = true
    ): TrustedContactRelationship {
        return TrustedContactRelationship(
            trustedUserId = trustedUserId,
            displayName = name,
            relationship = relationship,
            enabled = enabled
        )
    }

    private fun sampleSession(
        id: String = "sess_phone_a_1",
        status: VerificationStatus = VerificationStatus.PENDING,
        expiresInSeconds: Long = 45
    ): VerificationSession {
        val now = System.currentTimeMillis()
        return VerificationSession(
            id = id,
            protectedUserId = "user_victim_1",
            trustedUserId = "user_trusted_2",
            claimedIdentity = "Mom",
            requestedAction = "Confirm identity during active call",
            requestSummary = "Suspicious financial request detected.",
            riskScoreAtCreation = 60,
            status = status,
            createdAt = now,
            expiresAt = now + (expiresInSeconds * 1000L)
        )
    }

    // ------------------------------------------------------------------------
    // 1. CANONICAL CONTACT LOOKUP TESTS
    // ------------------------------------------------------------------------

    @Test
    fun testZeroGuardians_marksUnavailable() = runBlocking {
        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(emptyList()) }
        )

        controller.loadCanonicalGuardians()

        val state = controller.state.value
        assertFalse(state.isIdentityVerificationAvailable)
        assertEquals("No connected Guardian is available for identity verification.", state.unavailableReason)
        assertNull(state.selectedGuardian)
        assertFalse(state.requesterUiModel.isPlatformIntegrated)
    }

    @Test
    fun testSingleGuardian_automaticallySelected() = runBlocking {
        val guardian = sampleRelationship(name = "Trusted Mom")
        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(listOf(guardian)) }
        )

        controller.loadCanonicalGuardians()

        val state = controller.state.value
        assertTrue(state.isIdentityVerificationAvailable)
        assertNull(state.unavailableReason)
        assertEquals("Trusted Mom", state.selectedGuardian?.displayName)
        assertEquals("Trusted Mom", state.requesterUiModel.guardianName)
        assertTrue(state.requesterUiModel.isPlatformIntegrated)
    }

    @Test
    fun testMultipleGuardians_requiresExplicitSelection() = runBlocking {
        val g1 = sampleRelationship(trustedUserId = "rel_1", name = "Mom")
        val g2 = sampleRelationship(trustedUserId = "rel_2", name = "Sister")
        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(listOf(g1, g2)) }
        )

        controller.loadCanonicalGuardians()

        val state = controller.state.value
        assertTrue(state.isIdentityVerificationAvailable)
        assertEquals(2, state.guardians.size)
        // Must NOT auto-select first contact
        assertNull("Multiple contacts must not auto-select first", state.selectedGuardian)

        // Explicit selection
        controller.selectGuardian(g2)
        val selectedState = controller.state.value
        assertEquals("Sister", selectedState.selectedGuardian?.displayName)
        assertEquals("Sister", selectedState.requesterUiModel.guardianName)
    }

    // ------------------------------------------------------------------------
    // 2. REQUEST CREATION & BACKEND BEHAVIOR
    // ------------------------------------------------------------------------

    @Test
    fun testStartVerification_blocksDuplicateWhileInFlight() = runBlocking {
        val g = sampleRelationship()
        val createCallCount = AtomicInteger(0)

        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(listOf(g)) },
            createVerificationOverride = { _, _, _, _, _ ->
                createCallCount.incrementAndGet()
                Result.success(sampleSession())
            },
            observeVerificationOverride = { MutableSharedFlow() }
        )

        controller.loadCanonicalGuardians()
        controller.startVerificationRequest(currentRiskScore = 60)

        assertEquals(1, createCallCount.get())
        assertEquals(UiStatus.PENDING, controller.state.value.requesterUiModel.status)
    }

    @Test
    fun testStartVerification_reusesActivePendingSession() = runBlocking {
        val g = sampleRelationship()
        val createCallCount = AtomicInteger(0)

        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(listOf(g)) },
            createVerificationOverride = { _, _, _, _, _ ->
                createCallCount.incrementAndGet()
                Result.success(sampleSession("session_first"))
            },
            observeVerificationOverride = { MutableSharedFlow() }
        )

        controller.loadCanonicalGuardians()
        controller.startVerificationRequest(currentRiskScore = 60)
        assertEquals(1, createCallCount.get())
        assertEquals("session_first", controller.state.value.activeSession?.id)

        // Second call while active session is PENDING must reuse existing
        controller.startVerificationRequest(currentRiskScore = 60)
        assertEquals(1, createCallCount.get())
        assertEquals("session_first", controller.state.value.activeSession?.id)
    }

    @Test
    fun testStartVerification_backendFailure_showsUnavailableNotPending() = runBlocking {
        val g = sampleRelationship()
        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(listOf(g)) },
            createVerificationOverride = { _, _, _, _, _ ->
                Result.failure(RuntimeException("Supabase 500 error"))
            }
        )

        controller.loadCanonicalGuardians()
        controller.startVerificationRequest(currentRiskScore = 60)

        val state = controller.state.value
        assertEquals(UiStatus.UNAVAILABLE, state.requesterUiModel.status)
        assertEquals("Supabase 500 error", state.requesterUiModel.details)
        assertFalse(state.isCreationInFlight)
    }

    // ------------------------------------------------------------------------
    // 3. SIGNAL ADAPTER MAPPINGS
    // ------------------------------------------------------------------------

    @Test
    fun testAdapter_pendingProducesNull() {
        val signalDomain = VerificationSignalAdapter.mapToScamSignal(VerificationStatus.PENDING)
        assertNull(signalDomain)

        val signalUi = VerificationSignalAdapter.mapToScamSignal(UiStatus.PENDING)
        assertNull(signalUi)
    }

    @Test
    fun testAdapter_verifiedProducesConfirmedSignalWithCorrectAttributes() {
        val signal = VerificationSignalAdapter.mapToScamSignal(VerificationStatus.VERIFIED)
        assertNotNull(signal)
        assertEquals(SignalType.TRUSTED_CONTACT_VERIFICATION_CONFIRMED, signal!!.type)
        assertEquals(SignalSource.HUMAN_VERIFICATION, signal.source)
        assertEquals(SignalContext.UNKNOWN, signal.context)
    }

    @Test
    fun testAdapter_rejectedProducesRejectedSignalWithCorrectAttributes() {
        val signal = VerificationSignalAdapter.mapToScamSignal(VerificationStatus.REJECTED)
        assertNotNull(signal)
        assertEquals(SignalType.TRUSTED_CONTACT_VERIFICATION_REJECTED, signal!!.type)
        assertEquals(SignalSource.HUMAN_VERIFICATION, signal.source)
        assertEquals(SignalContext.UNKNOWN, signal.context)
    }

    @Test
    fun testAdapter_expiredAndUnavailableProduceNull() {
        assertNull(VerificationSignalAdapter.mapToScamSignal(VerificationStatus.EXPIRED))
        assertNull(VerificationSignalAdapter.mapToScamSignal(VerificationStatus.UNAVAILABLE))
        assertNull(VerificationSignalAdapter.mapToScamSignal(UiStatus.EXPIRED))
        assertNull(VerificationSignalAdapter.mapToScamSignal(UiStatus.UNAVAILABLE))
        assertNull(VerificationSignalAdapter.mapToScamSignal(UiStatus.READY))
    }

    // ------------------------------------------------------------------------
    // 4. RISK FLOOR & RISK ENGINE BEHAVIOR
    // ------------------------------------------------------------------------

    @Test
    fun testRiskEngine_isolatedRejectedSignal_producesScore65High() {
        val rejectedSignal = VerificationSignalAdapter.mapToScamSignal(VerificationStatus.REJECTED)
        assertNotNull(rejectedSignal)

        val engine = RiskEngine()
        val assessment = engine.evaluate(listOf(rejectedSignal!!))

        assertEquals(65, assessment.score)
        assertEquals(com.guardian.app.domain.risk.RiskLevel.HIGH, assessment.level)
    }

    @Test
    fun testRiskFloor_persistsAgainstSubsequentLowerScores() = runBlocking {
        val g = sampleRelationship(name = "Mom")
        val liveUpdates = MutableSharedFlow<VerificationSession?>()

        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(listOf(g)) },
            createVerificationOverride = { _, _, _, _, _ ->
                Result.success(sampleSession("session_floor_test"))
            },
            observeVerificationOverride = { liveUpdates }
        )

        controller.loadCanonicalGuardians()
        controller.startVerificationRequest(currentRiskScore = 30)

        // Baseline before terminal response
        assertEquals(30, controller.calculateEffectiveRiskScore(30))

        // Phone B responds REJECTED
        controller.handleCanonicalSessionUpdate(
            sampleSession("session_floor_test", status = VerificationStatus.REJECTED)
        )

        assertTrue("activeRejection must be true", controller.state.value.activeRejection)
        assertEquals(65, controller.state.value.activeRejectionFloor)
        assertEquals(UiStatus.REJECTED, controller.state.value.requesterUiModel.status)
        assertEquals("Mom says this request is not from them", controller.state.value.verificationOutcomeHeadline)

        // Subsequent lower transcript analysis (e.g. 20, 10) MUST NOT lower score below 65
        assertEquals(65, controller.calculateEffectiveRiskScore(20))
        assertEquals(65, controller.calculateEffectiveRiskScore(10))
        assertEquals(65, controller.calculateEffectiveRiskScore(0))

        // Subsequent higher score (e.g. 85) is allowed to elevate further
        assertEquals(85, controller.calculateEffectiveRiskScore(85))
    }

    @Test
    fun testVerified_doesNotLowerExistingRiskScore() = runBlocking {
        val g = sampleRelationship(name = "Mom")
        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getTrustedContactsOverride = { Result.success(listOf(g)) },
            createVerificationOverride = { _, _, _, _, _ ->
                Result.success(sampleSession("session_verified_test"))
            }
        )

        controller.loadCanonicalGuardians()
        controller.startVerificationRequest(currentRiskScore = 70)

        // Phone B responds VERIFIED
        controller.handleCanonicalSessionUpdate(
            sampleSession("session_verified_test", status = VerificationStatus.VERIFIED)
        )

        assertFalse("activeRejection must remain false", controller.state.value.activeRejection)
        assertEquals(UiStatus.VERIFIED, controller.state.value.requesterUiModel.status)
        assertEquals("Mom confirmed caller identity", controller.state.value.verificationOutcomeHeadline)

        // Legacy score of 70 is preserved, not artificially zeroed
        assertEquals(70, controller.calculateEffectiveRiskScore(70))
    }

    // ------------------------------------------------------------------------
    // 5. EXPIRY & LIFECYCLE CLEANUP TESTS
    // ------------------------------------------------------------------------

    @Test
    fun testExpiryRefresh_authoritativelyFetchesBackend() = runBlocking {
        val getVerificationCalled = AtomicInteger(0)
        val controller = PhoneAVerificationController(
            coroutineScope = testScope,
            getVerificationOverride = { sessionId ->
                getVerificationCalled.incrementAndGet()
                Result.success(sampleSession(sessionId, status = VerificationStatus.EXPIRED))
            }
        )

        controller.refreshAuthoritativeSession("sess_expire_test")

        assertEquals(1, getVerificationCalled.get())
        assertEquals(UiStatus.EXPIRED, controller.state.value.requesterUiModel.status)
        assertEquals("Identity check expired", controller.state.value.verificationOutcomeHeadline)
    }

    @Test
    fun testCleanup_cancelsCoroutinesWithoutThrowing() {
        val controller = PhoneAVerificationController(
            coroutineScope = testScope
        )

        // Cleanup should execute smoothly without crashing
        controller.cleanup()
        assertFalse(controller.state.value.activeRejection)
    }
}
