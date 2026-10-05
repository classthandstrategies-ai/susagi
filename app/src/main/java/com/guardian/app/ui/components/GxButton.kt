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
import com.guardian.app.ui.theme.GxBorder
import com.guardian.app.ui.theme.GxDanger
import com.guardian.app.ui.theme.GxPrimary
import com.guardian.app.ui.theme.GxShapeMd
import com.guardian.app.ui.theme.GxSurfaceAlt
import com.guardian.app.ui.theme.GxTextHi
import com.guardian.app.ui.theme.GxTextLo
import com.guardian.app.ui.theme.GxTextMid

/**
 * GxButton (Refined)
 *
 * Refined for accessibility, calm banking-grade aesthetics, and minimum 48dp touch targets.
 * Distracting continuous danger strobing has been replaced with calm, high-contrast styling.
 */
object GxButton {

    @Composable
    fun Primary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        loading: Boolean = false,
        height: Dp = 52.dp
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            shape = GxShapeMd,
            colors = ButtonDefaults.buttonColors(
                containerColor = GxPrimary,
                contentColor = GxTextHi,
                disabledContainerColor = GxSurfaceAlt,
                disabledContentColor = GxTextLo
            ),
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = modifier
                .height(height)
                .defaultMinSize(minHeight = 48.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = GxTextHi,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    @Composable
    fun Danger(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        pulsing: Boolean = false,
        enabled: Boolean = true,
        height: Dp = 52.dp
    ) {
        // Accessibility improvement: replace anxiety-inducing continuous alpha strobe
        // with steady, confident high-contrast solid danger container
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = GxShapeMd,
            colors = ButtonDefaults.buttonColors(
                containerColor = GxDanger,
                contentColor = Color.White,
                disabledContainerColor = GxSurfaceAlt,
                disabledContentColor = GxTextLo
            ),
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = modifier
                .height(height)
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    fun Ghost(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        height: Dp = 52.dp
    ) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = GxShapeMd,
            border = BorderStroke(1.dp, GxBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GxTextMid,
                disabledContentColor = GxTextLo
            ),
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = modifier
                .height(height)
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
