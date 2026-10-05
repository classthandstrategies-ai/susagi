package com.guardian.app.ui.guardians

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Phone A: Identity Verification Requester Screen
 *
 * Rendered on the device of a user who is currently on a suspicious phone call.
 * Allows sending an out-of-band challenge to a trusted contact (e.g. Mom, Partner)
 * to verify whether the caller is who they claim to be.
 *
 * Implements all 6 lifecycle presentation states:
 * - READY: Before initiation (disables action if Platform session is unavailable)
 * - PENDING: Challenge sent, awaiting responder decision with live countdown
 * - VERIFIED: Contact confirmed identity (with conservative transaction caution)
 * - REJECTED: Guardian rejected identity. Urgent protective advice and one-tap End Call
 * - EXPIRED: Timeout reached without response
 * - UNAVAILABLE: Platform or network offline
 */
@Composable
fun IdentityVerificationRequesterScreen(
    uiModel: RequesterVerificationUiModel,
    onSendVerification: () -> Unit,
    onCancelVerification: () -> Unit,
    onEndCall: () -> Unit,
    onReturnToCall: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
    ) {
        SuSagiTopBar(
            title = if (isHindi) "पहचान सत्यापन" else "Identity Verification",
            subtitle = if (isHindi) "कॉल करने वाले की पहचान जांचें" else "Out-of-band caller verification",
            onBack = onReturnToCall,
            backContentDescription = "Return to Call"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SuSagiSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Caller Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
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
                        color = SuSagiColors.SurfaceElevated,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = SuSagiColors.TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "इनकमिंग कॉल" else "Current Call",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                        Text(
                            text = uiModel.callerName,
                            style = SuSagiTheme.typography.title,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiModel.callerNumber,
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextSecondary
                        )
                    }
                }
            }

            // State-Specific Hero Card
            when (uiModel.status) {
                VerificationStatus.READY -> {
                    ReadyStateCard(
                        uiModel = uiModel,
                        onSendVerification = onSendVerification,
                        onReturnToCall = onReturnToCall,
                        isHindi = isHindi
                    )
                }
                VerificationStatus.PENDING -> {
                    PendingStateCard(
                        uiModel = uiModel,
                        onCancelVerification = onCancelVerification,
                        isHindi = isHindi
                    )
                }
                VerificationStatus.VERIFIED -> {
                    VerifiedStateCard(
                        uiModel = uiModel,
                        onReturnToCall = onReturnToCall,
                        isHindi = isHindi
                    )
                }
                VerificationStatus.REJECTED -> {
                    RejectedStateCard(
                        uiModel = uiModel,
                        onEndCall = onEndCall,
                        isHindi = isHindi
                    )
                }
                VerificationStatus.EXPIRED -> {
                    ExpiredStateCard(
                        uiModel = uiModel,
                        onRetry = onSendVerification,
                        onReturnToCall = onReturnToCall,
                        isHindi = isHindi
                    )
                }
                VerificationStatus.UNAVAILABLE -> {
                    UnavailableStateCard(
                        uiModel = uiModel,
                        onReturnToCall = onReturnToCall,
                        isHindi = isHindi
                    )
                }
            }
        }
    }
}

// ============================================================================
// STATE CARDS
// ============================================================================

