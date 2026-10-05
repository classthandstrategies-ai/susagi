package com.guardian.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.EmptyState
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Production SuSagi Home Screen
 *
 * Implements the core product purpose:
 * 1. Is SuSagi protecting me? -> Clean dominant status hero
 * 2. Is anything requiring my attention? -> Capability health checklist
 * 3. What useful action can I take right now? -> Quick defensive actions
 *
 * Visual principles:
 * - Trustworthy, calm, banking-grade aesthetic
 * - No hacker UI, no AI-demo telemetry, no false statistics
 * - Enforces minimum 48dp touch targets and accessible contrast
 */
@Composable
fun SuSagiHomeScreen(
    uiState: HomeUiState,
    onToggleProtection: (Boolean) -> Unit,
    onOpenCallRisk: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenLinkCheck: () -> Unit,
    onOpenCallHistory: () -> Unit,
    onNavigateToGuardians: () -> Unit,
    onNavigateToProtect: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SuSagiSpacing.screenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ----------------------------------------------------
        // 1. DOMINANT PROTECTION STATUS HERO
        // ----------------------------------------------------
        HomeProtectionStatusHero(
            status = uiState.overallStatus,
            isProtectionEnabled = uiState.isProtectionEnabled,
            onToggleProtection = onToggleProtection,
            onConfigureShields = onNavigateToProtect,
            isHindi = isHindi
        )

        // ----------------------------------------------------
        // 2. PROTECTION CAPABILITIES CHECKLIST
        // ----------------------------------------------------
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isHindi) "सुरक्षा क्षमताएं" else "Protection Capabilities",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHindi) "प्रबंधित करें" else "Manage",
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.BrandLight,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToProtect)
                        .padding(4.dp)
                )
            }

            uiState.capabilities.forEach { capability ->
                CapabilityRow(
                    capability = capability,
                    isHindi = isHindi,
                    onClick = onNavigateToProtect
                )
            }
        }

        // ----------------------------------------------------
        // 3. QUICK ACTIONS
        // ----------------------------------------------------
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (isHindi) "त्वरित सुरक्षा क्रियाएं" else "Quick Actions",
                style = SuSagiTheme.typography.title,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = if (isHindi) "कॉल जांचें" else "Check Call",
                    subtitle = if (isHindi) "लाइव सुरक्षा" else "Live Defense",
                    icon = Icons.Default.Phone,
                    onClick = onOpenCallRisk,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = if (isHindi) "क्यूआर स्कैन" else "Scan QR",
                    subtitle = if (isHindi) "धोखाधड़ी पहचानें" else "Verify Codes",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = onOpenQrScanner,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = if (isHindi) "लिंक जांचें" else "Verify Link",
                    subtitle = if (isHindi) "यूआरएल सुरक्षा" else "Phishing Check",
                    icon = Icons.Default.Link,
                    onClick = onOpenLinkCheck,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = if (isHindi) "गतिविधि देखें" else "View Activity",
                    subtitle = if (isHindi) "गतिविधि विवरण" else "Activity",
                    icon = Icons.Default.History,
                    onClick = onOpenCallHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ----------------------------------------------------
        // 4. RECENT SAFETY ACTIVITY
        // ----------------------------------------------------
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isHindi) "हाल की सुरक्षा गतिविधि" else "Recent Safety Activity",
                style = SuSagiTheme.typography.title,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            if (uiState.recentActivities.isEmpty()) {
                EmptyState(
                    title = if (isHindi) "हाल ही में कोई खतरा नहीं मिला" else "No Recent Threats",
                    description = if (isHindi)
                        "सुसागी आपके आने वाले कॉल और संदेशों की निगरानी कर रहा है। कोई भी संदिग्ध गतिविधि यहां दिखाई देगी।"
                    else "SuSagi is monitoring incoming calls and messages. Any suspicious activity will appear here.",
                    icon = Icons.Default.Shield
                )
            } else {
                uiState.recentActivities.forEach { activity ->
                    RecentActivityRow(activity = activity)
                }
            }
        }

        // ----------------------------------------------------
        // 5. GUARDIAN CIRCLE ENTRY
        // ----------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = SuSagiShape.card,
            colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
            border = BorderStroke(1.dp, SuSagiColors.Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SuSagiSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SuSagiColors.BrandSoft,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = SuSagiColors.Brand,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "संरक्षक नेटवर्क (Guardian Circle)" else "Guardian Circle",
                            style = SuSagiTheme.typography.title,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isHindi) "विश्वसनीय संपर्क सत्यापन" else "Trusted emergency contacts",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                    }
                }

                Text(
                    text = if (isHindi)
                        "विश्वसनीय लोग असामान्य अनुरोधों को सत्यापित करने में आपकी सहायता कर सकते हैं।"
                    else "People you trust can help verify unusual requests.",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 22.sp
                )

                SecondarySafetyAction(
                    text = if (isHindi) "संरक्षक देखें" else "View Guardians",
                    onClick = onNavigateToGuardians,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.fillMaxWidth(),
                    height = 46.dp
                )
            }
        }
    }
}

