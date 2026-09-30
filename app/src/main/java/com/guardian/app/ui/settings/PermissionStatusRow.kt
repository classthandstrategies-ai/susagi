package com.guardian.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Accessible permission status row explaining context before action:
 * CONTEXT -> WHY IT HELPS -> ENABLE
 */
@Composable
fun PermissionStatusRow(
    item: PermissionItem,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.description,
                style = SuSagiTheme.typography.caption,
                color = SuSagiColors.TextSecondary,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        if (item.isGranted) {
            RiskBadge(
                level = SuSagiRiskLevel.LOW,
                customLabel = if (isHindi) "सक्रिय" else "Granted",
                size = RiskBadgeSize.Small
            )
        } else {
            SecondarySafetyAction(
                text = if (isHindi) "सक्षम करें" else "Enable",
                onClick = onGrant,
                height = 36.dp
            )
        }
    }
}
