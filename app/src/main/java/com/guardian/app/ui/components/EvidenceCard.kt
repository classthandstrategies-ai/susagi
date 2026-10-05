package com.guardian.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.ScamEvidenceItem
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * EvidenceCard
 *
 * Displays captured scam interaction evidence for user review, legal reporting,
 * or trusted guardian dispatch.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EvidenceCard(
    evidence: ScamEvidenceItem,
    modifier: Modifier = Modifier,
    onExport: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(
            containerColor = SuSagiColors.Surface
        ),
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title & Severity
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evidence.title,
                        style = SuSagiTheme.typography.title,
                        color = SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = SuSagiColors.TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = evidence.callerOrSource,
                            style = SuSagiTheme.typography.code,
                            fontSize = 13.sp,
                            color = SuSagiColors.TextSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "• ${evidence.timestamp}",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                    }
                }
                RiskBadge(level = evidence.severity, size = RiskBadgeSize.Small)
            }

            // Transcript Snippet (if available)
            if (!evidence.transcriptSnippet.isNullOrBlank()) {
                Surface(
                    shape = SuSagiShape.sm,
                    color = SuSagiColors.SurfaceElevated,
                    border = BorderStroke(1.dp, SuSagiColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "RECORDED EXCERPT",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "\"${evidence.transcriptSnippet}\"",
                            style = SuSagiTheme.typography.bodyMedium,
                            color = SuSagiColors.TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Tags
            if (evidence.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    evidence.tags.forEach { tag ->
                        Surface(
                            shape = SuSagiShape.pill,
                            color = SuSagiColors.SurfaceElevated,
                            border = BorderStroke(1.dp, SuSagiColors.BorderSubtle)
                        ) {
                            Text(
                                text = tag,
                                style = SuSagiTheme.typography.caption,
                                color = SuSagiColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Optional Actions (Export, Share)
            if (onExport != null || onShare != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onExport != null) {
                        SecondarySafetyAction(
                            text = "Export Evidence",
                            onClick = onExport,
                            icon = Icons.Default.FileDownload,
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )
                    }
                    if (onShare != null) {
                        SecondarySafetyAction(
                            text = "Share With Guardian",
                            onClick = onShare,
                            icon = Icons.Default.Share,
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )
                    }
                }
            }
        }
    }
}