// ============================================================================
// COMPONENT HELPERS
// ============================================================================

@Composable
private fun HomeProtectionStatusHero(
    status: HomeProtectionStatus,
    isProtectionEnabled: Boolean,
    onToggleProtection: (Boolean) -> Unit,
    onConfigureShields: () -> Unit,
    isHindi: Boolean
) {
    val (statusTitle, statusDesc, statusColor, statusBg, icon) = when (status) {
        HomeProtectionStatus.ACTIVE -> StatusHeroConfig(
            title = if (isHindi) status.hindiTitle else status.title,
            desc = if (isHindi) status.hindiDescription else status.description,
            color = SuSagiColors.RiskLow,
            bg = SuSagiColors.RiskLowSoft,
            icon = Icons.Default.Shield
        )
        HomeProtectionStatus.PARTIALLY_PROTECTED -> StatusHeroConfig(
            title = if (isHindi) status.hindiTitle else status.title,
            desc = if (isHindi) status.hindiDescription else status.description,
            color = SuSagiColors.RiskCaution,
            bg = SuSagiColors.RiskCautionSoft,
            icon = Icons.Default.WarningAmber
        )
        HomeProtectionStatus.ATTENTION_REQUIRED -> StatusHeroConfig(
            title = if (isHindi) status.hindiTitle else status.title,
            desc = if (isHindi) status.hindiDescription else status.description,
            color = SuSagiColors.RiskHigh,
            bg = SuSagiColors.RiskHighSoft,
            icon = Icons.Default.WarningAmber
        )
        HomeProtectionStatus.PAUSED -> StatusHeroConfig(
            title = if (isHindi) status.hindiTitle else status.title,
            desc = if (isHindi) status.hindiDescription else status.description,
            color = SuSagiColors.TextMuted,
            bg = SuSagiColors.SurfaceElevated,
            icon = Icons.Default.PauseCircle
        )
        HomeProtectionStatus.OFFLINE -> StatusHeroConfig(
            title = if (isHindi) status.hindiTitle else status.title,
            desc = if (isHindi) status.hindiDescription else status.description,
            color = SuSagiColors.BrandLight,
            bg = SuSagiColors.BrandSoft,
            icon = Icons.Default.Security
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = statusBg,
                    modifier = Modifier.size(46.dp)
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        style = SuSagiTheme.typography.headline,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isProtectionEnabled) "Real-time scam screening active" else "Standby mode",
                        style = SuSagiTheme.typography.caption,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = statusDesc,
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 22.sp
            )

            // Primary control action
            if (!isProtectionEnabled) {
                PrimarySafetyAction(
                    text = if (isHindi) "सुरक्षा चालू करें" else "Turn Protection On",
                    onClick = { onToggleProtection(true) },
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )
            } else if (status == HomeProtectionStatus.PARTIALLY_PROTECTED || status == HomeProtectionStatus.ATTENTION_REQUIRED) {
                PrimarySafetyAction(
                    text = if (isHindi) "शील्ड कॉन्फ़िगर करें" else "Enable All Shields",
                    onClick = onConfigureShields,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )
            } else {
                SecondarySafetyAction(
                    text = if (isHindi) "सुरक्षा रोकें" else "Pause Protection",
                    onClick = { onToggleProtection(false) },
                    modifier = Modifier.fillMaxWidth(),
                    height = 44.dp
                )
            }
        }
    }
}

