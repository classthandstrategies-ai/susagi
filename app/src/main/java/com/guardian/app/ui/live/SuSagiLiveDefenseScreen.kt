package com.guardian.app.ui.live

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * SuSagiLiveDefenseScreen
 *
 * Primary user-facing Live Defense hero screen.
 * Formatted with banking-grade calm and clear information hierarchy:
 * 1. WHAT IS HAPPENING (Caller metadata & current risk status)
 * 2. WHAT SHOULD I DO NEXT (Immediate protective guidance and action triggers)
 * 3. WHY IT IS RISKY (Formatted scam signals)
 * 4. CALLER EVIDENCE (Latest live transcribed statement)
 */
@Composable
fun SuSagiLiveDefenseScreen(
    uiState: LiveDefenseUiState,
    onVerifyIdentity: () -> Unit = {},
    onEndCall: () -> Unit = {},
    onBlockNumber: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SuSagiSpacing.screenPadding)
            .padding(top = SuSagiSpacing.md, bottom = SuSagiSpacing.section),
        verticalArrangement = Arrangement.spacedBy(SuSagiSpacing.lg)
    ) {
        // ----------------------------------------------------
        // TOP APP BAR / DISMISSAL
        // ----------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "SuSagi",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.Brand,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHindi) "रीयल-टाइम कॉल सुरक्षा" else "Live Call Defense",
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextMuted
                )
            }

            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(SuSagiSpacing.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss live defense screen",
                        tint = SuSagiColors.TextSecondary
                    )
                }
            }
        }

        // ----------------------------------------------------
        // ANALYSIS UNAVAILABLE / TEMPORARY ERROR NOTICE
        // ----------------------------------------------------
        if (uiState.sessionStatus == LiveSessionStatus.ANALYSIS_UNAVAILABLE || uiState.errorMessage != null) {
            Surface(
                shape = SuSagiShape.sm,
                color = SuSagiColors.RiskCautionSoft,
                border = BorderStroke(1.dp, SuSagiColors.RiskCautionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = SuSagiColors.RiskCaution,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = uiState.errorMessage
                            ?: if (isHindi) "वॉयस विश्लेषण अनुपलब्ध है। बुनियादी कॉल सुरक्षा सक्रिय है।"
                            else "Live voice analysis is currently unavailable. On-device basic call protection remains active.",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // ----------------------------------------------------
        // 1. WHAT IS HAPPENING: RISK STATE & CALL IDENTITY
        // ----------------------------------------------------
        RiskStateHeader(
            uiState = uiState,
            isHindi = isHindi
        )

        // ----------------------------------------------------
        // 2. WHAT SHOULD I DO NEXT: RECOMMENDED ACTION & BUTTONS
        // ----------------------------------------------------
        RecommendedActionCard(
            uiState = uiState,
            onVerifyIdentity = onVerifyIdentity,
            onEndCall = onEndCall,
            onBlockNumber = onBlockNumber,
            isHindi = isHindi
        )

        // ----------------------------------------------------
        // 3. WHY IT IS RISKY: SIGNALS BREAKDOWN
        // ----------------------------------------------------
        LiveSignalList(
            signals = uiState.signals,
            isHindi = isHindi
        )

        // ----------------------------------------------------
        // 4. EVIDENCE: LATEST CALLER STATEMENT
        // ----------------------------------------------------
        LiveEvidenceCard(
            transcriptText = uiState.latestTranscript,
            isHindi = isHindi
        )

        // ----------------------------------------------------
        // 5. PRIVACY & ACCESS FOOTER
        // ----------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = SuSagiColors.TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = if (isHindi)
                    "सुसागी केवल सक्षम सुरक्षा सुविधाओं के लिए आवश्यक पहुंच का उपयोग करता है।"
                else
                    "SuSagi only uses the access needed for enabled protection features.",
                style = SuSagiTheme.typography.caption,
                color = SuSagiColors.TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS (UI PREVIEW DATA — NOT RUNTIME DATA)
// ============================================================================

@Preview(name = "Live Defense - Monitoring (Standby)", showBackground = true)
@Composable
private fun PreviewLiveDefenseMonitoring() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.LOW,
            sessionStatus = LiveSessionStatus.MONITORING,
            callerIdentifier = "+91 98765 43210",
            claimedIdentity = null,
            summaryHeadline = "Monitoring active call for deceptive intent.",
            summaryHeadlineHi = "सक्रिय बातचीत की निगरानी की जा रही है।",
            signals = emptyList(),
            latestTranscript = null,
            recommendedActionHeadline = "Continue normally. SuSagi is monitoring.",
            recommendedActionHeadlineHi = "सामान्य रूप से जारी रखें। सुसागी निगरानी कर रहा है।",
            recommendedActionDetail = "No sensitive requests or urgency tactics detected so far.",
            recommendedActionDetailHi = "अब तक कोई संवेदनशील अनुरोध नहीं देखा गया है।",
            canEndCall = true,
            canVerifyIdentity = false
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}

@Preview(name = "Live Defense - Low Risk", showBackground = true)
@Composable
private fun PreviewLiveDefenseLow() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.LOW,
            sessionStatus = LiveSessionStatus.MONITORING,
            callerIdentifier = "Known Contact: Rahul",
            claimedIdentity = null,
            summaryHeadline = "No suspicious signals found so far.",
            summaryHeadlineHi = "अब तक कोई संदिग्ध संकेत नहीं मिला।",
            signals = emptyList(),
            latestTranscript = "Hey, are you free for lunch tomorrow?",
            recommendedActionHeadline = "No action needed.",
            recommendedActionHeadlineHi = "किसी कार्रवाई की आवश्यकता नहीं है।",
            recommendedActionDetail = "Conversation appears routine and consistent with normal contacts.",
            recommendedActionDetailHi = "बातचीत सामान्य प्रतीत होती है।",
            canEndCall = true,
            canVerifyIdentity = false
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}

@Preview(name = "Live Defense - Caution", showBackground = true)
@Composable
private fun PreviewLiveDefenseCaution() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.CAUTION,
            sessionStatus = LiveSessionStatus.MONITORING,
            callerIdentifier = "+91 22 4000 1234",
            claimedIdentity = "Courier Service",
            summaryHeadline = "SuSagi noticed an unusual delivery verification request.",
            summaryHeadlineHi = "सुसागी ने असामान्य डिलीवरी सत्यापन अनुरोध देखा।",
            signals = listOf(
                LiveSignalUiModel(
                    title = "Unverified Delivery Fee",
                    description = "Caller is requesting a nominal 5 rupee verification charge.",
                    isCautionary = true
                )
            ),
            latestTranscript = "Please open your UPI app to pay the five rupee parcel fee.",
            recommendedActionHeadline = "Proceed with caution.",
            recommendedActionHeadlineHi = "सावधानी से आगे बढ़ें।",
            recommendedActionDetail = "Do not initiate UPI payments on unverified delivery calls.",
            recommendedActionDetailHi = "अनपेक्षित डिलीवरी कॉल पर यूपीआई भुगतान न करें।",
            canEndCall = true,
            canVerifyIdentity = true
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}

