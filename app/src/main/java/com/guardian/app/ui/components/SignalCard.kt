package com.guardian.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
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
import com.guardian.app.ui.design.ScamSignalItem
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * SignalCard
 *
 * Displays an individual scam signal detected during speech or message analysis.
 * Replaces debug-telemetry grid with accessible, human-readable signal explanation.
 */
@Composable
fun SignalCard(
    signal: ScamSignalItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val borderColor = when (signal.severity) {
        SuSagiRiskLevel.LOW -> SuSagiColors.BorderSubtle
        SuSagiRiskLevel.CAUTION -> SuSagiColors.RiskCautionBorder
        SuSagiRiskLevel.HIGH -> SuSagiColors.RiskHighBorder
        SuSagiRiskLevel.CRITICAL -> SuSagiColors.RiskCriticalBorder
    }

    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(
            containerColor = SuSagiColors.Surface
        ),
        border = BorderStroke(1.dp, borderColor)
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
                    text = signal.title,
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                RiskBadge(level = signal.severity, size = RiskBadgeSize.Small)
            }

            Text(
                text = signal.description,
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 20.sp
            )

            if (!signal.highlightedText.isNullOrBlank()) {
                Surface(
                    shape = SuSagiShape.sm,
                    color = SuSagiColors.SurfaceElevated,
                    border = BorderStroke(1.dp, SuSagiColors.BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = SuSagiColors.RiskCaution,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "\"${signal.highlightedText}\"",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!signal.timestamp.isNullOrBlank()) {
                Text(
                    text = signal.timestamp,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextMuted
                )
            }
        }
    }
}
