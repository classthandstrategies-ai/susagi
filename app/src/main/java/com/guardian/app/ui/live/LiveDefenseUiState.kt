package com.guardian.app.ui.live

import com.guardian.app.RiskReport
import com.guardian.app.ui.design.SuSagiRiskLevel

/**
 * Presentation model for an individual scam indicator signal.
 */
data class LiveSignalUiModel(
    val title: String,
    val description: String,
    val isCautionary: Boolean = false,
    val isCritical: Boolean = false
)

/**
 * Operational state of the live monitoring session.
 */
enum class LiveSessionStatus {
    STANDBY,
    MONITORING,
    ANALYZING,
    ANALYSIS_UNAVAILABLE,
    ERROR
}

/**
 * LiveDefenseUiState
 *
 * Presentation state for the redesigned SuSagi Live Defense screen.
 * Grounded in real runtime RiskReport and transcript data without calculating risk in the UI.
 */
data class LiveDefenseUiState(
    val riskLevel: SuSagiRiskLevel = SuSagiRiskLevel.LOW,
    val sessionStatus: LiveSessionStatus = LiveSessionStatus.STANDBY,
    val callerIdentifier: String = "Unknown Caller",
    val claimedIdentity: String? = null,
    val summaryHeadline: String = "Monitoring active call for deceptive patterns.",
    val summaryHeadlineHi: String = "सक्रिय कॉल में धोखाधड़ी के पैटर्न की निगरानी की जा रही है।",
    val signals: List<LiveSignalUiModel> = emptyList(),
    val latestTranscript: String? = null,
    val recommendedActionHeadline: String = "Continue normally. SuSagi is monitoring.",
    val recommendedActionHeadlineHi: String = "सामान्य रूप से जारी रखें। सुसागी निगरानी कर रहा है।",
    val recommendedActionDetail: String = "No sensitive requests or urgency tactics detected so far.",
    val recommendedActionDetailHi: String = "अब तक कोई संवेदनशील अनुरोध या दबाव नहीं देखा गया है।",
    val canEndCall: Boolean = true,
    val canVerifyIdentity: Boolean = true,
    val isIdentityVerificationAvailable: Boolean = false,
    val isOfflineMode: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {

        /* ============================================================================
         * LEGACY UI ADAPTER — REMOVE WHEN SHARED RiskLevel IS AVAILABLE
         * Centralizes the mapping of inherited riskScore into UI presentation levels.
         * Frontend does not calculate or alter scores; it only maps for display.
         * ============================================================================ */
        fun mapRiskScoreToPresentationLevel(score: Int): SuSagiRiskLevel {
            return when {
                score >= 85 -> SuSagiRiskLevel.CRITICAL
                score >= 60 -> SuSagiRiskLevel.HIGH
                score >= 25 -> SuSagiRiskLevel.CAUTION
                else -> SuSagiRiskLevel.LOW
            }
        }

        /**
         * Adapts existing runtime RiskReport, transcript buffer, and activity state
         * into clean, readable LiveDefenseUiState.
         */
        fun fromRuntime(
            report: RiskReport,
            transcript: String,
            callerNumber: String,
            isDetecting: Boolean,
            isAnalyzing: Boolean,
            errorMessage: String?,
            isHindi: Boolean = false
        ): LiveDefenseUiState {
            val level = mapRiskScoreToPresentationLevel(report.riskScore)

            val sessionStatus = when {
                errorMessage != null -> LiveSessionStatus.ERROR
                !isDetecting -> LiveSessionStatus.STANDBY
                isAnalyzing -> LiveSessionStatus.ANALYZING
                else -> LiveSessionStatus.MONITORING
            }

            // Extract human-friendly signals from real runtime breakdown
            val formattedSignals = mutableListOf<LiveSignalUiModel>()

            // 1. Runtime topSignals formatted for human readability
            report.topSignals.forEach { raw ->
                val humanTitle = when {
                    raw.title.contains("Authority", ignoreCase = true) || raw.title.contains("Impersonation", ignoreCase = true) ->
                        if (isHindi) "पहचान प्रतिरूपण का प्रयास" else "Authority Impersonation"
                    raw.title.contains("Credential", ignoreCase = true) || raw.title.contains("Extraction", ignoreCase = true) ->
                        if (isHindi) "ओटीपी या धनराशि की मांग" else "Sensitive Request (OTP / Funds)"
                    raw.title.contains("Pressure", ignoreCase = true) || raw.title.contains("Urgency", ignoreCase = true) ->
                        if (isHindi) "अत्यावश्यक दबाव और धमकी" else "Urgent Coercive Pressure"
                    raw.title.contains("Pattern:", ignoreCase = true) ->
                        if (isHindi) "ज्ञात घोटाला बातचीत पैटर्न" else "Recognized Scam Pattern"
                    else -> raw.title
                }
                formattedSignals.add(
                    LiveSignalUiModel(
                        title = humanTitle,
                        description = raw.detail.ifBlank { "Unusual conversation pattern detected." },
                        isCautionary = level == SuSagiRiskLevel.CAUTION,
                        isCritical = level == SuSagiRiskLevel.HIGH || level == SuSagiRiskLevel.CRITICAL
                    )
                )
            }

            // 2. Synthetic voice indicator (conservative supporting evidence)
            if (report.syntheticConfidence >= 0.6f || report.syntheticReasons.isNotEmpty()) {
                val reason = report.syntheticReasons.firstOrNull()
                    ?: if (isHindi) "वॉयस की प्रामाणिकता असामान्य लग रही है।" else "Voice authenticity looks unusual."
                formattedSignals.add(
                    LiveSignalUiModel(
                        title = if (isHindi) "संभावित सिंथेटिक वॉयस संकेत" else "Possible synthetic voice signal",
                        description = reason,
                        isCautionary = true
                    )
                )
            }

            // 3. Identity mismatch indicator (conservative profile comparison)
            if (report.identityMismatch.detected) {
                val mismatchReason = report.identityMismatch.reasons.firstOrNull()
                    ?: if (isHindi) "कॉलर का व्यवहार सहेजे गए संपर्क प्रोफाइल से मेल नहीं खाता।" else "Caller behavior does not match saved contact profile."
                formattedSignals.add(
                    LiveSignalUiModel(
                        title = if (isHindi) "कॉलर पहचान असत्यापित" else "Caller Identity Mismatch",
                        description = mismatchReason,
                        isCautionary = true
                    )
                )
            }

            // Headline explanation derived from plainReasoning or explanation
            val headlineEn = report.plainReasoning.ifBlank { report.explanationEn }
            val headlineHi = report.plainReasoningHi.ifBlank { report.explanationHi }

            // Recommended Action derived calmly based on risk level
            val (recHeadlineEn, recHeadlineHi, recDetailEn, recDetailHi) = when (level) {
                SuSagiRiskLevel.CRITICAL -> Quadruple(
                    "Do not share the OTP or send money.",
                    "ओटीपी साझा न करें और पैसे न भेजें।",
                    "Banks and police never demand OTPs or order digital arrest over a phone call.",
                    "बैंक और पुलिस कभी भी फोन पर ओटीपी नहीं मांगते और न ही डिजिटल गिरफ्तारी करते हैं।"
                )
                SuSagiRiskLevel.HIGH -> Quadruple(
                    "Verify the caller independently before continuing.",
                    "बातचीत जारी रखने से पहले कॉलर को स्वतंत्र रूप से सत्यापित करें।",
                    "This caller is requesting sensitive actions under urgency. Hang up and call back on an official number.",
                    "कॉलर जल्दबाजी में संवेदनशील कार्रवाई का अनुरोध कर रहा है। कॉल समाप्त करें और आधिकारिक नंबर पर कॉल करें।"
                )
                SuSagiRiskLevel.CAUTION -> Quadruple(
                    "Proceed with caution.",
                    "सावधानी से आगे बढ़ें।",
                    "Unusual financial requests or pressure detected. Do not make hurried payment decisions.",
                    "असामान्य वित्तीय अनुरोध या दबाव देखा गया है। जल्दबाजी में कोई भुगतान न करें।"
                )
                SuSagiRiskLevel.LOW -> Quadruple(
                    "No suspicious signals found so far.",
                    "अब तक कोई संदिग्ध संकेत नहीं मिला।",
                    if (isDetecting) "SuSagi is actively monitoring call speech in real time for any changes." else "No suspicious signals found so far.",
                    if (isDetecting) "सुसागी वास्तविक समय में किसी भी बदलाव के लिए कॉल पर नज़र रख रहा है।" else "अब तक कोई संदिग्ध संकेत नहीं मिला।"
                )
            }

            // Claimed identity extracted from pretext report if available
            val claimed = if (report.engines.pretextLegitimacy.detected && report.engines.pretextLegitimacy.summary.isNotBlank()) {
                report.engines.pretextLegitimacy.summary.take(40)
            } else null

            return LiveDefenseUiState(
                riskLevel = level,
                sessionStatus = sessionStatus,
                callerIdentifier = callerNumber.ifBlank { "Unknown Caller" },
                claimedIdentity = claimed,
                summaryHeadline = headlineEn,
                summaryHeadlineHi = headlineHi,
                signals = formattedSignals.take(4),
                latestTranscript = transcript.trim().ifBlank { null },
                recommendedActionHeadline = recHeadlineEn,
                recommendedActionHeadlineHi = recHeadlineHi,
                recommendedActionDetail = recDetailEn,
                recommendedActionDetailHi = recDetailHi,
                canEndCall = true,
                canVerifyIdentity = level != SuSagiRiskLevel.LOW,
                isIdentityVerificationAvailable = false,
                isOfflineMode = report.isOffline,
                errorMessage = errorMessage
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
