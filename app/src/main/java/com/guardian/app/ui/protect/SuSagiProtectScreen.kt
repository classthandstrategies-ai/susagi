package com.guardian.app.ui.protect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.design.ProtectionState
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * SuSagiProtectScreen
 *
 * Production Protect destination for SuSagi.
 * Transparently presents all defense capabilities, availability status,
 * contextual permission guidance, and direct actions.
 */
@Composable
fun SuSagiProtectScreen(
    uiState: ProtectUiState,
    onPhoneProtectionToggle: (Boolean) -> Unit = {},
    onMessageProtectionToggle: (Boolean) -> Unit = {},
    onOpenCallRisk: () -> Unit = {},
    onOpenQrScanner: () -> Unit = {},
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showLinkModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SuSagiSpacing.screenPadding)
            .padding(
                top = SuSagiSpacing.md,
                bottom = SuSagiSpacing.section
            ),
        verticalArrangement = Arrangement.spacedBy(SuSagiSpacing.section)
    ) {
        // ----------------------------------------------------
        // 1. COMPACT OVERALL STATUS SUMMARY
        // ----------------------------------------------------
        ProtectSummaryHeader(
            state = uiState.overallStatus,
            activeCount = uiState.activeProtectionCount,
            totalCount = uiState.totalProtectionCount,
            isHindi = isHindi
        )

        // ----------------------------------------------------
        // 2. DEFENSE CAPABILITIES
        // ----------------------------------------------------
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isHindi) "सुरक्षा शील्ड्स" else "Active Shields",
                style = SuSagiTheme.typography.title,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            uiState.capabilities.forEach { capability ->
                ProtectionCapabilityCard(
                    capability = capability,
                    onToggle = { isChecked ->
                        when (capability.id) {
                            "calls" -> onPhoneProtectionToggle(isChecked)
                            "messages" -> onMessageProtectionToggle(isChecked)
                        }
                    },
                    onActionClick = {
                        when (capability.id) {
                            "links" -> showLinkModal = true
                            "qr" -> onOpenQrScanner()
                        }
                    },
                    onGrantPermissionClick = {
                        when (capability.id) {
                            "calls" -> onPhoneProtectionToggle(true)
                            "messages" -> onMessageProtectionToggle(true)
                        }
                    },
                    onOpenCallRisk = if (capability.id == "calls") onOpenCallRisk else null,
                    isHindi = isHindi
                )
            }
        }

        // ----------------------------------------------------
        // 3. PRIVACY & ARCHITECTURE GUARANTEE CARD
        // ----------------------------------------------------
        PrivacyArchitectureCard(isHindi = isHindi)
    }

    // Link Verification In-App Modal
    if (showLinkModal) {
        LinkVerificationModal(
            onDismiss = { showLinkModal = false },
            isHindi = isHindi
        )
    }
}

/**
 * ProtectSummaryHeader
 *
 * Compact, informative summary of protection posture (not duplicate hero).
 */
