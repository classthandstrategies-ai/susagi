package com.guardian.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape

enum class RiskBadgeSize(
    val height: Dp,
    val iconSize: Dp,
    val fontSize: Int,
    val horizontalPadding: Dp,
    val spacing: Dp
) {
    Small(height = 24.dp, iconSize = 13.dp, fontSize = 12, horizontalPadding = 8.dp, spacing = 4.dp),
    Regular(height = 30.dp, iconSize = 16.dp, fontSize = 13, horizontalPadding = 10.dp, spacing = 6.dp),
    Large(height = 36.dp, iconSize = 18.dp, fontSize = 14, horizontalPadding = 12.dp, spacing = 8.dp)
}

/**
 * Semantic Risk Badge
 *
 * Accessibility requirement:
 * Never conveys risk through color alone — every badge displays both an explicit status icon
 * and clear, human-readable text.
 */
@Composable
fun RiskBadge(
    level: SuSagiRiskLevel,
    modifier: Modifier = Modifier,
    customLabel: String? = null,
    showIcon: Boolean = true,
    size: RiskBadgeSize = RiskBadgeSize.Regular
) {
    val (bgColor, contentColor, borderColor, icon) = when (level) {
        SuSagiRiskLevel.LOW -> BadgeConfig(
            bgColor = SuSagiColors.RiskLowSoft,
            contentColor = SuSagiColors.RiskLow,
            borderColor = SuSagiColors.RiskLowBorder,
            icon = Icons.Default.CheckCircle
        )
        SuSagiRiskLevel.CAUTION -> BadgeConfig(
            bgColor = SuSagiColors.RiskCautionSoft,
            contentColor = SuSagiColors.RiskCaution,
            borderColor = SuSagiColors.RiskCautionBorder,
            icon = Icons.Default.WarningAmber
        )
        SuSagiRiskLevel.HIGH -> BadgeConfig(
            bgColor = SuSagiColors.RiskHighSoft,
            contentColor = SuSagiColors.RiskHigh,
            borderColor = SuSagiColors.RiskHighBorder,
            icon = Icons.Default.Warning
        )
        SuSagiRiskLevel.CRITICAL -> BadgeConfig(
            bgColor = SuSagiColors.RiskCriticalSoft,
            contentColor = SuSagiColors.RiskCritical,
            borderColor = SuSagiColors.RiskCriticalBorder,
            icon = Icons.Default.Dangerous
        )
    }

    val labelText = customLabel ?: level.title
    val semanticDescription = "Risk Level: ${level.title}"

    Surface(
        modifier = modifier
            .height(size.height)
            .semantics { contentDescription = semanticDescription },
        shape = SuSagiShape.badge,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = size.horizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIcon) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(size.iconSize)
                    )
                    Spacer(Modifier.width(size.spacing))
                }
                Text(
                    text = labelText,
                    color = contentColor,
                    fontSize = size.fontSize.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

private data class BadgeConfig(
    val bgColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val icon: ImageVector
)