@Preview(name = "Live Defense - High Risk", showBackground = true)
@Composable
private fun PreviewLiveDefenseHigh() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.HIGH,
            sessionStatus = LiveSessionStatus.MONITORING,
            callerIdentifier = "+91 80000 99999",
            claimedIdentity = "Electricity Board Helpline",
            summaryHeadline = "This caller is requesting urgent payment to avoid power disconnection.",
            summaryHeadlineHi = "यह कॉलर बिजली कटने से बचने के लिए तत्काल भुगतान की मांग कर रहा है।",
            signals = listOf(
                LiveSignalUiModel(
                    title = "Urgent Disconnection Threat",
                    description = "Claiming power will be cut tonight unless immediate payment is made.",
                    isCritical = true
                ),
                LiveSignalUiModel(
                    title = "Unrecognized Payment Channel",
                    description = "Requesting direct UPI transfer to an unknown personal account.",
                    isCritical = true
                )
            ),
            latestTranscript = "Your meter will be disconnected in 30 minutes if you do not pay right now.",
            recommendedActionHeadline = "Do not transfer money.",
            recommendedActionHeadlineHi = "पैसे ट्रांसफर न करें।",
            recommendedActionDetail = "Contact your official electricity provider using the number on your latest bill.",
            recommendedActionDetailHi = "अपने नवीनतम बिल पर दिए गए आधिकारिक नंबर से संपर्क करें।",
            canEndCall = true,
            canVerifyIdentity = true
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}