@Composable
private fun ProtectSummaryHeader(
    state: ProtectionState,
    activeCount: Int,
    totalCount: Int,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    val (statusTitle, statusDesc, statusColor, statusBg, icon) = when (state) {
        ProtectionState.ACTIVE -> StatusMeta(
            title = if (isHindi) "कॉल और संदेश सुरक्षा सक्रिय है" else "Call and message protection active",
            desc = if (isHindi)
                "फ़ोन कॉल और संदेश अलर्ट की रीयल-टाइम निगरानी की जा रही है।"
            else
                "Real-time speech analysis and notification screening are actively safeguarding your device.",
            color = SuSagiColors.RiskLow,
            bg = SuSagiColors.RiskLowSoft,
            icon = Icons.Default.Shield
        )
        ProtectionState.ATTENTION_REQUIRED -> StatusMeta(
            title = if (isHindi) "कार्रवाई आवश्यक है" else "Action Required",
            desc = if (isHindi)
                "पूर्ण सुरक्षा सुनिश्चित करने के लिए कुछ अनुमतियां प्रदान करने की आवश्यकता है।"
            else
                "One or more protection shields require permission grants to provide complete scam defense.",
            color = SuSagiColors.RiskHigh,
            bg = SuSagiColors.RiskHighSoft,
            icon = Icons.Default.WarningAmber
        )
        ProtectionState.PAUSED -> StatusMeta(
            title = if (isHindi) "सुरक्षा रोक दी गई है" else "Protection Paused",
            desc = if (isHindi)
                "सक्रिय सेंसर स्टैंडबाय मोड में हैं। सुरक्षा फिर से शुरू करने के लिए शील्ड चालू करें।"
            else
                "Active sensors are in standby. Re-enable shields to resume real-time scam interception.",
            color = SuSagiColors.RiskCaution,
            bg = SuSagiColors.RiskCautionSoft,
            icon = Icons.Default.PauseCircle
        )
        ProtectionState.OFFLINE -> StatusMeta(
            title = if (isHindi) "ऑफलाइन मोड" else "Offline Defense Active",
            desc = if (isHindi)
                "स्थानीय ऑन-डिवाइस नियम बिना इंटरनेट के आपकी सुरक्षा कर रहे हैं।"
            else
                "Local on-device models are analyzing calls and messages without cloud connectivity.",
            color = SuSagiColors.BrandLight,
            bg = SuSagiColors.BrandSoft,
            icon = Icons.Default.Security
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = statusBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusTitle,
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    RiskBadge(
                        level = when (state) {
                            ProtectionState.ACTIVE -> SuSagiRiskLevel.LOW
                            ProtectionState.ATTENTION_REQUIRED -> SuSagiRiskLevel.HIGH
                            ProtectionState.PAUSED -> SuSagiRiskLevel.CAUTION
                            ProtectionState.OFFLINE -> SuSagiRiskLevel.LOW
                        },
                        customLabel = when (state) {
                            ProtectionState.ACTIVE -> if (isHindi) "सक्रिय" else "Active"
                            ProtectionState.ATTENTION_REQUIRED -> if (isHindi) "कार्रवाई आवश्यक" else "Action Needed"
                            ProtectionState.PAUSED -> if (isHindi) "रोक दिया गया" else "Paused"
                            ProtectionState.OFFLINE -> if (isHindi) "ऑफलाइन" else "Offline"
                        },
                        size = RiskBadgeSize.Small
                    )
                }

                Text(
                    text = statusDesc,
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * PrivacyArchitectureCard
 *
 * Banking-grade transparency card highlighting on-device processing and confidentiality.
 */
@Composable
private fun PrivacyArchitectureCard(
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
        border = BorderStroke(1.dp, SuSagiColors.BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SuSagiColors.Brand,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isHindi) "गोपनीयता और डेटा सुरक्षा" else "Privacy & Data Protection",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() }
                )
            }

            Text(
                text = if (isHindi)
                    "सुसागी केवल सक्षम सुरक्षा सुविधाओं के लिए आवश्यक अनुमतियों का उपयोग करता है। आपकी सुरक्षा प्राथमिक उद्देश्य है।"
                else
                    "SuSagi only uses the access needed for enabled protection features. Protecting you from deception is our sole purpose.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

private data class StatusMeta(
    val title: String,
    val desc: String,
    val color: Color,
    val bg: Color,
    val icon: ImageVector
)

// ============================================================================
// COMPOSE PREVIEWS (UI PREVIEW DATA — NOT RUNTIME DATA)
// ============================================================================

@Preview(name = "Protect - All Active", showBackground = true)
@Composable
private fun PreviewProtectAllActive() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = ProtectUiState(
            overallStatus = ProtectionState.ACTIVE,
            activeProtectionCount = 4,
            totalProtectionCount = 4,
            isPhoneProtected = true,
            isMessageProtected = true,
            isLinkProtected = true,
            isQrAvailable = true,
            capabilities = listOf(
                ProtectionCapabilityUiModel(
                    id = "calls",
                    title = "Call Protection",
                    titleHi = "कॉल सुरक्षा",
                    description = "Live speech analysis during phone calls to detect impersonation, digital arrest extortion, and urgent payment pressure.",
                    descriptionHi = "फ़ोन कॉल के दौरान वॉयस विश्लेषण जो प्रतिरूपण और जबरन भुगतान का पता लगाता है।",
                    status = CapabilityStatus.ACTIVE,
                    isEnabled = true,
                    privacyAssurance = "SuSagi only uses the access needed for enabled protection features.",
                    actionType = CapabilityActionType.TOGGLE
                ),
                ProtectionCapabilityUiModel(
                    id = "messages",
                    title = "Message Protection",
                    titleHi = "संदेश सुरक्षा",
                    description = "Screens incoming notifications and SMS for fraudulent payment links, fake bank alerts, and credential-harvesting scams.",
                    descriptionHi = "धोखाधड़ी वाले भुगतान लिंक और नकली बैंक अलर्ट के लिए एसएमएस की जांच करता है।",
                    status = CapabilityStatus.ACTIVE,
                    isEnabled = true,
                    privacyAssurance = "SuSagi only uses notification access to detect known scam patterns.",
                    actionType = CapabilityActionType.TOGGLE
                ),
                ProtectionCapabilityUiModel(
                    id = "links",
                    title = "Link Protection",
                    titleHi = "लिंक सुरक्षा",
                    description = "Verifies web links before opening them to block phishing portals, rogue APK downloads, and spoofed bank logins.",
                    descriptionHi = "फ़िशिंग पोर्टल और नकली बैंक लॉगिन को ब्लॉक करने के लिए वेब लिंक की पुष्टि करता है।",
                    status = CapabilityStatus.AVAILABLE,
                    isEnabled = true,
                    privacyAssurance = "SuSagi only evaluates URLs when you verify them or open external links.",
                    actionType = CapabilityActionType.ACTION_BUTTON,
                    actionLabel = "Verify Link"
                ),
                ProtectionCapabilityUiModel(
                    id = "qr",
                    title = "QR Shield",
                    titleHi = "क्यूआर शील्ड",
                    description = "Scans QR codes to prevent malicious payment redirection, disguised UPI IDs, and dangerous website downloads.",
                    descriptionHi = "दुर्भावनापूर्ण भुगतान पुनर्निर्देशन को रोकने के लिए क्यूआर कोड स्कैन करता है।",
                    status = CapabilityStatus.AVAILABLE,
                    isEnabled = true,
                    privacyAssurance = "Camera access is used only while the scanner is actively open.",
                    actionType = CapabilityActionType.ACTION_BUTTON,
                    actionLabel = "Scan QR"
                )
            )
        )
        SuSagiProtectScreen(
            uiState = previewState,
            onPhoneProtectionToggle = {},
            onMessageProtectionToggle = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {}
        )
    }
}

