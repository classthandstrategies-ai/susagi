package com.guardian.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.guardian.app.ui.components.RiskBadge
import com.guardian.app.ui.components.RiskBadgeSize
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Production Settings Destination for SuSagi V1.
 *
 * Provides control over:
 * 1. Interface & reasoning language (English / Hindi)
 * 2. Real-time protection permissions and access status
 * 3. Transparent privacy architecture (on-device speech reasoning)
 * 4. Application version and protective baseline
 * 5. Isolated internal demo tools for evaluation and testing
 */
@Composable
fun SuSagiSettingsScreen(
    modifier: Modifier = Modifier,
    isHindi: Boolean = false,
    uiStateOverride: SettingsUiState? = null,
    onToggleHindi: () -> Unit = {},
    onBack: () -> Unit = {},
    onOpenCallRiskDemo: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val uiState = uiStateOverride ?: remember(isHindi) {
        SettingsUiState.fromRuntime(context, isHindi)
    }

    val openSystemSettings = {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    var showDemoTools by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .verticalScroll(scrollState)
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Language Preference Section
        SettingsSection(
            title = if (isHindi) "भाषा प्राथमिकता" else "Language",
            subtitle = if (isHindi) "चेतावनी और सुरक्षा मार्गदर्शन की भाषा चुनें" else "Choose language for warnings and safety guidance"
        ) {
            LanguageSelectorCard(
                isHindi = isHindi,
                onSelectEnglish = { if (isHindi) onToggleHindi() },
                onSelectHindi = { if (!isHindi) onToggleHindi() }
            )
        }

        // Permissions & Operational Capabilities Section
        SettingsSection(
            title = if (isHindi) "सुरक्षा अनुमतियां" else "Protection Permissions",
            subtitle = if (isHindi) "घोटालों और प्रतिरूपण का पता लगाने के लिए आवश्यक सिस्टम एक्सेस" else "Operational system access required to detect threats in real time"
        ) {
            uiState.permissions.forEachIndexed { index, perm ->
                PermissionStatusRow(
                    item = perm,
                    onGrant = openSystemSettings,
                    isHindi = isHindi
                )
                if (index < uiState.permissions.size - 1) {
                    HorizontalDivider(
                        color = SuSagiColors.Border,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            SecondarySafetyAction(
                text = if (isHindi) "सिस्टम अनुमतियां प्रबंधित करें" else "Manage System Permissions",
                onClick = openSystemSettings,
                height = 42.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Privacy & Data Governance Section
        SettingsSection(
            title = if (isHindi) "डेटा और गोपनीयता" else "Privacy & Data Protection",
            subtitle = if (isHindi) "SuSagi आपकी व्यक्तिगत जानकारी की सुरक्षा कैसे करता है" else "How SuSagi safeguards your personal information"
        ) {
            PrivacyPrincipleRow(
                icon = Icons.Default.Lock,
                title = if (isHindi) "माइक्रोफ़ोन एक्सेस का सीमित उपयोग" else "Microphone Usage",
                description = if (isHindi)
                    "SuSagi माइक्रोफ़ोन एक्सेस का उपयोग केवल तभी करता है जब सक्षम सुरक्षा सुविधाओं को इसकी आवश्यकता होती है।"
                else
                    "SuSagi uses microphone access only when enabled protection features need it to analyze supported calls."
            )

            HorizontalDivider(color = SuSagiColors.Border, thickness = 0.5.dp)

            PrivacyPrincipleRow(
                icon = Icons.Default.Security,
                title = if (isHindi) "केवल सुरक्षा सिग्नल्स" else "Protective Threat Signals Only",
                description = if (isHindi)
                    "केवल घोटाले के संकेतक, दबाव के पैटर्न और वित्तीय मांगें जांची जाती हैं ताकि आपको चेतावनी दी जा सके।"
                else
                    "Only suspicious behavioral patterns, urgency markers, and payment demands are evaluated to deliver safety warnings."
            )

            HorizontalDivider(color = SuSagiColors.Border, thickness = 0.5.dp)

            PrivacyPrincipleRow(
                icon = Icons.Default.Info,
                title = if (isHindi) "पूर्ण उपयोगकर्ता नियंत्रण" else "Full User Control",
                description = if (isHindi)
                    "आप किसी भी समय सुरक्षा बंद कर सकते हैं या सिस्टम सेटिंग्स में जाकर अनुमतियां हटा सकते हैं।"
                else
                    "You remain in control. You can pause protection or revoke any permission at any time in Android settings."
            )
        }

        // About SuSagi Section
        SettingsSection(
            title = if (isHindi) "SuSagi के बारे में" else "About SuSagi",
            subtitle = if (isHindi) "संस्करण और सुरक्षा विवरण" else "Application version and defense profile"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SuSagi Scam Defense",
                        style = SuSagiTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuSagiColors.TextPrimary
                    )
                    Text(
                        text = uiState.appVersion,
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextSecondary
                    )
                }

                RiskBadge(
                    level = SuSagiRiskLevel.LOW,
                    customLabel = uiState.appVersion,
                    size = RiskBadgeSize.Small
                )
            }

            Text(
                text = if (isHindi)
                    "SuSagi परिवारों को सामाजिक इंजीनियरिंग घोटालों, डिजिटल अरेस्ट के दबाव, वित्तीय धोखाधड़ी और पहचान की चोरी से बचाने के लिए बनाया गया है।"
                else
                    "Built to protect individuals and families against social engineering, digital arrest coercion, financial fraud, and impersonation.",
                style = SuSagiTheme.typography.caption,
                color = SuSagiColors.TextSecondary,
                lineHeight = 18.sp
            )
        }

        // Hidden Internal Demo Section (preserves hackathon evaluation callbacks without exposing them in normal consumer settings)
        if (uiState.showInternalDemoTools) {
            InternalDemoSection(
                isOpen = showDemoTools,
                onToggleOpen = { showDemoTools = !showDemoTools },
                onLaunchCallRisk = onOpenCallRiskDemo,
                isHindi = isHindi
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Language selection cards for English and Hindi.
 */
@Composable
private fun LanguageSelectorCard(
    isHindi: Boolean,
    onSelectEnglish: () -> Unit,
    onSelectHindi: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LanguageOption(
            title = "English",
            nativeTitle = "English",
            flag = "🇬🇧",
            isSelected = !isHindi,
            onClick = onSelectEnglish,
            modifier = Modifier.weight(1f)
        )

        LanguageOption(
            title = "हिंदी",
            nativeTitle = "Hindi",
            flag = "🇮🇳",
            isSelected = isHindi,
            onClick = onSelectHindi,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LanguageOption(
    title: String,
    nativeTitle: String,
    flag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = SuSagiShape.md,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SuSagiColors.SurfaceElevated else SuSagiColors.Surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SuSagiColors.Brand else SuSagiColors.Border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = flag, fontSize = 20.sp)
                Column {
                    Text(
                        text = title,
                        style = SuSagiTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SuSagiColors.Brand else SuSagiColors.TextPrimary
                    )
                    Text(
                        text = nativeTitle,
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextSecondary
                    )
                }
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = SuSagiColors.Brand,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = SuSagiColors.Base,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPrincipleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = SuSagiColors.SurfaceElevated,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SuSagiColors.Brand,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = SuSagiTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SuSagiColors.TextPrimary
            )
            Text(
                text = description,
                style = SuSagiTheme.typography.caption,
                color = SuSagiColors.TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * Isolated internal evaluation section.
 * Clearly demarcated as NOT for consumer operation.
 */
@Composable
private fun InternalDemoSection(
    isOpen: Boolean,
    onToggleOpen: () -> Unit,
    onLaunchCallRisk: () -> Unit,
    isHindi: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SuSagiShape.card,
        colors = CardDefaults.cardColors(containerColor = SuSagiColors.SurfaceElevated),
        border = BorderStroke(1.dp, SuSagiColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SuSagiSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleOpen),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = SuSagiColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "INTERNAL DEMO (EVALUATION ONLY)",
                        style = SuSagiTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = SuSagiColors.TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = if (isOpen) "Hide" else "Show",
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.Brand,
                    fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedVisibility(visible = isOpen) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "These testing utilities trigger simulated threat analysis and test overlays. Keep disabled during normal defense operation.",
                        style = SuSagiTheme.typography.caption,
                        color = SuSagiColors.TextSecondary
                    )

                    SecondarySafetyAction(
                        text = "Launch Call Risk Simulation Screen",
                        onClick = onLaunchCallRisk,
                        height = 40.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS (UI PREVIEW DATA — NOT RUNTIME DATA)
// ============================================================================

@Preview(name = "Settings - English", showBackground = true)
@Composable
private fun PreviewSuSagiSettingsEnglish() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiSettingsScreen(
            isHindi = false
        )
    }
}

@Preview(name = "Settings - Hindi", showBackground = true)
@Composable
private fun PreviewSuSagiSettingsHindi() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiSettingsScreen(
            isHindi = true
        )
    }
}

@Preview(name = "Settings - Permissions Missing", showBackground = true)
@Composable
private fun PreviewSuSagiSettingsPermissionsMissing() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiSettingsScreen(
            isHindi = false,
            uiStateOverride = SettingsUiState(
                isHindi = false,
                permissions = listOf(
                    PermissionItem(
                        id = "phone",
                        title = "Call Protection Access",
                        description = "Recognizes incoming calls to activate protection",
                        isGranted = false
                    ),
                    PermissionItem(
                        id = "mic",
                        title = "Microphone Access",
                        description = "Analyzes audio speech patterns on speakerphone",
                        isGranted = false
                    ),
                    PermissionItem(
                        id = "overlay",
                        title = "Display Over Other Apps",
                        description = "Shows safety guidance during active phone calls",
                        isGranted = false
                    ),
                    PermissionItem(
                        id = "notif",
                        title = "Notifications",
                        description = "Delivers urgent scam and message warnings",
                        isGranted = true
                    )
                )
            )
        )
    }
}
