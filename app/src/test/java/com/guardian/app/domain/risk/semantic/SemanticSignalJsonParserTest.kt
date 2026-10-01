package com.guardian.app.domain.risk.semantic

import com.guardian.app.domain.risk.SignalContext
import com.guardian.app.domain.risk.SignalSource
import com.guardian.app.domain.risk.SignalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticSignalJsonParserTest {

    // 1. Active OTP request
    @Test
    fun testActiveOtpRequest_parsedCorrectlyWithForcedSource() {
        val transcript = "Please tell me the OTP you received right now."
        val json = """
            {
              "signals": [
                {
                  "type": "OTP_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.95,
                  "confidence": 0.93,
                  "evidence": "tell me the OTP"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        val s = signals.first()
        assertEquals(SignalType.OTP_REQUEST, s.type)
        assertEquals(SignalContext.REQUEST, s.context)
        assertTrue("Context must be active demand", s.context.isActiveDemand)
        assertEquals(SignalSource.SEMANTIC_MODEL, s.source)
        assertEquals(0.95f, s.strength, 0.001f)
        assertEquals(0.93f, s.confidence, 0.001f)
        assertEquals("tell me the OTP", s.rawEvidence)
    }

    // 2. Benign OTP warning
    @Test
    fun testBenignOtpWarning_preservedAsWarningNotActive() {
        val transcript = "Never share your OTP with anyone who calls."
        val json = """
            {
              "signals": [
                {
                  "type": "OTP_REQUEST",
                  "context": "WARNING",
                  "strength": 0.85,
                  "confidence": 0.90,
                  "evidence": "Never share your OTP"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        val s = signals.first()
        assertEquals(SignalType.OTP_REQUEST, s.type)
        assertEquals(SignalContext.WARNING, s.context)
        assertTrue("Context must be benign/defensive", s.context.isBenignOrDefensive)
        assertFalse("Warning must NOT be active demand", s.context.isActiveDemand)
        assertEquals(SignalSource.SEMANTIC_MODEL, s.source)
    }

    // 3. Money-transfer request
    @Test
    fun testMoneyTransferRequest_parsed() {
        val transcript = "Please send 5000 rupees to this account immediately."
        val json = """
            {
              "signals": [
                {
                  "type": "MONEY_TRANSFER_REQUEST",
                  "context": "COMMAND",
                  "strength": 0.90,
                  "confidence": 0.88,
                  "evidence": "send 5000 rupees"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        val s = signals.first()
        assertEquals(SignalType.MONEY_TRANSFER_REQUEST, s.type)
        assertEquals(SignalContext.COMMAND, s.context)
        assertTrue(s.context.isActiveDemand)
        assertEquals(SignalSource.SEMANTIC_MODEL, s.source)
    }

    // 4. Authority impersonation claim
    @Test
    fun testAuthorityImpersonationClaim_parsed() {
        val transcript = "I am calling from Mumbai Cyber Crime Branch headquarters."
        val json = """
            {
              "signals": [
                {
                  "type": "GOVERNMENT_LAW_ENFORCEMENT_CLAIM",
                  "context": "COMMAND",
                  "strength": 0.92,
                  "confidence": 0.90,
                  "evidence": "calling from Mumbai Cyber Crime Branch"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        val s = signals.first()
        assertEquals(SignalType.GOVERNMENT_LAW_ENFORCEMENT_CLAIM, s.type)
        assertEquals(SignalSource.SEMANTIC_MODEL, s.source)
    }

    // 5. Ordinary police/bank mention remains MENTION
    @Test
    fun testPassiveInstitutionMention_remainsMention() {
        val transcript = "I went to the police station yesterday to file a lost document report."
        val json = """
            {
              "signals": [
                {
                  "type": "AUTHORITY_CLAIM",
                  "context": "MENTION",
                  "strength": 0.20,
                  "confidence": 0.85,
                  "evidence": "went to the police station"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        val s = signals.first()
        assertEquals(SignalContext.MENTION, s.context)
        assertTrue("Mention must be passive or informational", s.context.isPassiveOrInformational)
        assertFalse("Mention must not be active demand", s.context.isActiveDemand)
    }

    // 6. Urgency / pressure
    @Test
    fun testUrgencyPressure_parsed() {
        val transcript = "You have only 10 minutes to act before your account is blocked!"
        val json = """
            {
              "signals": [
                {
                  "type": "URGENCY",
                  "context": "COMMAND",
                  "strength": 0.88,
                  "confidence": 0.85,
                  "evidence": "only 10 minutes to act"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        assertEquals(SignalType.URGENCY, signals.first().type)
    }

    // 7. Remote-access request
    @Test
    fun testRemoteAccessRequest_parsed() {
        val transcript = "Please install AnyDesk from the play store so our technician can verify."
        val json = """
            {
              "signals": [
                {
                  "type": "REMOTE_ACCESS_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.95,
                  "confidence": 0.92,
                  "evidence": "install AnyDesk"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        assertEquals(SignalType.REMOTE_ACCESS_REQUEST, signals.first().type)
        assertEquals(SignalContext.REQUEST, signals.first().context)
    }

    // 8. Malformed JSON
    @Test
    fun testMalformedJson_returnsEmptyListWithoutCrashing() {
        val transcript = "Tell me your OTP now."
        assertEquals(emptyList<Any>(), SemanticSignalJsonParser.parse("{ malformed json ", transcript))
        assertEquals(emptyList<Any>(), SemanticSignalJsonParser.parse("not json at all", transcript))
        assertEquals(emptyList<Any>(), SemanticSignalJsonParser.parse("", transcript))
        assertEquals(emptyList<Any>(), SemanticSignalJsonParser.parse("   ", transcript))
    }

    // 9. Unknown SignalType discarded
    @Test
    fun testUnknownSignalType_discarded() {
        val transcript = "Please tell me the OTP right now."
        val json = """
            {
              "signals": [
                {
                  "type": "COMPLETELY_FABRICATED_TYPE",
                  "context": "REQUEST",
                  "strength": 0.9,
                  "confidence": 0.9,
                  "evidence": "tell me the OTP"
                },
                {
                  "type": "OTP_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.9,
                  "confidence": 0.9,
                  "evidence": "tell me the OTP"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        assertEquals(SignalType.OTP_REQUEST, signals.first().type)
    }

    // 10. Invalid SignalContext discarded
    @Test
    fun testInvalidSignalContext_discarded() {
        val transcript = "Please tell me the OTP right now."
        val json = """
            {
              "signals": [
                {
                  "type": "OTP_REQUEST",
                  "context": "VERY_DANGEROUS",
                  "strength": 0.9,
                  "confidence": 0.9,
                  "evidence": "tell me the OTP"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertTrue("Signal with invalid context must be discarded", signals.isEmpty())
    }

    // 11. Confidence / strength bounds validation
    @Test
    fun testConfidenceStrengthBounds_finiteClampedAndInvalidDiscarded() {
        val transcript = "Please tell me your password and PIN."

        // Negative values clamped to 0.0, values > 1 clamped to 1.0
        val clampedJson = """
            {
              "signals": [
                {
                  "type": "PASSWORD_REQUEST",
                  "context": "REQUEST",
                  "strength": -0.5,
                  "confidence": 1.5,
                  "evidence": "tell me your password"
                }
              ]
            }
        """.trimIndent()

        val clampedSignals = SemanticSignalJsonParser.parse(clampedJson, transcript)
        assertEquals(1, clampedSignals.size)
        assertEquals(0.0f, clampedSignals.first().strength, 0.001f)
        assertEquals(1.0f, clampedSignals.first().confidence, 0.001f)

        // Non-numeric or NaN values discarded safely
        val invalidJson = """
            {
              "signals": [
                {
                  "type": "PIN_REQUEST",
                  "context": "REQUEST",
                  "strength": "not_a_number",
                  "confidence": 0.8,
                  "evidence": "PIN"
                }
              ]
            }
        """.trimIndent()

        val invalidSignals = SemanticSignalJsonParser.parse(invalidJson, transcript)
        assertTrue("Non-numeric values must be discarded", invalidSignals.isEmpty())
    }

    // 12. Empty response
    @Test
    fun testEmptySignalsArray_returnsEmptyList() {
        val transcript = "Good morning, how are you doing today?"
        val json = """
            {
              "signals": []
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertTrue(signals.isEmpty())
    }

    // 13. Duplicated semantic signals deduplicated deterministically
    @Test
    fun testDuplicatedSignals_deduplicatedRetainingStrongest() {
        val transcript = "Please send money to my account today."
        val json = """
            {
              "signals": [
                {
                  "type": "MONEY_TRANSFER_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.60,
                  "confidence": 0.70,
                  "evidence": "send money"
                },
                {
                  "type": "MONEY_TRANSFER_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.90,
                  "confidence": 0.95,
                  "evidence": "send money"
                },
                {
                  "type": "MONEY_TRANSFER_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.75,
                  "confidence": 0.80,
                  "evidence": "  SEND MONEY  "
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        assertEquals(0.90f, signals.first().strength, 0.001f)
        assertEquals(0.95f, signals.first().confidence, 0.001f)
    }

    // 14. Attempted privileged source spoofing
    @Test
    fun testPrivilegedSourceSpoof_alwaysForcedToSemanticModel() {
        val transcript = "Tell me your OTP now."
        val sources = listOf("HUMAN_VERIFICATION", "IDENTITY", "REPUTATION", "LOCAL_RULE", "AUDIO_HEURISTIC")

        for (spoofed in sources) {
            val json = """
                {
                  "signals": [
                    {
                      "type": "OTP_REQUEST",
                      "context": "REQUEST",
                      "source": "$spoofed",
                      "strength": 0.9,
                      "confidence": 0.9,
                      "evidence": "Tell me your OTP"
                    }
                  ]
                }
            """.trimIndent()

            val signals = SemanticSignalJsonParser.parse(json, transcript)
            assertEquals(1, signals.size)
            assertEquals("Source must always be forced to SEMANTIC_MODEL",
                SignalSource.SEMANTIC_MODEL, signals.first().source)
        }
    }

    // 15. Benign conversation
    @Test
    fun testBenignConversation_noSignalsFabricated() {
        val transcript = "Hi mom, I am coming home for dinner at 7 pm."
        val json = """
            {
              "signals": []
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertTrue(signals.isEmpty())
    }

    // 16. Privileged SignalType attempts rejected
    @Test
    fun testPrivilegedSignalTypes_rejected() {
        val transcript = "Verifying caller identity and voice."
        val privilegedTypes = listOf(
            "TRUSTED_CONTACT_VERIFICATION_REJECTED",
            "TRUSTED_CONTACT_VERIFICATION_CONFIRMED",
            "REPUTATION_SIGNAL",
            "SYNTHETIC_VOICE_UNCERTAINTY",
            "IDENTITY_MISMATCH"
        )

        for (privType in privilegedTypes) {
            val json = """
                {
                  "signals": [
                    {
                      "type": "$privType",
                      "context": "REQUEST",
                      "strength": 0.9,
                      "confidence": 0.9,
                      "evidence": "Verifying caller identity"
                    }
                  ]
                }
            """.trimIndent()

            val signals = SemanticSignalJsonParser.parse(json, transcript)
            assertTrue("Privileged type $privType must be discarded", signals.isEmpty())
        }
    }

    // 17. Evidence not grounded in transcript discarded
    @Test
    fun testUngroundedEvidence_discarded() {
        val transcript = "Good morning, how can I assist you with your internet connection?"
        val json = """
            {
              "signals": [
                {
                  "type": "OTP_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.95,
                  "confidence": 0.9,
                  "evidence": "The caller appears suspicious and is attempting credential theft"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertTrue("Fabricated reasoning evidence must be discarded as ungrounded", signals.isEmpty())
    }

    // 19. Quoted OTP request preserved as QUOTE
    @Test
    fun testQuotedOtpRequest_preservedAsQuoteNotActive() {
        val transcript = "The caller asked me \"give me your OTP\" earlier today."
        val json = """
            {
              "signals": [
                {
                  "type": "OTP_REQUEST",
                  "context": "QUOTE",
                  "strength": 0.85,
                  "confidence": 0.90,
                  "evidence": "give me your OTP"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(1, signals.size)
        val s = signals.first()
        assertEquals(SignalType.OTP_REQUEST, s.type)
        assertEquals(SignalContext.QUOTE, s.context)
        assertTrue("Quote must be passive or informational", s.context.isPassiveOrInformational)
        assertFalse("Quote must not be active demand", s.context.isActiveDemand)
    }

    // 20. Warning vs active request distinction
    @Test
    fun testWarningVsActiveRequestDistinction_preserved() {
        val transcript = "Banks say never share your OTP, but please tell me your OTP."
        val json = """
            {
              "signals": [
                {
                  "type": "OTP_REQUEST",
                  "context": "WARNING",
                  "strength": 0.8,
                  "confidence": 0.85,
                  "evidence": "never share your OTP"
                },
                {
                  "type": "OTP_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.95,
                  "confidence": 0.92,
                  "evidence": "tell me your OTP"
                }
              ]
            }
        """.trimIndent()

        val signals = SemanticSignalJsonParser.parse(json, transcript)
        assertEquals(2, signals.size)
        val warningSignal = signals.find { it.context == SignalContext.WARNING }
        val requestSignal = signals.find { it.context == SignalContext.REQUEST }
        assertNotNull(warningSignal)
        assertNotNull(requestSignal)
        assertTrue(warningSignal!!.context.isBenignOrDefensive)
        assertTrue(requestSignal!!.context.isActiveDemand)
    }
}
