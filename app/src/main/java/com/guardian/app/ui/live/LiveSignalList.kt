package com.guardian.app.ui.live

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * LiveSignalList
 *
 * Displays the top 3-4 scam signals formatted in human terms
 * under the "Why SuSagi is concerned" section.
 */
@Composable
fun LiveSignalList(
    signals: List<LiveSignalUiModel>,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isHindi) "सुसागी क्यों चिंतित है" else "Why SuSagi is concerned",
                style = SuSagiTheme.typography.title,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            if (signals.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuSagiColors.RiskLow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isHindi)
                            "अब तक किसी भी दबाव या धोखाधड़ी रणनीति का पता नहीं चला है।"
                        else
                            "No suspicious tactics or urgency signals detected in this call so far.",
                        style = SuSagiTheme.typography.bodyMedium,
                        color = SuSagiColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    signals.forEach { signal ->
                        val indicatorColor = when {
                            signal.isCritical -> SuSagiColors.RiskHigh
                            signal.isCautionary -> SuSagiColors.RiskCaution
                            else -> SuSagiColors.Brand
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(8.dp)
                                    .background(indicatorColor, CircleShape)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = signal.title,
                                    style = SuSagiTheme.typography.bodyLarge,
                                    color = SuSagiColors.TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = signal.description,
                                    style = SuSagiTheme.typography.bodyMedium,
                                    color = SuSagiColors.TextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
