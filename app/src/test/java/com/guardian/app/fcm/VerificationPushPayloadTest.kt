package com.guardian.app.fcm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationPushPayloadTest {

    @Test
    fun testValidVerificationPayload_parsesCorrectly() {
        val data = mapOf(
            "type" to "identity_verification",
            "sessionId" to "sess_2026_test_123",
            "claimedIdentity" to "Reserve Bank Officer",
            "expiresAt" to "1790900000000",
            "requestedAction" to "Verify high-value transfer"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNotNull("Payload must parse successfully for valid data", payload)
        assertEquals("sess_2026_test_123", payload?.sessionId)
        assertEquals("Reserve Bank Officer", payload?.claimedIdentity)
        assertEquals(1790900000000L, payload?.expiresAt)
        assertEquals("Verify high-value transfer", payload?.requestedAction)
    }

    @Test
    fun testValidVerificationPayload_withoutOptionalRequestedAction_parsesCorrectly() {
        val data = mapOf(
            "type" to "identity_verification",
            "sessionId" to "sess_simple_456",
            "claimedIdentity" to "Police Cyber Cell",
            "expiresAt" to "1790900000000"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNotNull(payload)
        assertEquals("sess_simple_456", payload?.sessionId)
        assertEquals("Police Cyber Cell", payload?.claimedIdentity)
        assertNull(payload?.requestedAction)
    }

    @Test
    fun testMalformedPayload_wrongType_rejected() {
        val data = mapOf(
            "type" to "scam_alert",
            "sessionId" to "sess_123",
            "claimedIdentity" to "Bank",
            "expiresAt" to "1790900000000"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNull("Payload with non-matching type must be rejected", payload)
    }

    @Test
    fun testMalformedPayload_missingSessionId_rejected() {
        val data = mapOf(
            "type" to "identity_verification",
            "claimedIdentity" to "Bank",
            "expiresAt" to "1790900000000"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNull("Missing sessionId must be rejected", payload)
    }

    @Test
    fun testMalformedPayload_emptySessionId_rejected() {
        val data = mapOf(
            "type" to "identity_verification",
            "sessionId" to "   ",
            "claimedIdentity" to "Bank",
            "expiresAt" to "1790900000000"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNull("Blank sessionId must be rejected", payload)
    }

    @Test
    fun testMalformedPayload_missingClaimedIdentity_rejected() {
        val data = mapOf(
            "type" to "identity_verification",
            "sessionId" to "sess_123",
            "expiresAt" to "1790900000000"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNull("Missing claimedIdentity must be rejected", payload)
    }

    @Test
    fun testMalformedPayload_invalidExpiresAt_rejected() {
        val data = mapOf(
            "type" to "identity_verification",
            "sessionId" to "sess_123",
            "claimedIdentity" to "Bank",
            "expiresAt" to "not_a_number"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNull("Non-numeric expiresAt must be rejected", payload)
    }

    @Test
    fun testMalformedPayload_negativeExpiresAt_rejected() {
        val data = mapOf(
            "type" to "identity_verification",
            "sessionId" to "sess_123",
            "claimedIdentity" to "Bank",
            "expiresAt" to "-500"
        )

        val payload = VerificationPushPayload.parse(data)
        assertNull("Negative expiresAt must be rejected", payload)
    }

    @Test
    fun testExpiryHelper_reportsCorrectExpiryState() {
        val futurePayload = VerificationPushPayload(
            sessionId = "sess_fut",
            claimedIdentity = "Bank Rep",
            expiresAt = 2000L
        )

        assertFalse("Future expiry is not expired", futurePayload.isExpired(now = 1000L))
        assertTrue("Past expiry is expired", futurePayload.isExpired(now = 3000L))
        assertTrue("Exact timestamp is expired", futurePayload.isExpired(now = 2000L))
    }
}
