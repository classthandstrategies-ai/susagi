package com.guardian.app.ui.guardians

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Banking-grade Guardian contact card.
 *
 * Displays trusted contact identity, automated alert posture, and management controls.
 * Enforces minimum 48dp touch targets and high-contrast accessible typography.
 */
@Composable
fun GuardianCard(
    guardian: GuardianUiModel,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Avatar, Name & Phone, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Guardian Avatar
                Surface(
                    shape = CircleShape,
                    color = SuSagiColors.BrandSoft,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SuSagiColors.Brand,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Identity info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guardian.name,
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = guardian.phone,
                        style = SuSagiTheme.typography.bodyMedium,
                        color = SuSagiColors.TextSecondary
                    )
                    if (!guardian.relationship.isNullOrBlank()) {
                        Text(
                            text = guardian.relationship,
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Status Badge
                when (guardian.status) {
                    GuardianStatus.ACTIVE -> {
                        RiskBadge(
                            level = SuSagiRiskLevel.LOW,
                            customLabel = if (isHindi) "सक्रिय" else "Active",
                            size = RiskBadgeSize.Small
                        )
                    }
                    GuardianStatus.PAUSED -> {
                        RiskBadge(
                            level = SuSagiRiskLevel.CAUTION,
                            customLabel = if (isHindi) "रोक दिया" else "Paused",
                            size = RiskBadgeSize.Small
                        )
                    }
                    GuardianStatus.PENDING_SETUP -> {
                        RiskBadge(
                            level = SuSagiRiskLevel.CAUTION,
                            customLabel = if (isHindi) "लंबित" else "Pending",
                            size = RiskBadgeSize.Small
                        )
                    }
                }
            }

            // Explanatory note
            val noteText = when (guardian.status) {
                GuardianStatus.ACTIVE -> {
                    if (isHindi) {
                        "आपका विश्वसनीय व्यक्ति सुरक्षा अलर्ट प्राप्त कर सकता है।"
                    } else {
                        "Your trusted person can receive safety alerts."
                    }
                }
                GuardianStatus.PAUSED -> {
                    if (isHindi) {
                        "सुरक्षा अलर्ट रोक दिए गए हैं। फिर से शुरू करने के लिए प्रबंधित करें।"
                    } else {
                        "Safety alerts paused. Tap Manage to resume."
                    }
                }
                GuardianStatus.PENDING_SETUP -> {
                    if (isHindi) {
                        "सेटअप पूरा होने तक अलर्ट नहीं भेजे जा सकते।"
                    } else {
                        "Alerts cannot be dispatched until contact setup is finalized."
                    }
                }
            }

            Text(
                text = noteText,
                style = SuSagiTheme.typography.caption,
                color = SuSagiColors.TextSecondary
            )

            // Management Action
            SecondarySafetyAction(
                text = if (isHindi) "प्रबंधित करें" else "Manage",
                onClick = onManage,
                modifier = Modifier.fillMaxWidth(),
                height = 44.dp
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS
// ============================================================================

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Guardian Card — Active", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun GuardianCardActivePreview() {
    SuSagiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GuardianCard(
                guardian = GuardianUiModel(
                    id = "preview_1",
                    name = "Priya Sharma",
                    phone = "+91 98765 43210",
                    status = GuardianStatus.ACTIVE,
                    isEnabled = true,
                    relationship = "Primary Emergency Contact"
                ),
                onManage = {}
            )
        }
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Guardian Card — Paused", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun GuardianCardPausedPreview() {
    SuSagiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GuardianCard(
                guardian = GuardianUiModel(
                    id = "preview_2",
                    name = "Rajesh Sharma",
                    phone = "+91 91234 56789",
                    status = GuardianStatus.PAUSED,
                    isEnabled = false,
                    relationship = "Spouse"
                ),
                onManage = {}
            )
        }
    }
}
