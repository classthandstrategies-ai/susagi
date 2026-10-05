package com.guardian.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.ProtectionState
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * ProtectionStatusCard
 *
 * Displays the current defense posture of SuSagi.
 * Communicates protection state with calm, trustworthy, banking-grade confidence.
 */
@Composable
fun ProtectionStatusCard(
    state: ProtectionState,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    onManageClick: (() -> Unit)? = null,
    manageActionLabel: String = "Configure Shields"
) {
    val (statusTitle, statusDesc, statusColor, statusBg, icon) = when (state) {
        ProtectionState.ACTIVE -> StatusConfig(
            title = title ?: "Protection Active",
            desc = description ?: "Real-time call screening and scam defense are safeguarding your device.",
            color = SuSagiColors.RiskLow,
            bg = SuSagiColors.RiskLowSoft,
            icon = Icons.Default.Shield
        )
        ProtectionState.PAUSED -> StatusConfig(
            title = title ?: "Protection Paused",
            desc = description ?: "Real-time call screening is temporarily suspended.",
            color = SuSagiColors.RiskCaution,
            bg = SuSagiColors.RiskCautionSoft,
            icon = Icons.Default.PauseCircle
        )
        ProtectionState.ATTENTION_REQUIRED -> StatusConfig(
            title = title ?: "Action Required",
            desc = description ?: "Permissions or settings need attention to ensure full coverage.",
            color = SuSagiColors.RiskHigh,
            bg = SuSagiColors.RiskHighSoft,
            icon = Icons.Default.WarningAmber
        )
        ProtectionState.OFFLINE -> StatusConfig(
            title = title ?: "Offline Mode Active",
            desc = description ?: "On-device rules and keyword detection are protecting you without cloud services.",
            color = SuSagiColors.BrandLight,
            bg = SuSagiColors.BrandSoft,
            icon = Icons.Default.WifiOff
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(
            containerColor = SuSagiColors.Surface
        ),
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
                    color = statusBg,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (state) {
                            ProtectionState.ACTIVE -> "All shields operational"
                            ProtectionState.PAUSED -> "Shields paused by user"
                            ProtectionState.ATTENTION_REQUIRED -> "Needs permission grant"
                            ProtectionState.OFFLINE -> "Local intelligence fallback"
                        },
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

            if (onManageClick != null) {
                SecondarySafetyAction(
                    text = manageActionLabel,
                    onClick = onManageClick,
                    modifier = Modifier.fillMaxWidth(),
                    height = 44.dp
                )
            }
        }
    }
}

private data class StatusConfig(
    val title: String,
    val desc: String,
    val color: Color,
    val bg: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
