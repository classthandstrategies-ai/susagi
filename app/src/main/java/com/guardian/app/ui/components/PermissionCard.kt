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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * PermissionCard
 *
 * Explains essential Android security permissions with transparency and privacy assurance.
 */
@Composable
fun PermissionCard(
    title: String,
    description: String,
    protectionBenefit: String,
    privacyAssurance: String,
    icon: ImageVector,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier,
    grantButtonLabel: String = "Enable Permission"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(
            containerColor = SuSagiColors.Surface
        ),
        border = BorderStroke(
            1.dp,
            if (isGranted) SuSagiColors.RiskLowBorder else SuSagiColors.Border
        )
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
                    color = if (isGranted) SuSagiColors.RiskLowSoft else SuSagiColors.BrandSoft,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) SuSagiColors.RiskLow else SuSagiColors.Brand,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted
                    )
                }

                if (isGranted) {
                    RiskBadge(
                        level = SuSagiRiskLevel.LOW,
                        customLabel = "Granted",
                        size = RiskBadgeSize.Small
                    )
                }
            }

            // Benefit
            Surface(
                shape = SuSagiShape.sm,
                color = SuSagiColors.SurfaceElevated,
                border = BorderStroke(1.dp, SuSagiColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PROTECTION BENEFIT",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = protectionBenefit,
                        style = SuSagiTheme.typography.bodyMedium,
                        color = SuSagiColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Privacy Assurance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SuSagiColors.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = privacyAssurance,
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextMuted,
                    lineHeight = 16.sp
                )
            }

            // Action if not granted
            if (!isGranted) {
                PrimarySafetyAction(
                    text = grantButtonLabel,
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth(),
                    height = 46.dp
                )
            }
        }
    }
}
