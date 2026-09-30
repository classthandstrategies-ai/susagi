package com.guardian.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * OfflineBanner
 *
 * Informs the user when internet connectivity is unavailable,
 * assuring them that on-device security rules remain active.
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    title: String = "Offline Protection Active",
    message: String = "On-device rules and keyword detection are protecting you without an active internet connection."
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        color = SuSagiColors.SurfaceElevated,
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = SuSagiShape.sm,
                color = SuSagiColors.RiskCautionSoft,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Offline indicator",
                        tint = SuSagiColors.RiskCaution,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SuSagiTheme.typography.labelLarge,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