@Composable
private fun ReadyStateCard(
    uiModel: RequesterVerificationUiModel,
    onSendVerification: () -> Unit,
    onReturnToCall: () -> Unit,
    isHindi: Boolean
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "सत्यापन चुनौती भेजें" else "Send Identity Challenge",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                RiskBadge(
                    level = SuSagiRiskLevel.LOW,
                    customLabel = if (isHindi) "तैयार" else "Ready",
                    size = RiskBadgeSize.Small
                )
            }

            Text(
                text = if (isHindi)
                    "सुसागी ${uiModel.guardianName} (${uiModel.guardianNumber}) को एक सुरक्षित जांच भेजेगा ताकि यह पुष्टि हो सके कि वे ही इस कॉल पर हैं।"
                else
                    "SuSagi will send a secure out-of-band verification challenge to ${uiModel.guardianName} (${uiModel.guardianNumber}) to confirm whether they are currently calling you.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 22.sp
            )

            // Button with honest disabled caption
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PrimarySafetyAction(
                    text = if (isHindi) "सत्यापन अनुरोध भेजें" else "Send Verification Request",
                    onClick = onSendVerification,
                    enabled = uiModel.isPlatformIntegrated,
                    icon = Icons.Default.Security,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!uiModel.isPlatformIntegrated) {
                    Text(
                        text = if (isHindi)
                            "पहचान सत्यापन अभी उपलब्ध नहीं है। (प्लेटफ़ॉर्म एकीकरण लंबित)"
                        else
                            "Identity verification is not available yet.",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            SecondarySafetyAction(
                text = if (isHindi) "कॉल पर वापस जाएं" else "Return to Call",
                onClick = onReturnToCall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PendingStateCard(
    uiModel: RequesterVerificationUiModel,
    onCancelVerification: () -> Unit,
    isHindi: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha-pulse"
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = SuSagiColors.Brand,
                    strokeWidth = 3.dp
                )
                // Runtime countdown is only displayed if platform provides real deadline/session
                if (uiModel.isPlatformIntegrated && uiModel.remainingSeconds > 0) {
                    Text(
                        text = "${uiModel.remainingSeconds}s",
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.Brand,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = if (isHindi)
                    "${uiModel.guardianName} से पुष्टि की प्रतीक्षा की जा रही है..."
                else
                    "Waiting for ${uiModel.guardianName} to confirm...",
                style = SuSagiTheme.typography.title,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isHindi)
                    "सत्यापन लंबित रहने के दौरान पैसे न भेजें और संवेदनशील जानकारी साझा न करें।"
                else
                    "Do not send money or share sensitive information while verification is pending.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 22.sp
            )

            SecondarySafetyAction(
                text = if (isHindi) "कॉल पर वापस जाएं" else "Return to Call",
                onClick = onCancelVerification,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VerifiedStateCard(
    uiModel: RequesterVerificationUiModel,
    onReturnToCall: () -> Unit,
    isHindi: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.5.dp, SuSagiColors.RiskLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "पहचान सत्यापित" else "Identity Confirmed",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.RiskLow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                RiskBadge(
                    level = SuSagiRiskLevel.LOW,
                    customLabel = if (isHindi) "सत्यापित" else "Verified",
                    size = RiskBadgeSize.Small
                )
            }

            Text(
                text = if (isHindi)
                    "${uiModel.guardianName} ने पुष्टि की है कि यह अनुरोध उनकी तरफ से है।"
                else
                    "${uiModel.guardianName} confirmed this request is from them.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )

            // CRITICAL CONSERVATIVE GUIDANCE:
            // Do NOT claim "call is safe" or "clean". Identity is verified, but financial prudence still applies.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
                border = BorderStroke(1.dp, SuSagiColors.Border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SuSagiColors.RiskLow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isHindi)
                            "ध्यान दें: पहचान का स्रोत सत्यापित हुआ है। बैंक हस्तांतरण करने से पहले हमेशा राशि की स्वतंत्र रूप से पुष्टि करें।"
                        else
                            "Notice: Identity attribution was confirmed. Always verify transfer amounts and account numbers independently before sending money.",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            PrimarySafetyAction(
                text = if (isHindi) "कॉल पर वापस जाएं" else "Return to Call",
                onClick = onReturnToCall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RejectedStateCard(
    uiModel: RequesterVerificationUiModel,
    onEndCall: () -> Unit,
    isHindi: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(2.dp, SuSagiColors.RiskCritical)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "पहचान सत्यापित नहीं हो सकी" else "Identity could not be verified",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.RiskCritical,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                RiskBadge(
                    level = SuSagiRiskLevel.CRITICAL,
                    customLabel = if (isHindi) "अस्वीकृत" else "Rejected",
                    size = RiskBadgeSize.Small
                )
            }

            Text(
                text = if (isHindi)
                    "${uiModel.guardianName} का कहना है कि यह अनुरोध उनकी तरफ से नहीं है।"
                else
                    "${uiModel.guardianName} says this request is not from them.",
                style = SuSagiTheme.typography.bodyLarge,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.card,
                colors = CardDefaults.cardColors(containerColor = SuSagiColors.RiskCritical.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, SuSagiColors.RiskCriticalBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = SuSagiColors.RiskCritical,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isHindi)
                            "पैसे न भेजें और संवेदनशील जानकारी साझा न करें।"
                        else
                            "Do not send money or share sensitive information.",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.RiskCritical,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            PrimarySafetyAction(
                text = if (isHindi) "कॉल तुरंत समाप्त करें" else "End Call Now",
                onClick = onEndCall,
                icon = Icons.Default.CallEnd,
                isCritical = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExpiredStateCard(
    uiModel: RequesterVerificationUiModel,
    onRetry: () -> Unit,
    onReturnToCall: () -> Unit,
    isHindi: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.dp, SuSagiColors.RiskHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "सत्यापन समय समाप्त" else "Request Timed Out",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                RiskBadge(
                    level = SuSagiRiskLevel.HIGH,
                    customLabel = if (isHindi) "समय समाप्त" else "Expired",
                    size = RiskBadgeSize.Small
                )
            }

            Text(
                text = if (isHindi)
                    "${uiModel.guardianName} से समय पर कोई प्रतिक्रिया प्राप्त नहीं हुई। सावधानी बरतें और कोई संवेदनशील कदम न उठाएं।"
                else
                    "No response was received from ${uiModel.guardianName} within the verification window. Treat this call with caution.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 22.sp
            )

            SecondarySafetyAction(
                text = if (isHindi) "कॉल पर वापस जाएं" else "Return to Call",
                onClick = onReturnToCall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UnavailableStateCard(
    uiModel: RequesterVerificationUiModel,
    onReturnToCall: () -> Unit,
    isHindi: Boolean
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "पहचान सत्यापन अभी अनुपलब्ध है" else "Identity verification is unavailable right now.",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                RiskBadge(
                    level = SuSagiRiskLevel.CAUTION,
                    customLabel = if (isHindi) "अनुपलब्ध" else "Unavailable",
                    size = RiskBadgeSize.Small
                )
            }

            Text(
                text = if (isHindi)
                    "आगे बढ़ने से पहले उस व्यक्ति से स्वतंत्र रूप से संपर्क करें।"
                else
                    "Contact the person independently before continuing.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 22.sp
            )

            SecondarySafetyAction(
                text = if (isHindi) "कॉल पर वापस जाएं" else "Return to Call",
                onClick = onReturnToCall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS — ALL 6 LIFECYCLE STATES
// ============================================================================

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Requester — 1. READY (Unlinked)", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun RequesterReadyPreview() {
    SuSagiTheme {
        IdentityVerificationRequesterScreen(
            uiModel = RequesterVerificationUiModel(
                callerName = "+91 99887 76655",
                callerNumber = "Unknown Caller (Claims: Mom)",
                guardianName = "Mom",
                guardianNumber = "+91 98765 43210",
                status = VerificationStatus.READY,
                isPlatformIntegrated = false
            ),
            onSendVerification = {},
            onCancelVerification = {},
            onEndCall = {},
            onReturnToCall = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Requester — 2. PENDING", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun RequesterPendingPreview() {
    SuSagiTheme {
        IdentityVerificationRequesterScreen(
            uiModel = RequesterVerificationUiModel(
                callerName = "+91 99887 76655",
                callerNumber = "Unknown Caller (Claims: Mom)",
                guardianName = "Mom",
                guardianNumber = "+91 98765 43210",
                status = VerificationStatus.PENDING,
                remainingSeconds = 38,
                isPlatformIntegrated = true
            ),
            onSendVerification = {},
            onCancelVerification = {},
            onEndCall = {},
            onReturnToCall = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Requester — 3. VERIFIED", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun RequesterVerifiedPreview() {
    SuSagiTheme {
        IdentityVerificationRequesterScreen(
            uiModel = RequesterVerificationUiModel(
                callerName = "+91 99887 76655",
                callerNumber = "Verified Caller",
                guardianName = "Mom",
                guardianNumber = "+91 98765 43210",
                status = VerificationStatus.VERIFIED,
                isPlatformIntegrated = true
            ),
            onSendVerification = {},
            onCancelVerification = {},
            onEndCall = {},
            onReturnToCall = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Requester — 4. REJECTED", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun RequesterRejectedPreview() {
    SuSagiTheme {
        IdentityVerificationRequesterScreen(
            uiModel = RequesterVerificationUiModel(
                callerName = "+91 99887 76655",
                callerNumber = "Unknown Caller (Claims: Mom)",
                guardianName = "Mom",
                guardianNumber = "+91 98765 43210",
                status = VerificationStatus.REJECTED,
                isPlatformIntegrated = true
            ),
            onSendVerification = {},
            onCancelVerification = {},
            onEndCall = {},
            onReturnToCall = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Requester — 5. EXPIRED", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun RequesterExpiredPreview() {
    SuSagiTheme {
        IdentityVerificationRequesterScreen(
            uiModel = RequesterVerificationUiModel(
                callerName = "+91 99887 76655",
                callerNumber = "Unknown Caller (Claims: Mom)",
                guardianName = "Mom",
                guardianNumber = "+91 98765 43210",
                status = VerificationStatus.EXPIRED,
                isPlatformIntegrated = true
            ),
            onSendVerification = {},
            onCancelVerification = {},
            onEndCall = {},
            onReturnToCall = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Requester — 6. UNAVAILABLE", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun RequesterUnavailablePreview() {
    SuSagiTheme {
        IdentityVerificationRequesterScreen(
            uiModel = RequesterVerificationUiModel(
                callerName = "+91 99887 76655",
                callerNumber = "Unknown Caller (Claims: Mom)",
                guardianName = "Mom",
                guardianNumber = "+91 98765 43210",
                status = VerificationStatus.UNAVAILABLE,
                isPlatformIntegrated = false
            ),
            onSendVerification = {},
            onCancelVerification = {},
            onEndCall = {},
            onReturnToCall = {}
        )
    }
}
