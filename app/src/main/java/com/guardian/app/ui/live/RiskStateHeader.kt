package com.guardian.app.ui.live

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.GxLiveDot
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * RiskStateHeader
 *
 * Prominent top section of the SuSagi Live Defense screen.
 * Displays ongoing call metadata, claimed identity, and primary semantic risk posture.
 */
@Composable
fun RiskStateHeader(
    uiState: LiveDefenseUiState,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg, borderStroke, icon) = when (uiState.riskLevel) {
        SuSagiRiskLevel.LOW -> Quad(
            SuSagiColors.RiskLow,
            SuSagiColors.RiskLowSoft,
            BorderStroke(1.dp, SuSagiColors.RiskLowBorder),
            Icons.Default.Shield
        )
        SuSagiRiskLevel.CAUTION -> Quad(
            SuSagiColors.RiskCaution,
            SuSagiColors.RiskCautionSoft,
            BorderStroke(1.dp, SuSagiColors.RiskCautionBorder),
            Icons.Default.WarningAmber
        )
        SuSagiRiskLevel.HIGH -> Quad(
            SuSagiColors.RiskHigh,
            SuSagiColors.RiskHighSoft,
            BorderStroke(1.dp, SuSagiColors.RiskHighBorder),
            Icons.Default.WarningAmber
        )
        SuSagiRiskLevel.CRITICAL -> Quad(
            SuSagiColors.RiskHigh,
            SuSagiColors.RiskHighSoft,
            BorderStroke(1.5.dp, SuSagiColors.RiskHigh),
            Icons.Default.WarningAmber
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ----------------------------------------------------
            // 1. CALL IDENTITY & MONITORING STATUS
            // ----------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isPulsing = uiState.sessionStatus == LiveSessionStatus.MONITORING ||
                                    uiState.sessionStatus == LiveSessionStatus.ANALYZING
                    GxLiveDot(
                        pulsing = isPulsing,
                        color = if (isPulsing) statusColor else SuSagiColors.TextMuted,
                        size = 10.dp
                    )
                    Text(
                        text = when (uiState.sessionStatus) {
                            LiveSessionStatus.MONITORING -> if (isHindi) "सक्रिय निगरानी" else "LIVE MONITORING"
                            LiveSessionStatus.ANALYZING -> if (isHindi) "विश्लेषण प्रगति पर" else "ANALYZING SPEECH"
                            LiveSessionStatus.STANDBY -> if (isHindi) "स्टैंडबाय" else "STANDBY"
                            LiveSessionStatus.ANALYSIS_UNAVAILABLE -> if (isHindi) "विश्लेषण अनुपलब्ध" else "STANDALONE MODE"
                            LiveSessionStatus.ERROR -> if (isHindi) "त्रुटि" else "ATTENTION NEEDED"
                        },
                        style = SuSagiTheme.typography.caption,
                        color = if (isPulsing) statusColor else SuSagiColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                RiskBadge(
                    level = uiState.riskLevel,
                    size = RiskBadgeSize.Regular
                )
            }

            // ----------------------------------------------------
            // 2. CALLER IDENTIFIER & CLAIMED IDENTITY
            // ----------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = uiState.callerIdentifier,
                    style = SuSagiTheme.typography.display,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )

                if (!uiState.claimedIdentity.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isHindi) "दावा की गई पहचान:" else "Claimed identity:",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                        Text(
                            text = uiState.claimedIdentity,
                            style = SuSagiTheme.typography.bodyMedium,
                            color = SuSagiColors.Brand,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // 3. SEMANTIC REASONING HEADLINE
            // ----------------------------------------------------
            Surface(
                shape = SuSagiShape.sm,
                color = statusBg,
                border = borderStroke,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )

                    Text(
                        text = if (isHindi) uiState.summaryHeadlineHi else uiState.summaryHeadline,
                        style = SuSagiTheme.typography.bodyLarge,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