@Preview(name = "Protect - Needs Attention", showBackground = true)
@Composable
private fun PreviewProtectNeedsAttention() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = ProtectUiState(
            overallStatus = ProtectionState.ATTENTION_REQUIRED,
            activeProtectionCount = 2,
            totalProtectionCount = 4,
            isPhoneProtected = false,
            isMessageProtected = true,
            isLinkProtected = true,
            isQrAvailable = true,
            capabilities = listOf(
                ProtectionCapabilityUiModel(
                    id = "calls",
                    title = "Call Protection",
                    titleHi = "कॉल सुरक्षा",
                    description = "Live speech analysis during phone calls to detect impersonation, digital arrest extortion, and urgent payment pressure.",
                    descriptionHi = "फ़ोन कॉल के दौरान वॉयस विश्लेषण जो प्रतिरूपण और जबरन भुगतान का पता लगाता है।",
                    status = CapabilityStatus.NEEDS_ATTENTION,
                    isEnabled = false,
                    permissionRequiredMessage = "Phone state and notification permissions are required to monitor incoming calls for scam patterns.",
                    privacyAssurance = "SuSagi only uses the access needed for enabled protection features.",
                    actionType = CapabilityActionType.TOGGLE
                ),
                ProtectionCapabilityUiModel(
                    id = "messages",
                    title = "Message Protection",
                    titleHi = "संदेश सुरक्षा",
                    description = "Screens incoming notifications and SMS for fraudulent payment links, fake bank alerts, and credential-harvesting scams.",
                    descriptionHi = "धोखाधड़ी वाले भुगतान लिंक और नकली बैंक अलर्ट के लिए एसएमएस की जांच करता है।",
                    status = CapabilityStatus.ACTIVE,
                    isEnabled = true,
                    privacyAssurance = "Only suspicious financial patterns and URLs are checked.",
                    actionType = CapabilityActionType.TOGGLE
                ),
                ProtectionCapabilityUiModel(
                    id = "links",
                    title = "Link Protection",
                    titleHi = "लिंक सुरक्षा",
                    description = "Verifies web links before opening them to block phishing portals, rogue APK downloads, and spoofed bank logins.",
                    descriptionHi = "फ़िशिंग पोर्टल और नकली बैंक लॉगिन को ब्लॉक करने के लिए वेब लिंक की पुष्टि करता है।",
                    status = CapabilityStatus.ACTIVE,
                    isEnabled = true,
                    privacyAssurance = "Links are evaluated against known malicious domain databases.",
                    actionType = CapabilityActionType.ACTION_BUTTON,
                    actionLabel = "Verify Link"
                ),
                ProtectionCapabilityUiModel(
                    id = "qr",
                    title = "QR Shield",
                    titleHi = "क्यूआर शील्ड",
                    description = "Scans QR codes to prevent malicious payment redirection, disguised UPI IDs, and dangerous website downloads.",
                    descriptionHi = "दुर्भावनापूर्ण भुगतान पुनर्निर्देशन को रोकने के लिए क्यूआर कोड स्कैन करता है।",
                    status = CapabilityStatus.AVAILABLE,
                    isEnabled = true,
                    privacyAssurance = "Camera is used strictly to read the QR pattern.",
                    actionType = CapabilityActionType.ACTION_BUTTON,
                    actionLabel = "Scan QR"
                )
            )
        )
        SuSagiProtectScreen(
            uiState = previewState,
            onPhoneProtectionToggle = {},
            onMessageProtectionToggle = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {}
        )
    }
}

