package com.guardian.app.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Individual chronological timeline step for an incident or session.
 */
@Composable
fun IncidentTimelineItem(
    event: ScamTimelineEvent,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (event.type) {
        ScamTimelineEventType.CALL -> Icons.Default.Phone
        ScamTimelineEventType.MESSAGE -> Icons.AutoMirrored.Filled.Message
        ScamTimelineEventType.LINK -> Icons.Default.Link
        ScamTimelineEventType.QR -> Icons.Default.QrCodeScanner
        ScamTimelineEventType.VERIFICATION -> Icons.Default.Security
        ScamTimelineEventType.ACTION -> Icons.Default.Block
        ScamTimelineEventType.SYSTEM -> Icons.Default.Shield
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline node column: Icon circle + vertical connecting line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SuSagiColors.SurfaceElevated,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SuSagiColors.Brand,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(SuSagiColors.Border)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Content Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = event.timestamp,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextMuted,
                    fontSize = 11.sp
                )
            }

            if (event.detail.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = event.detail,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
