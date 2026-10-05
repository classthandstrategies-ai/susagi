package com.guardian.app.ui.activity

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.callprotect.NumberReputationRepository
import com.guardian.app.evidence.EvidenceCaptureActivity
import com.guardian.app.evidence.EvidenceSharer
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.components.SuSagiTopBar
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme
import kotlinx.coroutines.launch

/**
 * Human-readable Incident & Scam Session Detail Screen.
 *
 * Grounded strictly in real data:
 * - What happened & why it is risky
 * - Chronological event timeline
 * - Outcome & conservative follow-up guidance
 * - Secondary evidence vault (transcripts & signals)
 * - Real actionable defenses (Block, Report 1930, Evidence Export)
 */
@Composable
fun ScamSessionDetailScreen(
    uiModel: ScamSessionDetailUiModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isBlocked by remember {
        mutableStateOf(uiModel.actionTaken?.contains("block", ignoreCase = true) == true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
    ) {
        SuSagiTopBar(
            title = if (uiModel.isAggregatedSession) {
                if (isHindi) "घोटाला सत्र विवरण" else "Scam Session Detail"
            } else {
                if (isHindi) "घटना विवरण" else "Incident Detail"
            },
            subtitle = "${uiModel.callerNumber} · ${uiModel.timestampFormatted}",
            onBack = onBack,
            backContentDescription = "Return to Activity"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SuSagiSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card: Incident Overview & Risk Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
                border = BorderStroke(1.dp, SuSagiColors.Border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiModel.title,
                            style = SuSagiTheme.typography.title,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { heading() }
                        )

                        Spacer(Modifier.width(8.dp))

                        RiskBadge(
                            level = uiModel.riskLevel,
                            size = RiskBadgeSize.Regular
                        )
                    }

                    Text(
                        text = uiModel.summary,
                        style = SuSagiTheme.typography.bodyMedium,
                        color = SuSagiColors.TextSecondary,
                        lineHeight = 22.sp
                    )

                    // Outcome line
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = SuSagiShape.card,
                        colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
                        border = BorderStroke(1.dp, SuSagiColors.Border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isHindi) "परिणाम" else "Outcome",
                                style = SuSagiTheme.typography.caption,
                                color = SuSagiColors.TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = uiModel.outcome,
                                style = SuSagiTheme.typography.bodyMedium,
                                color = SuSagiColors.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Timeline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
                border = BorderStroke(1.dp, SuSagiColors.Border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isHindi) "घटनाक्रम (टाइमलाइन)" else "Timeline",
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )

                    Column {
                        uiModel.timelineEvents.forEachIndexed { index, event ->
                            val isLast = index == uiModel.timelineEvents.lastIndex
                            IncidentTimelineItem(
                                event = event,
                                isLast = isLast
                            )
                        }
                    }
                }
            }

            // Recommended Follow-Up Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(
                    containerColor = if (uiModel.riskLevel == SuSagiRiskLevel.CRITICAL) {
                        SuSagiColors.RiskCriticalSoft
                    } else {
                        SuSagiColors.Surface
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (uiModel.riskLevel == SuSagiRiskLevel.CRITICAL) {
                        SuSagiColors.RiskCriticalBorder
                    } else {
                        SuSagiColors.Border
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isHindi) "अनुशंसित सुरक्षा कदम" else "Recommended Follow-Up",
                        style = SuSagiTheme.typography.title,
                        color = if (uiModel.riskLevel == SuSagiRiskLevel.CRITICAL) {
                            SuSagiColors.RiskCritical
                        } else {
                            SuSagiColors.TextPrimary
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )

                    Text(
                        text = uiModel.recommendedFollowUp,
                        style = SuSagiTheme.typography.bodyMedium,
                        color = SuSagiColors.TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Secondary Evidence Vault Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
                border = BorderStroke(1.dp, SuSagiColors.Border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = SuSagiColors.Brand,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "साक्ष्य और विवरण" else "Captured Evidence",
                            style = SuSagiTheme.typography.title,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { heading() }
                        )
                    }

                    if (uiModel.detectedSignals.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isHindi) "पहचाने गए पैटर्न:" else "Identified Risk Indicators:",
                                style = SuSagiTheme.typography.caption,
                                color = SuSagiColors.TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = uiModel.detectedSignals.joinToString(" · "),
                                style = SuSagiTheme.typography.bodyMedium,
                                color = SuSagiColors.RiskCaution,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!uiModel.transcript.isNullOrBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isHindi) "बातचीत का अंश:" else "Transcript Snippet:",
                                style = SuSagiTheme.typography.caption,
                                color = SuSagiColors.TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = SuSagiShape.card,
                                colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
                                border = BorderStroke(1.dp, SuSagiColors.Border)
                            ) {
                                Text(
                                    text = "\"${uiModel.transcript}\"",
                                    style = SuSagiTheme.typography.caption,
                                    color = SuSagiColors.TextSecondary,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    } else if (uiModel.detectedSignals.isEmpty()) {
                        Text(
                            text = if (isHindi) "इस कॉल के दौरान कोई संदिग्ध साक्ष्य दर्ज नहीं किया गया।" else "No suspicious indicators or recordings captured during this session.",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                    }
                }
            }

            // Real Action Section
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Action 1: Open Cybercrime Portal
                SecondarySafetyAction(
                    text = if (isHindi) "साइबर क्राइम पोर्टल खोलें" else "Open cybercrime portal",
                    onClick = {
                        EvidenceSharer.openCybercrimePortal(context)
                    },
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )

                // Action 2: Mark as Suspicious
                SecondarySafetyAction(
                    text = if (isBlocked) {
                        if (isHindi) "संदिग्ध के रूप में चिह्नित" else "Marked as suspicious"
                    } else {
                        if (isHindi) "संदिग्ध के रूप में चिह्नित करें" else "Mark as suspicious"
                    },
                    onClick = {
                        if (!isBlocked) {
                            scope.launch {
                                val repo = NumberReputationRepository(context)
                                repo.flag(uiModel.callerNumber, "SCAM", "Flagged from Activity Ledger")
                                isBlocked = true
                                Toast.makeText(context, "Number marked as suspicious", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isBlocked,
                    isDestructive = !isBlocked,
                    icon = Icons.Default.Block,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )

                // Action 3: Generate Evidence Report
                PrimarySafetyAction(
                    text = if (isHindi) "साक्ष्य रिपोर्ट तैयार करें" else "Generate Evidence Report",
                    onClick = {
                        val intent = Intent(context, EvidenceCaptureActivity::class.java).apply {
                            putExtra("riskScore", uiModel.riskScore)
                            putExtra("callerId", uiModel.callerNumber)
                            putExtra("packageName", "SuSagi Safety Ledger")
                            putExtra("transcript", uiModel.transcript ?: "")
                        }
                        context.startActivity(intent)
                    },
                    icon = Icons.Default.Description,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS — RICH SCAM SESSIONS & ACCESSIBILITY
// ============================================================================

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "PreviewScamSessionBankImpersonation", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewScamSessionBankImpersonation() {
    SuSagiTheme {
        ScamSessionDetailScreen(
            uiModel = ScamSessionDetailUiModel(
                incidentId = 101L,
                title = "Possible SBI impersonation",
                riskLevel = SuSagiRiskLevel.HIGH,
                riskScore = 82,
                callerNumber = "+91 98765 00112",
                timestampFormatted = "Today · 2:07 PM",
                summary = "Caller claimed to be from State Bank of India and requested sensitive verification information.",
                timelineEvents = listOf(
                    ScamTimelineEvent("2:02 PM", "Suspicious message received", "SMS claiming KYC suspension with verification link", ScamTimelineEventType.MESSAGE),
                    ScamTimelineEvent("2:03 PM", "Link inspected", "hxxp://sbi-kyc-update.xyz flagged by QR/Link Shield", ScamTimelineEventType.LINK),
                    ScamTimelineEvent("2:05 PM", "Incoming call received", "Caller claimed to represent SBI Fraud Department", ScamTimelineEventType.CALL),
                    ScamTimelineEvent("2:06 PM", "OTP requested", "Caller pressurized for SMS 6-digit one-time passcode", ScamTimelineEventType.SYSTEM),
                    ScamTimelineEvent("2:07 PM", "Call ended", "Call disconnected by user upon high-risk prompt", ScamTimelineEventType.ACTION)
                ),
                transcript = "Sir, your SBI account KYC has expired. Please share the 6-digit authorization code received on your registered mobile number immediately.",
                detectedSignals = listOf("KYC Suspension Threat", "OTP Solicitation", "Artificial Urgency"),
                actionTaken = "CALL_ENDED",
                outcome = "Identity could not be verified — High risk",
                recommendedFollowUp = "Do not send money or share sensitive information. Contact SBI directly at their official helpline 1800 1234.",
                isAggregatedSession = true
            ),
            onBack = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "PreviewScamSessionFamilyImpersonation", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewScamSessionFamilyImpersonation() {
    SuSagiTheme {
        ScamSessionDetailScreen(
            uiModel = ScamSessionDetailUiModel(
                incidentId = 102L,
                title = "Possible family impersonation",
                riskLevel = SuSagiRiskLevel.CRITICAL,
                riskScore = 94,
                callerNumber = "+91 91234 99887",
                timestampFormatted = "Yesterday · 7:42 PM",
                summary = "Caller claimed to be your son requiring urgent hospital payment. Out-of-band identity check failed.",
                timelineEvents = listOf(
                    ScamTimelineEvent("7:38 PM", "Incoming call received", "Caller claimed to be family member in urgent medical distress", ScamTimelineEventType.CALL),
                    ScamTimelineEvent("7:40 PM", "₹25,000 transfer requested", "Caller insisted on immediate UPI transfer to third-party QR", ScamTimelineEventType.SYSTEM),
                    ScamTimelineEvent("7:41 PM", "Identity verification requested", "Out-of-band challenge dispatched to Mom (+91 98765 43210)", ScamTimelineEventType.VERIFICATION),
                    ScamTimelineEvent("7:42 PM", "Trusted person rejected request", "Guardian responded NO, NOT ME", ScamTimelineEventType.VERIFICATION),
                    ScamTimelineEvent("7:42 PM", "Call ended & Number Blocked", "User immediately hung up and added caller to blocklist", ScamTimelineEventType.ACTION)
                ),
                transcript = "Mom please, my friend met with an accident, send 25,000 immediately to this hospital UPI ID, I will explain later.",
                detectedSignals = listOf("Impersonation Claim", "Emotional Coercion", "Emergency Wire Request"),
                actionTaken = "BLOCKED",
                outcome = "Identity could not be verified — Trusted person responded NO",
                recommendedFollowUp = "Do not send money or share sensitive information. The caller was attempting to impersonate your family member.",
                isAggregatedSession = true
            ),
            onBack = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "PreviewScamSessionLargeFont", fontScale = 1.3f, showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewScamSessionLargeFont() {
    SuSagiTheme {
        ScamSessionDetailScreen(
            uiModel = ScamSessionDetailUiModel(
                incidentId = 103L,
                title = "Suspicious caller",
                riskLevel = SuSagiRiskLevel.CAUTION,
                riskScore = 35,
                callerNumber = "+91 99887 76655",
                timestampFormatted = "2 days ago",
                summary = "Caller claimed unusual prize winnings requiring courier fee.",
                timelineEvents = listOf(
                    ScamTimelineEvent("11:15 AM", "Incoming call received", "Unknown caller claiming lottery award", ScamTimelineEventType.CALL),
                    ScamTimelineEvent("11:17 AM", "Fee requested", "Courier fee requested via UPI", ScamTimelineEventType.SYSTEM),
                    ScamTimelineEvent("11:18 AM", "Call ended", "Call disconnected", ScamTimelineEventType.ACTION)
                ),
                transcript = "Congratulations you have won 5 lakh lottery, pay registration charges of 1,500 rupees to claim.",
                detectedSignals = listOf("Advance-Fee Fraud Pattern"),
                actionTaken = "CALL_ENDED",
                outcome = "Unusual conversational patterns",
                recommendedFollowUp = "Exercise caution. Never pay advance fees for unsolicited prizes.",
                isAggregatedSession = false
            ),
            onBack = {}
        )
    }
}
