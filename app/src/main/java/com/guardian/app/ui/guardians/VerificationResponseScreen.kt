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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.device.DeviceIdentityStore
import com.guardian.app.network.PlatformApiClient
import com.guardian.app.ui.components.ErrorState
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
import com.guardian.app.verification.VerificationSession
import com.guardian.app.verification.VerificationStatus

/**
 * Phone B: Identity Verification Response Screen
 *
 * Full lifecycle response flow for trusted contacts receiving out-of-band identity challenges.
 * Starts from canonical session ID, fetches authoritative VerificationSession, and renders:
 * - Calm loading indicator (no unverified metadata)
 * - Canonical details (claimed identity, requested action, summary)
 * - Two unambiguous decisions ("Yes, this is from me", "No, this is not from me")
 * - In-flight submission duplicate guard (buttons disabled)
 * - First-terminal-state-wins terminal view (VERIFIED, REJECTED, EXPIRED, UNAVAILABLE)
 * - Safe exit / dismiss navigation
 */
@Composable
fun VerificationResponseScreen(
    sessionId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false,
    viewModel: VerificationResponseViewModel = run {
        val context = LocalContext.current.applicationContext
        remember(sessionId) {
            VerificationResponseViewModel(
                apiClient = PlatformApiClient(),
                deviceIdProvider = { DeviceIdentityStore.getOrCreateDeviceId(context) }
            )
        }
    }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    VerificationResponseScreenContent(
        uiState = uiState,
        onConfirmYes = { viewModel.respond(VerificationStatus.VERIFIED) },
        onRejectNo = { viewModel.respond(VerificationStatus.REJECTED) },
        onRetry = { viewModel.retry() },
        onClose = onDismiss,
        modifier = modifier,
        isHindi = isHindi
    )
}

