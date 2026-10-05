package com.guardian.app

import com.guardian.app.voip.DualSttController
import com.guardian.app.voip.LiveRiskAnalyzer
import com.guardian.app.voip.Speaker
import com.guardian.app.voip.TranscriptLine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

/**
 * NOTE: These tests require Android Context for SemanticAnalyzer
 * and cannot run in pure JVM unit tests. Marked @Ignore until
 * migrated to androidTest or a Context-free SemanticAnalyzer stub.
 */
@Ignore("SemanticAnalyzer requires Android Context — cannot run as JVM unit test")
class VoipPipelineTest {

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun stubAnalyzer() = SemanticAnalyzer(null as android.content.Context)

    @Test
    fun testInstantKeywordScoring_triggersImmediateHighRiskScore() = runTest {
        val flow = MutableSharedFlow<TranscriptLine>(extraBufferCapacity = 16)
        var latestReport: RiskReport? = null

        val analyzer = stubAnalyzer()
        val liveRiskAnalyzer = LiveRiskAnalyzer(analyzer, "en") { report ->
            latestReport = report
        }

        liveRiskAnalyzer.start(flow)

        // Emit instant high risk keyword "otp"
        flow.emit(TranscriptLine(Speaker.REMOTE, "Please share your OTP immediately", isFinal = false))

        // Assert score jumped to >= 60% instantly
        assertTrue(latestReport != null)
        assertEquals(60, latestReport?.riskScore)
        assertTrue(latestReport?.topSignals?.firstOrNull()?.title?.contains("otp", ignoreCase = true) == true)

        liveRiskAnalyzer.stop()
    }

    @Test
    fun testInstantKeywordScoring_hindiKeyword_triggersImmediateRiskScore() = runTest {
        val flow = MutableSharedFlow<TranscriptLine>(extraBufferCapacity = 16)
        var latestReport: RiskReport? = null

        val analyzer = stubAnalyzer()
        val liveRiskAnalyzer = LiveRiskAnalyzer(analyzer, "hi") { report ->
            latestReport = report
        }

        liveRiskAnalyzer.start(flow)

        // Emit instant Hindi high risk keyword "गिरफ्तार" (arrest)
        flow.emit(TranscriptLine(Speaker.REMOTE, "आपको पुलिस द्वारा गिरफ्तार किया जाएगा", isFinal = false))

        assertTrue(latestReport != null)
        assertEquals(60, latestReport?.riskScore)

        liveRiskAnalyzer.stop()
    }
}
