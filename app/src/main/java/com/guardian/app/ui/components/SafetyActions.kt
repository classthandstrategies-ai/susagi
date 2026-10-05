package com.guardian.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing

/**
 * Primary Safety Action Button
 *
 * Essential defensive action (e.g. "Hang Up Call", "Block & Report", "Enable Protection").
 * Enforces minimum 48dp touch target, high-contrast readable text, and calm non-distracting styling.
 */
@Composable
fun PrimarySafetyAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    isCritical: Boolean = false,
    height: Dp = SuSagiSpacing.buttonHeight
) {
    val containerColor = if (isCritical) SuSagiColors.RiskCritical else SuSagiColors.Brand
    val contentColor = if (isCritical) SuSagiColors.TextOnDanger else SuSagiColors.TextOnBrand

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = SuSagiShape.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = SuSagiColors.SurfaceElevated,
            disabledContentColor = SuSagiColors.TextDisabled
        ),
        contentPadding = PaddingValues(horizontal = SuSagiSpacing.buttonPaddingHorizontal),
        modifier = modifier
            .height(height)
            .defaultMinSize(minHeight = SuSagiSpacing.minTouchTarget)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Secondary Safety Action Button
 *
 * Supporting protective actions (e.g. "Verify With Bank", "Share with Guardian", "Dismiss").
 * Outlined button with 48dp minimum touch target.
 */
@Composable
fun SecondarySafetyAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    height: Dp = SuSagiSpacing.buttonHeight
) {
    val contentColor = if (isDestructive) SuSagiColors.RiskCritical else SuSagiColors.TextPrimary
    val borderColor = if (isDestructive) SuSagiColors.RiskCriticalBorder else SuSagiColors.Border

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = SuSagiShape.button,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = SuSagiColors.TextDisabled
        ),
        contentPadding = PaddingValues(horizontal = SuSagiSpacing.buttonPaddingHorizontal),
        modifier = modifier
            .height(height)
            .defaultMinSize(minHeight = SuSagiSpacing.minTouchTarget)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
