package com.guardian.app.ui.live

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * RecommendedActionCard
 *
 * Prominent recommendation and defensive action triggers (Verify Identity, End Call, Block).
 * Answers "WHAT SHOULD I DO NEXT" with calm, direct, banking-grade authority.
 */
@Composable
fun RecommendedActionCard(
    uiState: LiveDefenseUiState,
    onVerifyIdentity: () -> Unit,
    onEndCall: () -> Unit,
    onBlockNumber: (() -> Unit)? = null,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isElevatedThreat = uiState.riskLevel == SuSagiRiskLevel.HIGH || uiState.riskLevel == SuSagiRiskLevel.CRITICAL
    val cardBorder = if (isElevatedThreat) {
        BorderStroke(1.dp, SuSagiColors.RiskHighBorder)
    } else {
        BorderStroke(1.dp, SuSagiColors.Border)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isHindi) "अनुशंसित कार्रवाई" else "Recommended Action",
                style = SuSagiTheme.typography.title,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            // Direct Guidance Banner
            Surface(
                shape = SuSagiShape.sm,
                color = if (isElevatedThreat) SuSagiColors.RiskHighSoft else SuSagiColors.SurfaceElevated,
                border = BorderStroke(
                    1.dp,
                    if (isElevatedThreat) SuSagiColors.RiskHighBorder else SuSagiColors.BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isHindi) uiState.recommendedActionHeadlineHi else uiState.recommendedActionHeadline,
                        style = SuSagiTheme.typography.title,
                        color = if (isElevatedThreat) SuSagiColors.RiskHigh else SuSagiColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isHindi) uiState.recommendedActionDetailHi else uiState.recommendedActionDetail,
                        style = SuSagiTheme.typography.bodyMedium,
                        color = SuSagiColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Primary Safety Actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Identity Verification Trigger (CP3 Entry Point)
                if (uiState.canVerifyIdentity) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PrimarySafetyAction(
                            text = if (isHindi) "पहचान सत्यापित करें" else "Verify Identity",
                            onClick = onVerifyIdentity,
                            icon = Icons.Default.Security,
                            enabled = uiState.isIdentityVerificationAvailable,
                            modifier = Modifier.fillMaxWidth(),
                            height = 48.dp
                        )
                        if (!uiState.isIdentityVerificationAvailable) {
                            Text(
                                text = if (isHindi) "पहचान सत्यापन अभी उपलब्ध नहीं है।" else "Identity verification is not available yet.",
                                style = SuSagiTheme.typography.caption,
                                color = SuSagiColors.TextMuted,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                // End Call Action
                if (uiState.canEndCall) {
                    Button(
                        onClick = onEndCall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = SuSagiShape.button,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuSagiColors.RiskHigh,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isHindi) "कॉल समाप्त करें" else "End Call",
                                style = SuSagiTheme.typography.buttonLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Optional Block Caller Action
                if (onBlockNumber != null && isElevatedThreat) {
                    SecondarySafetyAction(
                        text = if (isHindi) "इस नंबर को ब्लॉक करें" else "Block Number",
                        onClick = onBlockNumber,
                        icon = Icons.Default.Block,
                        modifier = Modifier.fillMaxWidth(),
                        height = 42.dp
                    )
                }
            }
        }
    }
}
