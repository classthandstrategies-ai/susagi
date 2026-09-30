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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiRiskSummaryModel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * RiskSummary: The Primary UX Anchor for SuSagi V1.
 *
 * Implements the core SuSagi product principle:
 * 1. WHAT IS HAPPENING (Clear situational headline)
 * 2. WHY IT IS RISKY (Plain-language explanation)
 * 3. WHAT THE USER SHOULD DO NEXT (Actionable protective advice)
 *
 * Replaces percentage-first rings and AI-debug telemetry with calm,
 * banking-grade clarity.
 */
@Composable
fun RiskSummary(
    riskLevel: SuSagiRiskLevel,
    headline: String,
    explanation: String,
    recommendedAction: String,
    modifier: Modifier = Modifier,
    contextBadgeText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionLabel: String? = null
) {
    val borderColor = when (riskLevel) {
        SuSagiRiskLevel.LOW -> SuSagiColors.BorderSubtle
        SuSagiRiskLevel.CAUTION -> SuSagiColors.RiskCautionBorder
        SuSagiRiskLevel.HIGH -> SuSagiColors.RiskHighBorder
        SuSagiRiskLevel.CRITICAL -> SuSagiColors.RiskCriticalBorder
    }

    val actionBannerBg = when (riskLevel) {
        SuSagiRiskLevel.LOW -> SuSagiColors.RiskLowSoft
        SuSagiRiskLevel.CAUTION -> SuSagiColors.RiskCautionSoft
        SuSagiRiskLevel.HIGH -> SuSagiColors.RiskHighSoft
        SuSagiRiskLevel.CRITICAL -> SuSagiColors.RiskCriticalSoft
    }

    val actionBannerAccent = when (riskLevel) {
        SuSagiRiskLevel.LOW -> SuSagiColors.RiskLow
        SuSagiRiskLevel.CAUTION -> SuSagiColors.RiskCaution
        SuSagiRiskLevel.HIGH -> SuSagiColors.RiskHigh
        SuSagiRiskLevel.CRITICAL -> SuSagiColors.RiskCritical
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Status Badge + Context (e.g. caller or channel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RiskBadge(level = riskLevel, size = RiskBadgeSize.Regular)

                if (!contextBadgeText.isNullOrBlank()) {
                    Text(
                        text = contextBadgeText,
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted
                    )
                }
            }

            // 1. WHAT IS HAPPENING
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "WHAT IS HAPPENING",
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = headline,
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. WHY IT IS RISKY
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "WHY IT IS RISKY",
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = explanation,
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 22.sp
                )
            }

            // 3. WHAT THE USER SHOULD DO NEXT
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = SuSagiShape.md,
                color = actionBannerBg,
                border = BorderStroke(1.dp, actionBannerAccent.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = actionBannerAccent,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WHAT YOU SHOULD DO NEXT",
                            style = SuSagiTheme.typography.caption,
                            color = actionBannerAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = recommendedAction,
                            style = SuSagiTheme.typography.bodyMedium,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Optional direct primary action
            if (onPrimaryAction != null && !primaryActionLabel.isNullOrBlank()) {
                val isUrgent = riskLevel == SuSagiRiskLevel.CRITICAL || riskLevel == SuSagiRiskLevel.HIGH
                PrimarySafetyAction(
                    text = primaryActionLabel,
                    onClick = onPrimaryAction,
                    isCritical = isUrgent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RiskSummary(
    model: SuSagiRiskSummaryModel,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false,
    contextBadgeText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionLabel: String? = null
) {
    val headline = if (isHindi && !model.hindiHeadline.isNullOrBlank()) model.hindiHeadline else model.headline
    val explanation = if (isHindi && !model.hindiExplanation.isNullOrBlank()) model.hindiExplanation else model.explanation
    val recommendedAction = if (isHindi && !model.hindiRecommendedAction.isNullOrBlank()) model.hindiRecommendedAction else model.recommendedAction

    RiskSummary(
        riskLevel = model.riskLevel,
        headline = headline,
        explanation = explanation,
        recommendedAction = recommendedAction,
        modifier = modifier,
        contextBadgeText = contextBadgeText,
        onPrimaryAction = onPrimaryAction,
        primaryActionLabel = primaryActionLabel
    )
}
