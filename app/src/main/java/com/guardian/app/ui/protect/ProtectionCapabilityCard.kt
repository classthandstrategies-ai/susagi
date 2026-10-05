package com.guardian.app.ui.protect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * ProtectionCapabilityCard
 *
 * Calm, accessible, banking-grade card representing a single SuSagi defense capability.
 * Displays operational status, human explanation, actionable controls, contextual permission
 * guidance when disabled, and transparent privacy assurances.
 */
@Composable
fun ProtectionCapabilityCard(
    capability: ProtectionCapabilityUiModel,
    onToggle: ((Boolean) -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    onGrantPermissionClick: (() -> Unit)? = null,
    onOpenCallRisk: (() -> Unit)? = null,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (capability.id) {
        "calls" -> Icons.Default.Phone
        "messages" -> Icons.AutoMirrored.Filled.Message
        "links" -> Icons.Default.Link
        "qr" -> Icons.Default.QrCodeScanner
        else -> Icons.Default.Phone
    }

    val (badgeLevel, badgeLabel) = when (capability.status) {
        CapabilityStatus.ACTIVE -> Pair(
            SuSagiRiskLevel.LOW,
            if (isHindi) "सक्रिय" else "Active"
        )
        CapabilityStatus.AVAILABLE -> Pair(
            SuSagiRiskLevel.LOW,
            if (isHindi) "तैयार" else "Ready"
        )
        CapabilityStatus.NEEDS_ATTENTION -> Pair(
            SuSagiRiskLevel.HIGH,
            if (isHindi) "अनुमति आवश्यक" else "Setup Required"
        )
        CapabilityStatus.PAUSED -> Pair(
            SuSagiRiskLevel.CAUTION,
            if (isHindi) "रोक दिया गया" else "Paused"
        )
    }

    val iconBgColor = when (capability.status) {
        CapabilityStatus.ACTIVE, CapabilityStatus.AVAILABLE -> SuSagiColors.RiskLowSoft
        CapabilityStatus.NEEDS_ATTENTION -> SuSagiColors.RiskHighSoft
        CapabilityStatus.PAUSED -> SuSagiColors.SurfaceElevated
    }

    val iconTintColor = when (capability.status) {
        CapabilityStatus.ACTIVE, CapabilityStatus.AVAILABLE -> SuSagiColors.RiskLow
        CapabilityStatus.NEEDS_ATTENTION -> SuSagiColors.RiskHigh
        CapabilityStatus.PAUSED -> SuSagiColors.TextMuted
    }

    val cardBorder = when (capability.status) {
        CapabilityStatus.NEEDS_ATTENTION -> BorderStroke(1.dp, SuSagiColors.RiskHighBorder)
        CapabilityStatus.ACTIVE -> BorderStroke(1.dp, SuSagiColors.BorderSubtle)
        else -> BorderStroke(1.dp, SuSagiColors.Border)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(
            containerColor = SuSagiColors.Surface
        ),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ----------------------------------------------------
            // HEADER ROW: Icon + Title + Status + Action
            // ----------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (isHindi) capability.titleHi else capability.title,
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RiskBadge(
                            level = badgeLevel,
                            customLabel = badgeLabel,
                            size = RiskBadgeSize.Small
                        )
                    }
                }

                // Interactive control on top right
                when (capability.actionType) {
                    CapabilityActionType.TOGGLE -> {
                        Switch(
                            checked = capability.isEnabled,
                            onCheckedChange = { isChecked ->
                                onToggle?.invoke(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuSagiColors.RiskLow,
                                uncheckedThumbColor = SuSagiColors.TextMuted,
                                uncheckedTrackColor = SuSagiColors.SurfaceElevated,
                                uncheckedBorderColor = SuSagiColors.Border
                            ),
                            modifier = Modifier.semantics {
                                role = Role.Switch
                                contentDescription = "${if (isHindi) capability.titleHi else capability.title} switch, currently ${if (capability.isEnabled) "enabled" else "disabled"}"
                            }
                        )
                    }
                    CapabilityActionType.ACTION_BUTTON -> {
                        SecondarySafetyAction(
                            text = if (isHindi) capability.actionLabelHi ?: "खोलें" else capability.actionLabel ?: "Open",
                            onClick = { onActionClick?.invoke() },
                            modifier = Modifier.height(38.dp)
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // DESCRIPTION
            // ----------------------------------------------------
            Text(
                text = if (isHindi) capability.descriptionHi else capability.description,
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 22.sp
            )

            // ----------------------------------------------------
            // CONTEXTUAL PERMISSION / SETUP BANNER (IF NEEDED)
            // ----------------------------------------------------
            if (capability.status == CapabilityStatus.NEEDS_ATTENTION && capability.permissionRequiredMessage != null) {
                Surface(
                    shape = SuSagiShape.sm,
                    color = SuSagiColors.RiskHighSoft,
                    border = BorderStroke(1.dp, SuSagiColors.RiskHighBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = SuSagiColors.RiskHigh,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isHindi) "आवश्यक अनुमति प्रदान करें" else "Action Required",
                                style = SuSagiTheme.typography.bodyMedium,
                                color = SuSagiColors.RiskHigh,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (isHindi) capability.permissionRequiredMessageHi ?: capability.permissionRequiredMessage
                            else capability.permissionRequiredMessage,
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextSecondary,
                            lineHeight = 18.sp
                        )

                        PrimarySafetyAction(
                            text = if (isHindi) "अनुमति चालू करें" else "Enable Shield",
                            onClick = { onGrantPermissionClick?.invoke() },
                            modifier = Modifier.fillMaxWidth(),
                            height = 42.dp
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // CALL PROTECTION ACTIVE INSPECTION LINK
            // ----------------------------------------------------
            if (capability.id == "calls" && capability.isEnabled && onOpenCallRisk != null) {
                SecondarySafetyAction(
                    text = if (isHindi) "लाइव कॉल सुरक्षा मॉनिटर देखें" else "Inspect Live Call Risk",
                    onClick = onOpenCallRisk,
                    modifier = Modifier.fillMaxWidth(),
                    height = 40.dp
                )
            }

            // ----------------------------------------------------
            // PRIVACY ASSURANCE
            // ----------------------------------------------------
            val privacyText = if (isHindi) capability.privacyAssuranceHi else capability.privacyAssurance
            if (!privacyText.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SuSagiColors.TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = privacyText,
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