private data class StatusHeroConfig(
    val title: String,
    val desc: String,
    val color: androidx.compose.ui.graphics.Color,
    val bg: androidx.compose.ui.graphics.Color,
    val icon: ImageVector
)

@Composable
private fun CapabilityRow(
    capability: ProtectionCapabilityItem,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.dp, SuSagiColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .defaultMinSize(minHeight = SuSagiSpacing.minTouchTarget),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (capability.id) {
                "calls" -> Icons.Default.Phone
                "messages" -> Icons.AutoMirrored.Filled.Message
                else -> Icons.Default.Link
            }

            Surface(
                shape = CircleShape,
                color = if (capability.isProtected) SuSagiColors.RiskLowSoft else SuSagiColors.RiskCautionSoft,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (capability.isProtected) SuSagiColors.RiskLow else SuSagiColors.RiskCaution,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isHindi) capability.hindiTitle else capability.title,
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isHindi) capability.hindiExplanation else capability.explanation,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            RiskBadge(
                level = if (capability.isProtected) SuSagiRiskLevel.LOW else SuSagiRiskLevel.CAUTION,
                customLabel = if (capability.isProtected) "Protected" else "Off",
                showIcon = false,
                size = RiskBadgeSize.Small
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = SuSagiSpacing.minTouchTarget)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "$title, $subtitle"
            },
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SuSagiColors.BrandSoft,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SuSagiColors.Brand,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = title,
                style = SuSagiTheme.typography.title,
                color = SuSagiColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Text(
                text = subtitle,
                style = SuSagiTheme.typography.caption,
                color = SuSagiColors.TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentActivityRow(
    activity: HomeRecentActivity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.dp, SuSagiColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = activity.description,
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = activity.timestamp,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextMuted
                )
            }

            Spacer(Modifier.width(8.dp))

            RiskBadge(
                level = activity.riskLevel,
                size = RiskBadgeSize.Small
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS (UI PREVIEW DATA — NOT RUNTIME DATA)
// ============================================================================

@Preview(name = "Home - Active Protection", showBackground = true)
@Composable
private fun PreviewHomeActive() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = HomeUiState(
            userName = "Sample User",
            overallStatus = HomeProtectionStatus.ACTIVE,
            isProtectionEnabled = true,
            isPhoneProtected = true,
            isMessageProtected = true,
            isLinkProtected = true,
            capabilities = listOf(
                ProtectionCapabilityItem("calls", "Call Protection", "कॉल सुरक्षा", true, "Live analysis for impersonation and urgency deception", ""),
                ProtectionCapabilityItem("messages", "Message Protection", "संदेश सुरक्षा", true, "Detects fraudulent OTP requests and fake alerts", ""),
                ProtectionCapabilityItem("links", "Link Protection", "लिंक सुरक्षा", true, "Checks suspicious web links before you open them", "")
            ),
            recentActivities = emptyList()
        )
        SuSagiHomeScreen(
            uiState = previewState,
            onToggleProtection = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {},
            onOpenLinkCheck = {},
            onOpenCallHistory = {},
            onNavigateToGuardians = {},
            onNavigateToProtect = {}
        )
    }
}

