package com.guardian.app.domain.risk.semantic

import com.guardian.app.domain.risk.SignalContext
import com.guardian.app.domain.risk.SignalSource
import com.guardian.app.domain.risk.SignalType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiSemanticSignalProviderTest {

    // 18. Provider network / transport exception safely degrades to emptyList()
    @Test
    fun testTransportException_returnsEmptyListWithoutCrashing() = runBlocking {
        val failingTransport = SemanticModelTransport { _, _ ->
            throw RuntimeException("429_RATE_LIMIT")
        }
        val provider = GeminiSemanticSignalProvider(failingTransport, "prompt")

        val result = provider.extractSignals("Please send money right now.")
        assertTrue("Transport exception must return emptyList without crashing", result.isEmpty())
    }

    @Test
    fun testTransportHttpError_returnsEmptyListWithoutCrashing() = runBlocking {
        val failingTransport = SemanticModelTransport { _, _ ->
            throw java.io.IOException("Connection timed out after 5000ms")
        }
        val provider = GeminiSemanticSignalProvider(failingTransport, "prompt")

        val result = provider.extractSignals("Please send money right now.")
        assertTrue("Network error must return emptyList", result.isEmpty())
    }

    @Test
    fun testBlankTranscript_returnsEmptyListWithoutCallingTransport() = runBlocking {
        var called = false
        val transport = SemanticModelTransport { _, _ ->
            called = true
            ""
        }
        val provider = GeminiSemanticSignalProvider(transport, "prompt")

        val result = provider.extractSignals("   ")
        assertTrue(result.isEmpty())
        assertEquals("Transport must not be called for blank transcript", false, called)
    }

    @Test
    fun testBlankResponse_returnsEmptyList() = runBlocking {
        val transport = SemanticModelTransport { _, _ -> "" }
        val provider = GeminiSemanticSignalProvider(transport, "prompt")

        val result = provider.extractSignals("Please tell me your OTP.")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSuccessfulExtraction_endToEnd() = runBlocking {
        val transcript = "Please tell me the OTP you just received."
        val mockJson = """
            {
              "signals": [
                {
                  "type": "OTP_REQUEST",
                  "context": "REQUEST",
                  "strength": 0.95,
                  "confidence": 0.92,
                  "evidence": "tell me the OTP"
                }
              ]
            }
        """.trimIndent()

        val transport = SemanticModelTransport { _, _ -> mockJson }
        val provider = GeminiSemanticSignalProvider(transport, "mock system prompt")

        val timestamp = 1700000000000L
        val signals = provider.extractSignals(transcript, timestampMs = timestamp)

        assertEquals(1, signals.size)
        val s = signals.first()
        assertEquals(SignalType.OTP_REQUEST, s.type)
        assertEquals(SignalContext.REQUEST, s.context)
        assertEquals(SignalSource.SEMANTIC_MODEL, s.source)
        assertEquals(timestamp, s.timestampMs)
        assertEquals("tell me the OTP", s.rawEvidence)
    }

    @Test
    fun testMalformedResponseFromTransport_returnsEmptyList() = runBlocking {
        val transport = SemanticModelTransport { _, _ -> "This is not valid json at all" }
        val provider = GeminiSemanticSignalProvider(transport, "prompt")

        val result = provider.extractSignals("Please send money right now.")
        assertTrue(result.isEmpty())
    }
}
