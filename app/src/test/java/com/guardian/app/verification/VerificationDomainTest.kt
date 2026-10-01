package com.guardian.app.verification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationDomainTest {

    @Test
    fun testVerificationStatusEnumValues() {
        val expectedStatuses = setOf("PENDING", "VERIFIED", "REJECTED", "EXPIRED", "UNAVAILABLE")
        val actualStatuses = VerificationStatus.values().map { it.name }.toSet()
        assertEquals("Canonical status enum must contain exactly the 5 specified values", expectedStatuses, actualStatuses)
        assertEquals(5, VerificationStatus.values().size)
    }

    @Test
    fun testVerificationStatusTerminalSemantics() {
        assertFalse("PENDING is non-terminal", VerificationStatus.PENDING.isTerminal)
        assertTrue("VERIFIED is terminal", VerificationStatus.VERIFIED.isTerminal)
        assertTrue("REJECTED is terminal", VerificationStatus.REJECTED.isTerminal)
        assertTrue("EXPIRED is terminal (timeout is not rejection)", VerificationStatus.EXPIRED.isTerminal)
        assertTrue("UNAVAILABLE is terminal (unavailable is not rejection)", VerificationStatus.UNAVAILABLE.isTerminal)
    }

    @Test
    fun testVerificationSessionCreationDefaults() {
        val session = VerificationSession(
            id = "sess_123",
            protectedUserId = "user_A",
            trustedUserId = "user_B",
            claimedIdentity = "State Bank Manager",
            requestedAction = "Transfer to safe custody account",
            requestSummary = "Caller claimed account is compromised",
            riskScoreAtCreation = 85,
            status = VerificationStatus.PENDING,
            createdAt = 1000L,
            expiresAt = 2000L
        )

        assertEquals("sess_123", session.id)
        assertEquals("user_A", session.protectedUserId)
        assertEquals("user_B", session.trustedUserId)
        assertEquals("State Bank Manager", session.claimedIdentity)
        assertEquals("Transfer to safe custody account", session.requestedAction)
        assertEquals("Caller claimed account is compromised", session.requestSummary)
        assertEquals(85, session.riskScoreAtCreation)
        assertEquals(VerificationStatus.PENDING, session.status)
        assertEquals(1000L, session.createdAt)
        assertEquals(2000L, session.expiresAt)
        assertNull(session.respondedAt)
        assertNull(session.responseDeviceId)
        assertEquals(1, session.version)
        assertFalse(session.isTerminal())
    }

    @Test
    fun testVerificationSessionTerminalAndExpiryHelpers() {
        val activeSession = VerificationSession(
            id = "sess_active",
            status = VerificationStatus.PENDING,
            expiresAt = 5000L
        )
        assertFalse(activeSession.isTerminal())
        assertFalse("Should not be expired before expiresAt", activeSession.isExpired(now = 4999L))
        assertTrue("Should be expired at expiresAt", activeSession.isExpired(now = 5000L))
        assertTrue("Should be expired past expiresAt", activeSession.isExpired(now = 5001L))

        val verifiedSession = activeSession.copy(
            status = VerificationStatus.VERIFIED,
            respondedAt = 4500L,
            responseDeviceId = "device_B_1"
        )
        assertTrue(verifiedSession.isTerminal())
        assertFalse("Terminal session should not be evaluated as expired", verifiedSession.isExpired(now = 6000L))

        val rejectedSession = activeSession.copy(
            status = VerificationStatus.REJECTED,
            respondedAt = 4600L
        )
        assertTrue(rejectedSession.isTerminal())
        assertFalse(rejectedSession.isExpired(now = 6000L))

        val unavailableSession = activeSession.copy(
            status = VerificationStatus.UNAVAILABLE,
            respondedAt = 4700L
        )
        assertTrue(unavailableSession.isTerminal())
        assertFalse(unavailableSession.isExpired(now = 6000L))
    }

    @Test
    fun testFirestoreSerializationMapping() {
        val originalSession = VerificationSession(
            id = "session_roundtrip_99",
            protectedUserId = "user_protected_1",
            trustedUserId = "user_trusted_2",
            claimedIdentity = "Cyber Crime Officer",
            requestedAction = "Send OTP",
            requestSummary = "Threatened with immediate arrest",
            riskScoreAtCreation = 95,
            status = VerificationStatus.VERIFIED,
            createdAt = 1700000000000L,
            expiresAt = 1700000300000L,
            respondedAt = 1700000150000L,
            responseDeviceId = "hw_pixel_9",
            version = 2
        )

        val map = FirestoreVerificationRepository.toMap(originalSession)
        assertEquals("user_protected_1", map["protectedUserId"])
        assertEquals("user_trusted_2", map["trustedUserId"])
        assertEquals("Cyber Crime Officer", map["claimedIdentity"])
        assertEquals("Send OTP", map["requestedAction"])
        assertEquals("Threatened with immediate arrest", map["requestSummary"])
        assertEquals(95, map["riskScoreAtCreation"])
        assertEquals("VERIFIED", map["status"])
        assertEquals(1700000000000L, map["createdAt"])
        assertEquals(1700000300000L, map["expiresAt"])
        assertEquals(1700000150000L, map["respondedAt"])
        assertEquals("hw_pixel_9", map["responseDeviceId"])
        assertEquals(2, map["version"])

        val deserialized = FirestoreVerificationRepository.fromMap(originalSession.id, map)
        assertEquals(originalSession, deserialized)
    }
}