@Preview(name = "Home - Attention Required", showBackground = true)
@Composable
private fun PreviewHomeAttentionRequired() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = HomeUiState(
            userName = "Sample User",
            overallStatus = HomeProtectionStatus.ATTENTION_REQUIRED,
            isProtectionEnabled = true,
            isPhoneProtected = false,
            isMessageProtected = true,
            isLinkProtected = true,
            capabilities = listOf(
                ProtectionCapabilityItem("calls", "Call Protection", "कॉल सुरक्षा", false, "Permission required to screen incoming calls", ""),
                ProtectionCapabilityItem("messages", "Message Protection", "संदेश सुरक्षा", true, "Detects fraudulent OTP requests and fake alerts", ""),
                ProtectionCapabilityItem("links", "Link Protection", "लिंक सुरक्षा", true, "Checks suspicious web links before you open them", "")
            ),
            recentActivities = emptyList()
        )
        SuSagiHomeScreen(
            uiState = previewState,
            onToggleProtection = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {},
            onOpenLinkCheck = {},
            onOpenCallHistory = {},
            onNavigateToGuardians = {},
            onNavigateToProtect = {}
        )
    }
}

@Preview(name = "Home - Protection Paused", showBackground = true)
@Composable
private fun PreviewHomePaused() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = HomeUiState(
            userName = "Sample User",
            overallStatus = HomeProtectionStatus.PAUSED,
            isProtectionEnabled = false,
            isPhoneProtected = false,
            isMessageProtected = false,
            isLinkProtected = false,
            capabilities = listOf(
                ProtectionCapabilityItem("calls", "Call Protection", "कॉल सुरक्षा", false, "Call screening is paused", ""),
                ProtectionCapabilityItem("messages", "Message Protection", "संदेश सुरक्षा", false, "Message monitoring is paused", ""),
                ProtectionCapabilityItem("links", "Link Protection", "लिंक सुरक्षा", false, "Link checking is paused", "")
            ),
            recentActivities = emptyList()
        )
        SuSagiHomeScreen(
            uiState = previewState,
            onToggleProtection = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {},
            onOpenLinkCheck = {},
            onOpenCallHistory = {},
            onNavigateToGuardians = {},
            onNavigateToProtect = {}
        )
    }
}

@Preview(name = "Home - No Recent Activity", showBackground = true)
@Composable
private fun PreviewHomeNoRecentActivity() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = HomeUiState(
            userName = "Sample User",
            overallStatus = HomeProtectionStatus.ACTIVE,
            isProtectionEnabled = true,
            isPhoneProtected = true,
            isMessageProtected = true,
            isLinkProtected = true,
            capabilities = listOf(
                ProtectionCapabilityItem("calls", "Call Protection", "कॉल सुरक्षा", true, "Live analysis for impersonation and urgency deception", "")
            ),
            recentActivities = emptyList()
        )
        SuSagiHomeScreen(
            uiState = previewState,
            onToggleProtection = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {},
            onOpenLinkCheck = {},
            onOpenCallHistory = {},
            onNavigateToGuardians = {},
            onNavigateToProtect = {}
        )
    }
}

@Preview(name = "Home - Large Font Scaling", fontScale = 1.3f, showBackground = true)
@Composable
private fun PreviewHomeLargeFont() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        val previewState = HomeUiState(
            userName = "Sample User",
            overallStatus = HomeProtectionStatus.ACTIVE,
            isProtectionEnabled = true,
            isPhoneProtected = true,
            isMessageProtected = true,
            isLinkProtected = true,
            capabilities = listOf(
                ProtectionCapabilityItem("calls", "Call Protection", "कॉल सुरक्षा", true, "Live analysis for impersonation and urgency deception", ""),
                ProtectionCapabilityItem("messages", "Message Protection", "संदेश सुरक्षा", true, "Detects fraudulent OTP requests and fake alerts", "")
            ),
            recentActivities = emptyList()
        )
        SuSagiHomeScreen(
            uiState = previewState,
            onToggleProtection = {},
            onOpenCallRisk = {},
            onOpenQrScanner = {},
            onOpenLinkCheck = {},
            onOpenCallHistory = {},
            onNavigateToGuardians = {},
            onNavigateToProtect = {}
        )
    }
}
