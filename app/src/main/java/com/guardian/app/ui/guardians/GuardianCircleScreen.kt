package com.guardian.app.ui.guardians

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.components.EmptyState
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Production SuSagi Guardian Circle Destination
 *
 * Implements the Guardian Circle product experience:
 * - Shows configured trusted emergency contacts backed by genuine runtime storage
 * - Clear, accessible presentation of automated alert posture (>75% risk SMS)
 * - Educational guidance on out-of-band identity verification
 * - Seamless modal flow for adding, updating, or removing trusted contacts
 */
@Composable
fun GuardianCircleScreen(
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(GuardianCircleUiState.fromRuntime(context)) }
    var showManageModal by remember { mutableStateOf(false) }
    var selectedContactForEdit by remember { mutableStateOf<GuardianUiModel?>(null) }

    val refreshState = {
        uiState = GuardianCircleUiState.fromRuntime(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(rememberScrollState())
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isHindi) "विश्वसनीय सुरक्षा घेरा" else "Guardian Circle",
                style = SuSagiTheme.typography.display,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = if (isHindi)
                    "विश्वसनीय संपर्क जो प्रतिरूपण से बचाते हैं और आपातकालीन अलर्ट प्राप्त करते हैं।"
                else
                    "Trusted contacts who protect you from impersonation and receive emergency alerts.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary
            )
        }

        // Active Contacts or Empty State
        if (uiState.hasGuardians) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.guardians.forEach { guardian ->
                    GuardianCard(
                        guardian = guardian,
                        onManage = {
                            selectedContactForEdit = guardian
                            showManageModal = true
                        },
                        isHindi = isHindi
                    )
                }

                // Information banner regarding single contact in V1
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SuSagiShape.card,
                    colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
                    border = BorderStroke(1.dp, SuSagiColors.Border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SuSagiColors.Brand,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (isHindi)
                                "सुसागी V1 वास्तविक समय के आपातकालीन अलर्ट के लिए 1 प्राथमिक संपर्क का समर्थन करता है।"
                            else
                                "SuSagi V1 currently routes high-risk emergency SMS alerts to 1 primary trusted contact.",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextSecondary
                        )
                    }
                }
            }
        } else {
            // Empty State
            EmptyState(
                title = if (isHindi) "कोई विश्वसनीय संपर्क नहीं जोड़ा गया" else "No Guardian Added Yet",
                description = if (isHindi)
                    "किसी परिवार के सदस्य या विश्वसनीय मित्र को जोड़ें जो सुरक्षा अलर्ट प्राप्त कर सकता है।"
                else
                    "Add a family member or trusted friend who can receive safety alerts.",
                icon = Icons.Default.People
            )

            PrimarySafetyAction(
                text = if (isHindi) "विश्वसनीय संपर्क जोड़ें" else "Add Trusted Contact",
                onClick = {
                    selectedContactForEdit = null
                    showManageModal = true
                },
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Educational Section: How Protection Works
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = SuSagiShape.card,
            colors = CardDefaults.cardColors(containerColor = SuSagiColors.Surface),
            border = BorderStroke(1.dp, SuSagiColors.Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SuSagiSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isHindi) "संरक्षण कैसे काम करता है" else "How Guardian Circle Protects You",
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )

                // Feature 1: Safety Alerts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SuSagiColors.BrandSoft,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = SuSagiColors.Brand,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "सुरक्षा अलर्ट" else "Safety Alerts",
                            style = SuSagiTheme.typography.bodyMedium,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isHindi)
                                "आपका विश्वसनीय व्यक्ति सुरक्षा अलर्ट प्राप्त कर सकता है।"
                            else
                                "Your trusted person can receive safety alerts.",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Feature 2: Identity Verification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SuSagiColors.BrandSoft,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SuSagiColors.Brand,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "पहचान सत्यापन" else "Identity Verification",
                            style = SuSagiTheme.typography.bodyMedium,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isHindi)
                                "भविष्य के सत्यापन प्रवाह में, सुसागी उनसे संवेदनशील अनुरोध की पुष्टि करने के लिए कह सकता है।"
                            else
                                "In a future verification flow, SuSagi can ask them to confirm a sensitive request.",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet
    if (showManageModal) {
        ManageTrustedContactModal(
            existingContact = selectedContactForEdit,
            onDismiss = { showManageModal = false },
            onContactUpdated = {
                refreshState()
                showManageModal = false
            },
            isHindi = isHindi
        )
    }
}

// ============================================================================
// COMPOSE PREVIEWS
// ============================================================================

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Guardian Circle — Empty State", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun GuardianCircleEmptyPreview() {
    SuSagiTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Guardian Circle",
                style = SuSagiTheme.typography.display,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            EmptyState(
                title = "No Guardian Added Yet",
                description = "Add a family member or trusted friend who can receive safety alerts.",
                icon = Icons.Default.People
            )
            PrimarySafetyAction(
                text = "Add Trusted Contact",
                onClick = {},
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "Guardian Circle — Single Active Contact (Genuine Runtime Shape)", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun GuardianCircleActivePreview() {
    SuSagiTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Guardian Circle",
                style = SuSagiTheme.typography.display,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            GuardianCard(
                guardian = GuardianUiModel(
                    id = "primary",
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
@Preview(name = "Guardian Circle — Multi-Contact Preview (Conceptual/Preview)", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun GuardianCircleMultiPreview() {
    SuSagiTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Guardian Circle",
                style = SuSagiTheme.typography.display,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            GuardianCard(
                guardian = GuardianUiModel(
                    id = "preview_1",
                    name = "Priya Sharma",
                    phone = "+91 98765 43210",
                    status = GuardianStatus.ACTIVE,
                    isEnabled = true,
                    relationship = "Primary Contact"
                ),
                onManage = {}
            )
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
