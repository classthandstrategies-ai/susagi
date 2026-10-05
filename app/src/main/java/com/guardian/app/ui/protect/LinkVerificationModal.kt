package com.guardian.app.ui.protect

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.LinkCheckActivity
import com.guardian.app.LinkVerdict
import com.guardian.app.RiskStatus
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * LinkVerificationModal
 *
 * In-app URL inspection modal allowing users to safely verify suspicious web links
 * before opening them. Grounded in LinkCheckActivity.evaluateLinkSafety().
 */
@Composable
fun LinkVerificationModal(
    onDismiss: () -> Unit,
    isHindi: Boolean = false
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }
    var verdict by remember { mutableStateOf<LinkVerdict?>(null) }
    var isEvaluated by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SuSagiColors.Surface,
        titleContentColor = SuSagiColors.TextPrimary,
        textContentColor = SuSagiColors.TextSecondary,
        shape = SuSagiShape.card,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = SuSagiColors.Brand,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isHindi) "वेब लिंक की जांच करें" else "Verify Suspicious Link",
                    style = SuSagiTheme.typography.headline,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isHindi)
                        "फ़िशिंग, फ़र्ज़ी बैंकिंग और मैलवेयर डाउनलोड से बचने के लिए खोलने से पहले किसी भी यूआरएल की जांच करें।"
                    else
                        "Paste any website URL before clicking it to evaluate phishing, fake banking portals, and malware download risks.",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary,
                    lineHeight = 20.sp
                )

                // Input Field
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        if (isEvaluated) {
                            isEvaluated = false
                            verdict = null
                        }
                    },
                    placeholder = {
                        Text(
                            text = "https://example.com/login...",
                            color = SuSagiColors.TextMuted,
                            style = SuSagiTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    shape = SuSagiShape.md,
                    trailingIcon = {
                        if (urlInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    urlInput = ""
                                    verdict = null
                                    isEvaluated = false
                                },
                                modifier = Modifier.size(SuSagiSpacing.minTouchTarget)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = SuSagiColors.TextMuted
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SuSagiColors.TextPrimary,
                        unfocusedTextColor = SuSagiColors.TextPrimary,
                        focusedContainerColor = SuSagiColors.SurfaceElevated,
                        unfocusedContainerColor = SuSagiColors.SurfaceElevated,
                        cursorColor = SuSagiColors.Brand,
                        focusedBorderColor = SuSagiColors.Brand,
                        unfocusedBorderColor = SuSagiColors.Border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Evaluation Result
                if (isEvaluated && verdict != null) {
                    val v = verdict!!
                    val riskLevel = when (v.status) {
                        RiskStatus.High -> SuSagiRiskLevel.HIGH
                        RiskStatus.Suspicious -> SuSagiRiskLevel.CAUTION
                        RiskStatus.Low -> SuSagiRiskLevel.LOW
                    }

                    val resultCardBorder = when (riskLevel) {
                        SuSagiRiskLevel.HIGH, SuSagiRiskLevel.CRITICAL -> BorderStroke(1.dp, SuSagiColors.RiskHighBorder)
                        SuSagiRiskLevel.CAUTION -> BorderStroke(1.dp, SuSagiColors.RiskCautionBorder)
                        SuSagiRiskLevel.LOW -> BorderStroke(1.dp, SuSagiColors.RiskLowBorder)
                    }

                    val resultCardBg = when (riskLevel) {
                        SuSagiRiskLevel.HIGH, SuSagiRiskLevel.CRITICAL -> SuSagiColors.RiskHighSoft
                        SuSagiRiskLevel.CAUTION -> SuSagiColors.RiskCautionSoft
                        SuSagiRiskLevel.LOW -> SuSagiColors.RiskLowSoft
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = SuSagiShape.sm,
                        colors = CardDefaults.cardColors(containerColor = resultCardBg),
                        border = resultCardBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (riskLevel) {
                                        SuSagiRiskLevel.HIGH, SuSagiRiskLevel.CRITICAL -> if (isHindi) "खतरनाक लिंक" else "Malicious Link Detected"
                                        SuSagiRiskLevel.CAUTION -> if (isHindi) "संदिग्ध लिंक" else "Suspicious Link"
                                        SuSagiRiskLevel.LOW -> if (isHindi) "कोई संदिग्ध संकेत नहीं" else "No suspicious signals found"
                                    },
                                    style = SuSagiTheme.typography.title,
                                    color = when (riskLevel) {
                                        SuSagiRiskLevel.HIGH, SuSagiRiskLevel.CRITICAL -> SuSagiColors.RiskHigh
                                        SuSagiRiskLevel.CAUTION -> SuSagiColors.RiskCaution
                                        SuSagiRiskLevel.LOW -> SuSagiColors.RiskLow
                                    },
                                    fontWeight = FontWeight.Bold
                                )

                                RiskBadge(
                                    level = riskLevel,
                                    size = RiskBadgeSize.Small
                                )
                            }

                            Text(
                                text = v.detail,
                                style = SuSagiTheme.typography.bodyMedium,
                                color = SuSagiColors.TextPrimary,
                                lineHeight = 18.sp
                            )

                            // Action Guidance
                            val guidance = when (riskLevel) {
                                SuSagiRiskLevel.HIGH, SuSagiRiskLevel.CRITICAL -> if (isHindi)
                                    "सलाह: इस लिंक को न खोलें। कोई भी पासवर्ड या ओटीपी दर्ज न करें।"
                                else
                                    "Recommendation: Do not open this link. Never enter passwords, banking credentials, or personal OTPs."

                                SuSagiRiskLevel.CAUTION -> if (isHindi)
                                    "सलाह: यह लिंक अपना असली पता छिपाता है। सावधान रहें।"
                                else
                                    "Recommendation: This URL hides its true destination. Verify the sender before proceeding."

                                SuSagiRiskLevel.LOW -> if (isHindi)
                                    "सलाह: वर्तमान विश्लेषण में कोई संदिग्ध संकेत नहीं मिला।"
                                else
                                    "Recommendation: No suspicious signals found based on current analysis."
                            }

                            Text(
                                text = guidance,
                                style = SuSagiTheme.typography.caption,
                                color = SuSagiColors.TextSecondary,
                                fontWeight = FontWeight.Medium
                            )

                            // Actions inside result
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SecondarySafetyAction(
                                    text = if (isHindi) "कॉपी करें" else "Copy URL",
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("URL", urlInput))
                                        Toast.makeText(context, if (isHindi) "लिंक कॉपी किया गया" else "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    icon = Icons.Default.ContentCopy,
                                    modifier = Modifier.weight(1f),
                                    height = 36.dp
                                )

                                if (riskLevel != SuSagiRiskLevel.HIGH && riskLevel != SuSagiRiskLevel.CRITICAL) {
                                    PrimarySafetyAction(
                                        text = if (isHindi) "ब्राउज़र में खोलें" else "Open in Browser",
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlInput.trim()))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        icon = Icons.Default.OpenInBrowser,
                                        modifier = Modifier.weight(1f),
                                        height = 36.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isEvaluated) {
                PrimarySafetyAction(
                    text = if (isHindi) "लिंक का विश्लेषण करें" else "Analyze Link",
                    onClick = {
                        val cleanUrl = urlInput.trim()
                        if (cleanUrl.isNotBlank()) {
                            val formattedUrl = if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                                "https://$cleanUrl"
                            } else {
                                cleanUrl
                            }
                            verdict = LinkCheckActivity.evaluateLinkSafety(formattedUrl)
                            isEvaluated = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 44.dp
                )
            } else {
                PrimarySafetyAction(
                    text = if (isHindi) "संपन्न" else "Done",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    height = 44.dp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = if (isHindi) "बंद करें" else "Close",
                    color = SuSagiColors.TextSecondary,
                    style = SuSagiTheme.typography.bodyMedium
                )
            }
        }
    )
}