@Composable
fun VerificationResponseScreenContent(
    uiState: VerificationResponseUiState,
    onConfirmYes: () -> Unit,
    onRejectNo: () -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit,
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
            subtitle = if (isHindi) "तत्काल सत्यापन अनुरोध" else "Immediate Verification Request",
            onBack = onClose,
            backContentDescription = if (isHindi) "सुसागी पर वापस जाएँ" else "Back to SuSagi"
        )

        when (uiState) {
            is VerificationResponseUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SuSagiColors.Brand,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (isHindi) "सत्यापन अनुरोध लोड हो रहा है…" else "Loading verification request…",
                            style = SuSagiTheme.typography.bodyLarge,
                            color = SuSagiColors.TextSecondary
                        )
                    }
                }
            }

            is VerificationResponseUiState.Error -> {
                ErrorState(
                    title = if (isHindi) "सत्यापन अनुरोध लोड नहीं हो सका" else "Unable to Load Identity Check",
                    description = uiState.userFacingMessage,
                    retryActionLabel = if (isHindi) "पुनः प्रयास करें" else "Try Again",
                    onRetry = if (uiState.retryAllowed) onRetry else null,
                    secondaryActionLabel = if (isHindi) "बंद करें" else "Close",
                    onSecondaryAction = onClose,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is VerificationResponseUiState.Unavailable -> {
                ErrorState(
                    title = if (isHindi) "पहचान जांच अनुपलब्ध है" else "Identity Check Unavailable",
                    description = uiState.message,
                    secondaryActionLabel = if (isHindi) "बंद करें" else "Close",
                    onSecondaryAction = onClose,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is VerificationResponseUiState.Loaded,
            is VerificationResponseUiState.Submitting -> {
                val session = when (uiState) {
                    is VerificationResponseUiState.Loaded -> uiState.session
                    is VerificationResponseUiState.Submitting -> uiState.session
                    else -> return
                }
                val isSubmitting = uiState is VerificationResponseUiState.Submitting
                val submittingResponse = (uiState as? VerificationResponseUiState.Submitting)?.selectedResponse

                val effectiveStatus = if (session.isExpired()) {
                    VerificationStatus.EXPIRED
                } else {
                    session.status
                }
                val isTerminal = effectiveStatus.isTerminal

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(SuSagiSpacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Alert Header Card
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
                                    text = if (isHindi) "सत्यापन अनुरोध" else "Identity Check",
                                    style = SuSagiTheme.typography.caption,
                                    color = SuSagiColors.TextMuted
                                )
                                Text(
                                    text = if (session.claimedIdentity.isNotBlank()) session.claimedIdentity else "Verification Request",
                                    style = SuSagiTheme.typography.title,
                                    color = SuSagiColors.TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isHindi)
                                        "एक संपर्क पूछ रहा है कि क्या यह अनुरोध वास्तव में आपसे आया है।"
                                    else
                                        "A contact is asking whether this sensitive request really came from you.",
                                    style = SuSagiTheme.typography.caption,
                                    color = SuSagiColors.TextSecondary
                                )
                            }

                            when (effectiveStatus) {
                                VerificationStatus.PENDING -> {
                                    RiskBadge(
                                        level = SuSagiRiskLevel.HIGH,
                                        customLabel = if (isHindi) "तत्काल" else "Urgent",
                                        size = RiskBadgeSize.Small
                                    )
                                }
                                VerificationStatus.VERIFIED -> {
                                    RiskBadge(
                                        level = SuSagiRiskLevel.LOW,
                                        customLabel = if (isHindi) "पुष्ट" else "Confirmed",
                                        size = RiskBadgeSize.Small
                                    )
                                }
                                VerificationStatus.REJECTED -> {
                                    RiskBadge(
                                        level = SuSagiRiskLevel.CRITICAL,
                                        customLabel = if (isHindi) "अस्वीकृत" else "Rejected",
                                        size = RiskBadgeSize.Small
                                    )
                                }
                                VerificationStatus.EXPIRED -> {
                                    RiskBadge(
                                        level = SuSagiRiskLevel.CAUTION,
                                        customLabel = if (isHindi) "समाप्त" else "Expired",
                                        size = RiskBadgeSize.Small
                                    )
                                }
                                VerificationStatus.UNAVAILABLE -> {
                                    RiskBadge(
                                        level = SuSagiRiskLevel.CAUTION,
                                        customLabel = if (isHindi) "अनुपलब्ध" else "Unavailable",
                                        size = RiskBadgeSize.Small
                                    )
                                }
                            }
                        }
                    }

                    // Canonical Session Details Card
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
                                text = if (isHindi) "अनुरोध विवरण" else "Request & Context",
                                style = SuSagiTheme.typography.caption,
                                color = SuSagiColors.TextMuted
                            )

                            if (session.claimedIdentity.isNotBlank()) {
                                Text(
                                    text = if (isHindi)
                                        "दावा की गई पहचान: ${session.claimedIdentity}"
                                    else
                                        "Claimed Identity: ${session.claimedIdentity}",
                                    style = SuSagiTheme.typography.bodyLarge,
                                    color = SuSagiColors.TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (session.requestedAction.isNotBlank()) {
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
                                            text = session.requestedAction,
                                            style = SuSagiTheme.typography.title,
                                            color = SuSagiColors.TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (session.requestSummary.isNotBlank()) {
                                Text(
                                    text = session.requestSummary,
                                    style = SuSagiTheme.typography.bodyMedium,
                                    color = SuSagiColors.TextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Decision / Status Section
                    if (!isTerminal) {
                        // Central Decision Hero Card
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
                                        "क्या यह संवेदनशील अनुरोध वास्तव में आपसे आया है?"
                                    else
                                        "Did this sensitive request really come from you?",
                                    style = SuSagiTheme.typography.title,
                                    color = SuSagiColors.TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp,
                                    modifier = Modifier.semantics { heading() }
                                )

                                // Decision Button 1: Yes, this is from me
                                PrimarySafetyAction(
                                    text = if (isHindi) "हाँ, यह मैं हूँ" else "Yes, this is from me",
                                    onClick = onConfirmYes,
                                    icon = Icons.Default.Check,
                                    enabled = !isSubmitting,
                                    loading = isSubmitting && submittingResponse == VerificationStatus.VERIFIED,
                                    height = 52.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Decision Button 2: No, this is not from me
                                PrimarySafetyAction(
                                    text = if (isHindi) "नहीं, मैं नहीं हूँ" else "No, this is not from me",
                                    onClick = onRejectNo,
                                    icon = Icons.Default.Close,
                                    isCritical = true,
                                    enabled = !isSubmitting,
                                    loading = isSubmitting && submittingResponse == VerificationStatus.REJECTED,
                                    height = 52.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // Terminal Status Card (No active Yes/No buttons)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = SuSagiShape.card,
                            colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
                            border = BorderStroke(
                                1.dp,
                                when (effectiveStatus) {
                                    VerificationStatus.VERIFIED -> SuSagiColors.Brand
                                    VerificationStatus.REJECTED -> SuSagiColors.RiskCritical
                                    else -> SuSagiColors.Border
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val (statusIcon, statusTitle, statusSupporting) = when (effectiveStatus) {
                                    VerificationStatus.VERIFIED -> Triple(
                                        Icons.Default.CheckCircle,
                                        if (isHindi) "आपने पुष्टि की है कि यह अनुरोध आपसे आया है।" else "You confirmed that this request is from you.",
                                        if (isHindi) "पहचान की पुष्टि का मतलब यह नहीं है कि भुगतान या लेनदेन सुरक्षित है।" else "Identity confirmation does not by itself mean a payment or transaction is safe."
                                    )
                                    VerificationStatus.REJECTED -> Triple(
                                        Icons.Default.HighlightOff,
                                        if (isHindi) "आपने कहा कि यह अनुरोध आपसे नहीं आया है।" else "You said this request is not from you.",
                                        if (isHindi) "सुरक्षित उपयोगकर्ता को रुकना चाहिए और आपसे किसी अन्य तरीके से संपर्क करना चाहिए।" else "The protected user should stop and contact you another way."
                                    )
                                    VerificationStatus.EXPIRED -> Triple(
                                        Icons.Default.HourglassEmpty,
                                        if (isHindi) "यह पहचान जांच समाप्त हो गई है।" else "This identity check has expired.",
                                        if (isHindi) "सत्यापन विंडो बिना किसी प्रतिक्रिया के समाप्त हो गई।" else "The verification window elapsed without a response."
                                    )
                                    else -> Triple(
                                        Icons.Default.Security,
                                        if (isHindi) "यह पहचान जांच अनुपलब्ध है।" else "This identity check is unavailable.",
                                        if (isHindi) "अनुरोधित सत्यापन सत्र अब उपलब्ध नहीं है।" else "The requested verification session is no longer reachable."
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = when (effectiveStatus) {
                                            VerificationStatus.VERIFIED -> SuSagiColors.Brand
                                            VerificationStatus.REJECTED -> SuSagiColors.RiskCritical
                                            else -> SuSagiColors.TextMuted
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = statusTitle,
                                        style = SuSagiTheme.typography.title,
                                        color = SuSagiColors.TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = statusSupporting,
                                    style = SuSagiTheme.typography.bodyMedium,
                                    color = SuSagiColors.TextSecondary,
                                    lineHeight = 20.sp
                                )

                                Spacer(Modifier.height(8.dp))

                                SecondarySafetyAction(
                                    text = if (isHindi) "सुसागी पर वापस जाएँ" else "Back to SuSagi",
                                    onClick = onClose,
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 48.dp
                                )
                            }
                        }
                    }

                    // Security note footer
                    Text(
                        text = if (isHindi)
                            "आपकी प्रतिक्रिया तुरंत सुसागी सुरक्षित चैनल के माध्यम से भेजी जाएगी ताकि आपके संपर्क को प्रतिरूपण घोटालों से बचाया जा सके।"
                        else
                            "Your response is transmitted securely to help protect your contact from real-time impersonation scams.",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS (UI PREVIEW DATA — NOT RUNTIME DATA)
// ============================================================================

@Preview(name = "Responder — Canonical Pending ₹25,000", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun VerificationResponseScreenPendingPreview() {
    SuSagiTheme {
        VerificationResponseScreenContent(
            uiState = VerificationResponseUiState.Loaded(
                VerificationSession(
                    id = "sess_preview_1",
                    claimedIdentity = "State Bank Manager",
                    requestedAction = "₹25,000 Urgent Wire Transfer",
                    requestSummary = "Caller claimed immediate funds transfer is required to avoid account block.",
                    status = VerificationStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + 60000L
                )
            ),
            onConfirmYes = {},
            onRejectNo = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview(name = "Responder — Terminal Verified", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun VerificationResponseScreenVerifiedPreview() {
    SuSagiTheme {
        VerificationResponseScreenContent(
            uiState = VerificationResponseUiState.Loaded(
                VerificationSession(
                    id = "sess_preview_2",
                    claimedIdentity = "State Bank Manager",
                    requestedAction = "₹25,000 Urgent Wire Transfer",
                    requestSummary = "Caller claimed immediate funds transfer is required to avoid account block.",
                    status = VerificationStatus.VERIFIED,
                    createdAt = System.currentTimeMillis() - 100000L,
                    expiresAt = System.currentTimeMillis() + 60000L
                )
            ),
            onConfirmYes = {},
            onRejectNo = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview(name = "Responder — Terminal Rejected", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun VerificationResponseScreenRejectedPreview() {
    SuSagiTheme {
        VerificationResponseScreenContent(
            uiState = VerificationResponseUiState.Loaded(
                VerificationSession(
                    id = "sess_preview_3",
                    claimedIdentity = "Cyber Crime Officer",
                    requestedAction = "Share OTP for verification",
                    requestSummary = "Caller threatened immediate arrest if OTP is not shared.",
                    status = VerificationStatus.REJECTED,
                    createdAt = System.currentTimeMillis() - 100000L,
                    expiresAt = System.currentTimeMillis() + 60000L
                )
            ),
            onConfirmYes = {},
            onRejectNo = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview(name = "Responder — Terminal Expired", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun VerificationResponseScreenExpiredPreview() {
    SuSagiTheme {
        VerificationResponseScreenContent(
            uiState = VerificationResponseUiState.Loaded(
                VerificationSession(
                    id = "sess_preview_4",
                    claimedIdentity = "Police Officer",
                    requestedAction = "Send location",
                    requestSummary = "Expired challenge",
                    status = VerificationStatus.EXPIRED,
                    createdAt = System.currentTimeMillis() - 200000L,
                    expiresAt = System.currentTimeMillis() - 100000L
                )
            ),
            onConfirmYes = {},
            onRejectNo = {},
            onRetry = {},
            onClose = {}
        )
    }
}
