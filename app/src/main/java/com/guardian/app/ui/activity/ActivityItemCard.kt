package com.guardian.app.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Human-readable Incident Card for the Activity ledger.
 *
 * Information Architecture:
 * 1. What happened (title: bold, primary)
 * 2. When (formatted date/time: caption)
 * 3. Severity (RiskBadge with explicit icon + text)
 * 4. Context line (calm description of detected risk/pattern)
 * 5. Caller number (secondary metadata)
 */
@Composable
fun ActivityItemCard(
    item: ActivityItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "View incident details",
                onClick = onClick
            ),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title & Severity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = item.formattedTimestamp,
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted
                    )
                }

                Spacer(Modifier.width(8.dp))

                RiskBadge(
                    level = item.riskLevel,
                    size = RiskBadgeSize.Small
                )
            }

            // Context Line (Human-readable explanation)
            Text(
                text = item.contextLine,
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary,
                lineHeight = 20.sp
            )

            // Footer Row: Caller Number & Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.callerNumber,
                    style = SuSagiTheme.typography.code,
                    color = SuSagiColors.TextMuted,
                    fontSize = 13.sp
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SuSagiColors.Brand,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS
// ============================================================================

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Activity Card — High Risk Call", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun ActivityItemCardHighRiskPreview() {
    SuSagiTheme {
        ActivityItemCard(
            item = ActivityItemUiModel(
                id = 1L,
                callerNumber = "+91 98765 43210",
                formattedTimestamp = "Today · 2:07 PM",
                timestampMillis = System.currentTimeMillis(),
                title = "Possible impersonation call",
                riskLevel = SuSagiRiskLevel.HIGH,
                riskScore = 78,
                contextLine = "Caller requested sensitive banking information.",
                topSignals = listOf("OTP Request", "Urgency"),
                transcriptSummary = "Caller claimed account will be suspended if OTP is not verified.",
                actionTaken = "CALL_ENDED",
                wasReported = false
            ),
            onClick = {}
        )
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Activity Card — Protected Call", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun ActivityItemCardProtectedPreview() {
    SuSagiTheme {
        ActivityItemCard(
            item = ActivityItemUiModel(
                id = 2L,
                callerNumber = "+91 91234 56789",
                formattedTimestamp = "Yesterday · 4:15 PM",
                timestampMillis = System.currentTimeMillis() - 86400000L,
                title = "Protected call",
                riskLevel = SuSagiRiskLevel.LOW,
                riskScore = 12,
                contextLine = "Adhered to safe conversational patterns.",
                topSignals = emptyList(),
                transcriptSummary = "Standard delivery coordination call.",
                actionTaken = "NORMAL",
                wasReported = false
            ),
            onClick = {}
        )
    }
}