@Preview(name = "Live Defense - Critical Risk", showBackground = true)
@Composable
private fun PreviewLiveDefenseCritical() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.CRITICAL,
            sessionStatus = LiveSessionStatus.MONITORING,
            callerIdentifier = "+91 11 2345 6789",
            claimedIdentity = "CBI Cyber Crime Officer",
            summaryHeadline = "Digital arrest extortion and immediate fund extraction attempt detected.",
            summaryHeadlineHi = "डिजिटल गिरफ्तारी जबरन वसूली और तत्काल धन निकासी का पता चला।",
            signals = listOf(
                LiveSignalUiModel(
                    title = "OTP / Credential Demand",
                    description = "Caller is demanding live banking OTP or password verification.",
                    isCritical = true
                ),
                LiveSignalUiModel(
                    title = "Impersonation Attempt",
                    description = "Claiming law enforcement authority to enforce fake legal confinement.",
                    isCritical = true
                ),
                LiveSignalUiModel(
                    title = "Urgent Coercive Pressure",
                    description = "Threatening immediate arrest if the call is disconnected or shared.",
                    isCritical = true
                )
            ),
            latestTranscript = "Tell me the six digit OTP immediately or an arrest team will arrive.",
            recommendedActionHeadline = "Do not share the OTP. End this call immediately.",
            recommendedActionHeadlineHi = "ओटीपी साझा न करें। तुरंत कॉल समाप्त करें।",
            recommendedActionDetail = "Police and banks NEVER enforce digital arrest or demand passwords over the phone.",
            recommendedActionDetailHi = "पुलिस और बैंक कभी भी फोन पर डिजिटल गिरफ्तारी या पासवर्ड की मांग नहीं करते हैं।",
            canEndCall = true,
            canVerifyIdentity = true
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}

@Preview(name = "Live Defense - No Transcript", showBackground = true)
@Composable
private fun PreviewLiveDefenseNoTranscript() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.LOW,
            sessionStatus = LiveSessionStatus.MONITORING,
            callerIdentifier = "Unknown Caller",
            claimedIdentity = null,
            summaryHeadline = "Monitoring active call for deceptive intent.",
            signals = emptyList(),
            latestTranscript = null,
            recommendedActionHeadline = "Listening for speech...",
            recommendedActionDetail = "SuSagi will evaluate speech patterns as the conversation begins.",
            canEndCall = true,
            canVerifyIdentity = false
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}

@Preview(name = "Live Defense - Analysis Unavailable", showBackground = true)
@Composable
private fun PreviewLiveDefenseAnalysisUnavailable() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.LOW,
            sessionStatus = LiveSessionStatus.ANALYSIS_UNAVAILABLE,
            callerIdentifier = "+91 99999 00000",
            claimedIdentity = null,
            summaryHeadline = "Speech analysis service is currently unavailable.",
            signals = emptyList(),
            latestTranscript = null,
            recommendedActionHeadline = "Basic call screening active.",
            recommendedActionDetail = "Number reputation blacklist checking remains operational.",
            canEndCall = true,
            canVerifyIdentity = false,
            errorMessage = "Speech recognition service is currently unavailable. On-device caller reputation protection is active."
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}

@Preview(name = "Live Defense - Large Font Scaling", fontScale = 1.3f, showBackground = true)
@Composable
private fun PreviewLiveDefenseLargeFont() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = LiveDefenseUiState(
            riskLevel = SuSagiRiskLevel.CRITICAL,
            sessionStatus = LiveSessionStatus.MONITORING,
            callerIdentifier = "+91 11 2345 6789",
            claimedIdentity = "CBI Cyber Cell",
            summaryHeadline = "Digital arrest extortion and fund extraction attempt detected.",
            signals = listOf(
                LiveSignalUiModel(
                    title = "OTP / Credential Demand",
                    description = "Caller is demanding live banking OTP.",
                    isCritical = true
                )
            ),
            latestTranscript = "Tell me the six digit OTP immediately.",
            recommendedActionHeadline = "Do not share the OTP. End this call immediately.",
            recommendedActionDetail = "Police and banks NEVER enforce digital arrest over the phone.",
            canEndCall = true,
            canVerifyIdentity = true
        )
        SuSagiLiveDefenseScreen(uiState = previewState)
    }
}
