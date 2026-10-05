package com.guardian.app.ui.guardians

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VerificationNotificationIntentParserTest {

    @Test
    fun testValidRoute_parsesSuccessfully() {
        val route = VerificationNotificationIntentParser.parse(
            source = "identity_verification",
            sessionId = "sess_canonical_123"
        )
        assertNotNull("Valid source and non-blank session ID must produce a valid route", route)
        assertEquals("sess_canonical_123", route?.sessionId)
    }

    @Test
    fun testValidRoute_trimsWhitespace() {
        val route = VerificationNotificationIntentParser.parse(
            source = "identity_verification",
            sessionId = "   sess_trimmed_456   "
        )
        assertNotNull(route)
        assertEquals("sess_trimmed_456", route?.sessionId)
    }

    @Test
    fun testUnrelatedSource_returnsNull() {
        val route = VerificationNotificationIntentParser.parse(
            source = "scam_alert",
            sessionId = "sess_123"
        )
        assertNull("Unrelated source must be ignored", route)
    }

    @Test
    fun testMissingSource_returnsNull() {
        val route = VerificationNotificationIntentParser.parse(
            source = null,
            sessionId = "sess_123"
        )
        assertNull("Missing source must return null", route)
    }

    @Test
    fun testMissingSessionId_returnsNull() {
        val route = VerificationNotificationIntentParser.parse(
            source = "identity_verification",
            sessionId = null
        )
        assertNull("Missing sessionId must return null", route)
    }

    @Test
    fun testEmptySessionId_returnsNull() {
        val route = VerificationNotificationIntentParser.parse(
            source = "identity_verification",
            sessionId = ""
        )
        assertNull("Empty sessionId must return null", route)
    }

    @Test
    fun testBlankSessionId_returnsNull() {
        val route = VerificationNotificationIntentParser.parse(
            source = "identity_verification",
            sessionId = "     "
        )
        assertNull("Blank sessionId must return null", route)
    }
}
