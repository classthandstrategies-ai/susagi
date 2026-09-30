package com.guardian.app.ui.guardians

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SuSagiTopBar
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Phone B: Identity Check Responder Screen
 *
 * Rendered on the trusted contact's device when a real-time out-of-band verification challenge
 * is dispatched. Features high-clarity incident context and two rapid, unambiguous decision actions:
 * - [ YES, IT'S ME ] -> Confirms legitimate caller on Phone A
 * - [ NO, NOT ME ]   -> Immediately triggers IMPOSTER DETECTED alert on Phone A
 */
@Composable
fun IdentityCheckResponderScreen(
    uiModel: ResponderIdentityCheckUiModel,
    onConfirmYes: () -> Unit,
    onRejectNo: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
    ) {
        SuSagiTopBar(
            title = if (isHindi) "सुसागी पहचान जांच" else "SuSagi Identity Check",
            subtitle = if (isHindi) "तत्काल सत्यापन अनुरोध" else "Immediate Verification Request"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SuSagiSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Alert Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
                border = BorderStroke(1.dp, SuSagiColors.Border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SuSagiColors.BrandSoft,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SuSagiColors.Brand,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "सत्यापन अनुरोधकर्ता" else "Challenge Requested By",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                        Text(
                            text = uiModel.requesterName,
                            style = SuSagiTheme.typography.title,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiModel.requesterNumber,
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextSecondary
                        )
                    }

                    RiskBadge(
                        level = SuSagiRiskLevel.HIGH,
                        customLabel = if (isHindi) "तत्काल" else "Urgent",
                        size = RiskBadgeSize.Small
                    )
                }
            }

            // Claim Detail Card
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
                    Text(
                        text = if (isHindi) "कॉल विवरण" else "Call Context & Claim",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted
                    )

                    Text(
                        text = if (isHindi)
                            "${uiModel.requesterName} के पास एक कॉल आई है जिसमें कॉलर आपका रूप धारण करने का दावा कर रहा है।"
                        else
                            "${uiModel.requesterName} is on a call with someone claiming to be you.",
                        style = SuSagiTheme.typography.bodyLarge,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    if (!uiModel.amountOrAction.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = SuSagiShape.card,
                            colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
                            border = BorderStroke(1.dp, SuSagiColors.Border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "दावा किया गया अनुरोध:" else "Reported Action / Request:",
                                    style = SuSagiTheme.typography.caption,
                                    color = SuSagiColors.TextSecondary
                                )
                                Text(
                                    text = uiModel.amountOrAction,
                                    style = SuSagiTheme.typography.title,
                                    color = SuSagiColors.TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = uiModel.claimDetail,
                        style = SuSagiTheme.typography.bodyMedium,
                        color = SuSagiColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Decision Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
                border = BorderStroke(1.5.dp, SuSagiColors.Brand)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isHindi)
                            "क्या आप इस समय ${uiModel.requesterName} के साथ कॉल पर हैं या यह अनुरोध कर रहे हैं?"
                        else
                            "Are you currently on a call with ${uiModel.requesterName} or making this request?",
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        modifier = Modifier.semantics { heading() }
                    )

                    // Decision Button 1: YES, IT'S ME
                    PrimarySafetyAction(
                        text = if (isHindi) "हाँ, यह मैं हूँ" else "YES, IT'S ME",
                        onClick = onConfirmYes,
                        icon = Icons.Default.Check,
                        height = 52.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Decision Button 2: NO, NOT ME
                    PrimarySafetyAction(
                        text = if (isHindi) "नहीं, मैं नहीं हूँ" else "NO, NOT ME",
                        onClick = onRejectNo,
                        icon = Icons.Default.Close,
                        isCritical = true,
                        height = 52.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Security note footer
            Text(
                text = if (isHindi)
                    "आपकी प्रतिक्रिया तुरंत सुसागी सुरक्षित चैनल के माध्यम से भेजी जाएगी ताकि आपके प्रियजन को प्रतिरूपण घोटालों से बचाया जा सके।"
                else
                    "Your response is transmitted securely to protect your contact from real-time impersonation scams.",
                style = SuSagiTheme.typography.caption,
                color = SuSagiColors.TextMuted,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS — CANONICAL DEMO SCENARIO
// ============================================================================

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Responder — Canonical ₹25,000 Hospital Emergency", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun IdentityCheckResponderPreview() {
    SuSagiTheme {
        IdentityCheckResponderScreen(
            uiModel = ResponderIdentityCheckUiModel(
                requesterName = "Aarav Sharma",
                requesterNumber = "+91 98765 43210",
                amountOrAction = "₹25,000 Urgent Hospital Wire Transfer",
                claimDetail = "The caller claims an urgent hospital admission requires immediate funds via UPI and asked not to delay."
            ),
            onConfirmYes = {},
            onRejectNo = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Responder — Hindi Preview", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun IdentityCheckResponderHindiPreview() {
    SuSagiTheme {
        IdentityCheckResponderScreen(
            uiModel = ResponderIdentityCheckUiModel(
                requesterName = "आरव शर्मा",
                requesterNumber = "+91 98765 43210",
                amountOrAction = "₹25,000 तत्काल अस्पताल वायर ट्रांसफर",
                claimDetail = "कॉलर का दावा है कि तत्काल अस्पताल में भर्ती के लिए UPI के माध्यम से तत्काल धन की आवश्यकता है।"
            ),
            onConfirmYes = {},
            onRejectNo = {},
            isHindi = true
        )
    }
}