@Preview(name = "Protect - All Off", showBackground = true)
@Composable
private fun PreviewProtectAllOff() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = ProtectUiState(
            overallStatus = ProtectionState.PAUSED,
            activeProtectionCount = 0,
            totalProtectionCount = 4,
            isPhoneProtected = false,
            isMessageProtected = false,
            isLinkProtected = false,
            isQrAvailable = false,
            capabilities = listOf(
                ProtectionCapabilityUiModel(
                    id = "calls",
                    title = "Call Protection",
                    titleHi = "कॉल सुरक्षा",
                    description = "Call screening is paused by user.",
                    descriptionHi = "कॉल स्क्रीनिंग रोक दी गई है।",
                    status = CapabilityStatus.PAUSED,
                    isEnabled = false,
                    actionType = CapabilityActionType.TOGGLE
                ),
                ProtectionCapabilityUiModel(
                    id = "messages",
                    title = "Message Protection",
                    titleHi = "संदेश सुरक्षा",
                    description = "Message monitoring is paused by user.",
                    descriptionHi = "संदेश निगरानी रोक दी गई है।",
                    status = CapabilityStatus.PAUSED,
                    isEnabled = false,
                    actionType = CapabilityActionType.TOGGLE
                )
            )
        )
        SuSagiProtectScreen(
            uiState = previewState,
            onPhoneProtectionToggle = {},
            onMessageProtectionToggle = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {}
        )
    }
}

@Preview(name = "Protect - Large Font Scaling", fontScale = 1.3f, showBackground = true)
@Composable
private fun PreviewProtectLargeFont() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = ProtectUiState(
            overallStatus = ProtectionState.ACTIVE,
            activeProtectionCount = 4,
            totalProtectionCount = 4,
            isPhoneProtected = true,
            isMessageProtected = true,
            isLinkProtected = true,
            isQrAvailable = true,
            capabilities = listOf(
                ProtectionCapabilityUiModel(
                    id = "calls",
                    title = "Call Protection",
                    titleHi = "कॉल सुरक्षा",
                    description = "Live speech analysis during phone calls to detect impersonation, digital arrest extortion, and urgent payment pressure.",
                    descriptionHi = "फ़ोन कॉल के दौरान वॉयस विश्लेषण जो प्रतिरूपण और जबरन भुगतान का पता लगाता है।",
                    status = CapabilityStatus.ACTIVE,
                    isEnabled = true,
                    privacyAssurance = "SuSagi only uses the access needed for enabled protection features.",
                    actionType = CapabilityActionType.TOGGLE
                )
            )
        )
        SuSagiProtectScreen(
            uiState = previewState,
            onPhoneProtectionToggle = {},
            onMessageProtectionToggle = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {}
        )
    }
}

@Preview(name = "Protect - Permission Needed", showBackground = true)
@Composable
private fun PreviewProtectPermissionNeeded() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = ProtectUiState(
            overallStatus = ProtectionState.ATTENTION_REQUIRED,
            activeProtectionCount = 1,
            totalProtectionCount = 4,
            isPhoneProtected = false,
            isMessageProtected = false,
            isLinkProtected = true,
            isQrAvailable = true,
            capabilities = listOf(
                ProtectionCapabilityUiModel(
                    id = "messages",
                    title = "Message Protection",
                    titleHi = "संदेश सुरक्षा",
                    description = "Screens incoming notifications and SMS for fraudulent payment links, fake bank alerts, and credential-harvesting scams.",
                    descriptionHi = "धोखाधड़ी वाले भुगतान लिंक और नकली बैंक अलर्ट के लिए एसएमएस की जांच करता है।",
                    status = CapabilityStatus.NEEDS_ATTENTION,
                    isEnabled = false,
                    permissionRequiredMessage = "Notification access permission is required so SuSagi can screen SMS and app alerts.",
                    permissionRequiredMessageHi = "नोटिफिकेशन एक्सेस अनुमति आवश्यक है ताकि सुसागी एसएमएस और ऐप अलर्ट की जांच कर सके।",
                    privacyAssurance = "Only suspicious financial patterns and URLs are checked; personal messages remain completely private.",
                    actionType = CapabilityActionType.TOGGLE
                )
            )
        )
        SuSagiProtectScreen(
            uiState = previewState,
            onPhoneProtectionToggle = {},
            onMessageProtectionToggle = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {}
        )
    }
}
