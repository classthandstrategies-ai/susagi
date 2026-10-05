package com.guardian.app.ui.live

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GraphicEq
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * LiveEvidenceCard
 *
 * Supporting evidence component displaying the most relevant or latest statement
 * captured from the caller. Does not overwhelm with an unreadable transcript wall.
 */
@Composable
fun LiveEvidenceCard(
    transcriptText: String?,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "कॉलर ने कहा" else "Caller said",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = SuSagiColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isHindi) "वॉयस ट्रांसक्रिप्शन" else "Speech excerpt",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextMuted
                    )
                }
            }

            Surface(
                shape = SuSagiShape.sm,
                color = SuSagiColors.SurfaceElevated,
                border = BorderStroke(1.dp, SuSagiColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (!transcriptText.isNullOrBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = SuSagiColors.Brand,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "\"$transcriptText\"",
                                style = SuSagiTheme.typography.bodyLarge,
                                color = SuSagiColors.TextPrimary,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 22.sp
                            )
                        }
                    } else {
                        Text(
                            text = if (isHindi)
                                "भाषण सुनने की प्रतीक्षा की जा रही है..."
                            else
                                "Listening for speech... Excerpts will appear here as the conversation unfolds.",
                            style = SuSagiTheme.typography.bodyMedium,
                            color = SuSagiColors.TextMuted,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
