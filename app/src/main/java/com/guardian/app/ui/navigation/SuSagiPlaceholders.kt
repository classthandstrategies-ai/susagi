package com.guardian.app.ui.navigation

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.EmptyState
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.ProtectionStatusCard
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.ProtectionState
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

// ============================================================================
// UI SHELL PLACEHOLDERS — INTENTIONAL ARCHITECTURAL SHELLS
// NOTE: Contains no fake functional data; preserves real integration triggers.
// ============================================================================

/**
 * HOME DESTINATION SHELL PLACEHOLDER
 * Protection overview and live security posture.
 */
@Composable
fun HomeDestinationPlaceholder(
    modifier: Modifier = Modifier,
    onOpenCallRisk: () -> Unit = {},
    onOpenCallHistory: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Defense Status Banner
        ProtectionStatusCard(
            state = ProtectionState.ACTIVE,
            title = "SuSagi Live Shield Active",
            description = "Real-time scam call screening and protective threat analysis are operational.",
            onManageClick = onOpenCallRisk,
            manageActionLabel = "Open Live Call Defense Simulator"
        )

        // Architectural Context Card
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Protection Overview",
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    RiskBadge(level = SuSagiRiskLevel.LOW, customLabel = "Protected", size = RiskBadgeSize.Small)
                }

                Text(
                    text = "SuSagi actively protects against impersonation fraud, deceptive urgency, fake police threats, and voice synthesis attacks.",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondarySafetyAction(
                        text = "View Activity Log",
                        onClick = onOpenCallHistory,
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )
                }
            }
        }
    }
}

/**
 * PROTECT DESTINATION SHELL PLACEHOLDER
 * Active defense tools: Call Protection, Message Protection, Link Check, QR Shield.
 */
@Composable
fun ProtectDestinationPlaceholder(
    modifier: Modifier = Modifier,
    onOpenCallRisk: () -> Unit = {},
    onOpenQrScanner: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Active Protection Tools",
            style = SuSagiTheme.typography.title,
            color = SuSagiColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Active defenses working across speech, incoming calls, messages, and QR codes.",
            style = SuSagiTheme.typography.bodyMedium,
            color = SuSagiColors.TextSecondary
        )

        // Tool 1: Call Protection (Live Defense)
        ProtectToolRow(
            title = "Call Protection & Speech Defense",
            description = "Real-time VoIP & speakerphone scam deception analysis.",
            icon = Icons.Default.Phone,
            actionLabel = "Launch Defense",
            onAction = onOpenCallRisk
        )

        // Tool 2: QR Code & Link Shield
        ProtectToolRow(
            title = "QR Code & Link Shield",
            description = "Inspect suspicious payment QR codes and phishing links.",
            icon = Icons.Default.QrCodeScanner,
            actionLabel = "Scan QR Code",
            onAction = onOpenQrScanner
        )

        // Tool 3: Message & SMS Interceptor
        ProtectToolRow(
            title = "Message & SMS Interceptor",
            description = "Intercept fake banking OTP and electricity bill threats.",
            icon = Icons.Default.Security,
            actionLabel = "Configured",
            onAction = {}
        )
    }
}

/**
 * ACTIVITY DESTINATION SHELL PLACEHOLDER
 * Scam sessions, incident history, and evidence vault.
 */
@Composable
fun ActivityDestinationPlaceholder(
    modifier: Modifier = Modifier,
    onOpenCallHistory: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Activity & Threat Logs",
            style = SuSagiTheme.typography.title,
            color = SuSagiColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Audit trail of analyzed calls, blocked numbers, and captured scam transcripts.",
            style = SuSagiTheme.typography.bodyMedium,
            color = SuSagiColors.TextSecondary
        )

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
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = SuSagiColors.Brand,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Call History Database",
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "View locally stored call logs, calculated risk levels, and identified deception vectors.",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary
                )

                PrimarySafetyAction(
                    text = "Open Call History Screen",
                    onClick = onOpenCallHistory,
                    height = 46.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        EmptyState(
            title = "No Active Threats Today",
            description = "All incoming calls and messages have adhered to safe baseline patterns.",
            icon = Icons.Default.Shield
        )
    }
}

/**
 * GUARDIANS DESTINATION SHELL PLACEHOLDER
 * Trusted contacts circle and identity verification relationships.
 */
@Composable
fun GuardiansDestinationPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Guardian Circle",
            style = SuSagiTheme.typography.title,
            color = SuSagiColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Trusted family members and guardians who receive high-priority alerts when you encounter a critical scam call.",
            style = SuSagiTheme.typography.bodyMedium,
            color = SuSagiColors.TextSecondary
        )

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SuSagiColors.BrandSoft,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = SuSagiColors.Brand,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency Alert Dispatch",
                            style = SuSagiTheme.typography.title,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatic push dispatch on confirmed scam threat",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                    }
                }

                Text(
                    text = "When SuSagi detects a severe scam attempt (e.g. digital arrest or OTP theft), your trusted contacts can be notified instantly.",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary
                )
            }
        }

        EmptyState(
            title = "Zero-Trust Contact Verification",
            description = "Known contact baseline tracking prevents voice clone and spoofing attacks.",
            icon = Icons.Default.People
        )
    }
}

/**
 * SETTINGS DESTINATION SHELL PLACEHOLDER
 * Language, privacy, permissions, and defense configuration.
 */
@Composable
fun SettingsDestinationPlaceholder(
    modifier: Modifier = Modifier,
    isHindi: Boolean = false,
    onToggleHindi: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Privacy",
            style = SuSagiTheme.typography.title,
            color = SuSagiColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

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
                Text(
                    text = "Language & Regional Voice",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "SuSagi supports multilingual threat reasoning in English and Hindi.",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary
                )

                SecondarySafetyAction(
                    text = if (isHindi) "Current Language: 🇮🇳 हिंदी (Switch to English)" else "Current Language: 🇬🇧 English (Switch to हिंदी)",
                    onClick = onToggleHindi,
                    height = 46.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SecondarySafetyAction(
            text = "Back to Navigation",
            onClick = onBack,
            height = 46.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProtectToolRow(
    title: String,
    description: String,
    icon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                color = SuSagiColors.SurfaceElevated,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SuSagiColors.Brand,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = description,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                SecondarySafetyAction(
                    text = actionLabel,
                    onClick = onAction,
                    height = 38.dp
                )
            }
        }
    }
}
