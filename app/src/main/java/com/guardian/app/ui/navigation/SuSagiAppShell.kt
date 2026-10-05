package com.guardian.app.ui.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.guardian.app.GuardianState
import com.guardian.app.LinkCheckActivity
import com.guardian.app.ui.components.SuSagiTopBar
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme
import com.guardian.app.ui.activity.SuSagiActivityScreen
import com.guardian.app.ui.guardians.GuardianCircleScreen
import com.guardian.app.ui.home.HomeUiState
import com.guardian.app.ui.home.SuSagiHomeScreen
import com.guardian.app.ui.protect.ProtectUiState
import com.guardian.app.ui.protect.SuSagiProtectScreen
import com.guardian.app.ui.settings.SuSagiSettingsScreen

/**
 * SuSagi App Shell
 *
 * Core application architecture shell implementing the SuSagi navigation model:
 * - Top Bar: "SuSagi" branding, protection status subtitle, accessible Settings action.
 * - Primary Bottom Bar: Home, Protect, Activity, Guardians.
 * - Content Area: Houses the production SuSagiHomeScreen and transitional destination shells.
 * - Preserves all working operational entry points (VoIP Call Risk, QR Scanner, Link Check, Call History).
 */
@Composable
fun SuSagiAppShell(
    modifier: Modifier = Modifier,
    initialDestination: SuSagiDestination = SuSagiDestination.HOME,
    state: GuardianState? = null,
    onProtectionToggle: (Boolean) -> Unit = {},
    onOpenCallRisk: () -> Unit = {},
    onOpenQrScanner: () -> Unit = {},
    onOpenCallHistory: () -> Unit = {},
    onPhoneProtectionToggle: (Boolean) -> Unit = {},
    onMessageProtectionToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(initialDestination) }
    var previousDestination by remember { mutableStateOf(SuSagiDestination.HOME) }
    var isHindi by remember { mutableStateOf(false) }

    val homeUiState = remember(state) {
        state?.let { HomeUiState.fromGuardianState(it) } ?: HomeUiState()
    }

    val protectUiState = remember(state) {
        state?.let { ProtectUiState.fromGuardianState(it) } ?: ProtectUiState()
    }

    val topBarTitle = when (currentDestination) {
        SuSagiDestination.HOME -> "SuSagi"
        SuSagiDestination.PROTECT -> if (isHindi) "सुरक्षा उपकरण" else "Protect"
        SuSagiDestination.ACTIVITY -> if (isHindi) "गतिविधि इतिहास" else "Activity"
        SuSagiDestination.GUARDIANS -> if (isHindi) "संरक्षक नेटवर्क" else "Guardians"
        SuSagiDestination.SETTINGS -> if (isHindi) "सेटिंग्स" else "Settings"
    }

    val topBarSubtitle = when (currentDestination) {
        SuSagiDestination.HOME -> if (isHindi) "रीयल-टाइम घोटाला सुरक्षा" else "Real-time scam defense"
        SuSagiDestination.PROTECT -> if (isHindi) "सक्रिय कॉल और संदेश शील्ड" else "Live speech & message shields"
        SuSagiDestination.ACTIVITY -> if (isHindi) "विश्लेषण सत्र और साक्ष्य लॉग" else "Analyzed sessions & incident evidence"
        SuSagiDestination.GUARDIANS -> if (isHindi) "विश्वसनीय संपर्क सत्यापन" else "Trusted emergency contact circle"
        SuSagiDestination.SETTINGS -> if (isHindi) "भाषा और सुरक्षा प्राथमिकताएं" else "Language, privacy & permissions"
    }

    val isSettingsOpen = currentDestination == SuSagiDestination.SETTINGS

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SuSagiTopBar(
                title = topBarTitle,
                subtitle = topBarSubtitle,
                onBack = if (isSettingsOpen) {
                    { currentDestination = previousDestination }
                } else null,
                backContentDescription = "Return to navigation",
                actions = {
                    if (!isSettingsOpen) {
                        IconButton(
                            onClick = {
                                previousDestination = currentDestination
                                currentDestination = SuSagiDestination.SETTINGS
                            },
                            modifier = Modifier.size(SuSagiSpacing.minTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Open Settings",
                                tint = SuSagiColors.TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!isSettingsOpen) {
                SuSagiNavigationBar(
                    selectedDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        currentDestination = destination
                    },
                    isHindi = isHindi
                )
            }
        },
        containerColor = SuSagiColors.Base
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "susagi-destination-transition",
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base)
                .padding(paddingValues)
        ) { target ->
            when (target) {
                SuSagiDestination.HOME -> SuSagiHomeScreen(
                    uiState = homeUiState,
                    onToggleProtection = onProtectionToggle,
                    onOpenCallRisk = onOpenCallRisk,
                    onOpenQrScanner = onOpenQrScanner,
                    onOpenLinkCheck = {
                        try {
                            context.startActivity(Intent(context, LinkCheckActivity::class.java))
                        } catch (_: Exception) {}
                    },
                    onOpenCallHistory = { currentDestination = SuSagiDestination.ACTIVITY },
                    onNavigateToGuardians = { currentDestination = SuSagiDestination.GUARDIANS },
                    onNavigateToProtect = { currentDestination = SuSagiDestination.PROTECT },
                    isHindi = isHindi
                )

                SuSagiDestination.PROTECT -> SuSagiProtectScreen(
                    uiState = protectUiState,
                    onPhoneProtectionToggle = onPhoneProtectionToggle,
                    onMessageProtectionToggle = onMessageProtectionToggle,
                    onOpenCallRisk = onOpenCallRisk,
                    onOpenQrScanner = onOpenQrScanner,
                    isHindi = isHindi
                )

                SuSagiDestination.ACTIVITY -> SuSagiActivityScreen(
                    onOpenLegacyCallHistory = onOpenCallHistory,
                    isHindi = isHindi
                )

                SuSagiDestination.GUARDIANS -> GuardianCircleScreen(
                    isHindi = isHindi
                )

                SuSagiDestination.SETTINGS -> SuSagiSettingsScreen(
                    isHindi = isHindi,
                    onToggleHindi = { isHindi = !isHindi },
                    onBack = { currentDestination = previousDestination },
                    onOpenCallRiskDemo = onOpenCallRisk
                )
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS (UI PREVIEW DATA — NOT RUNTIME DATA)
// ============================================================================

@Preview(name = "SuSagi Shell - Home Selected", showBackground = true)
@Composable
private fun PreviewSuSagiAppShellHome() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiAppShell(
            initialDestination = SuSagiDestination.HOME
        )
    }
}

@Preview(name = "SuSagi Shell - Protect Selected", showBackground = true)
@Composable
private fun PreviewSuSagiAppShellProtect() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiAppShell(
            initialDestination = SuSagiDestination.PROTECT
        )
    }
}

@Preview(name = "SuSagi Shell - Activity Selected", showBackground = true)
@Composable
private fun PreviewSuSagiAppShellActivity() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiAppShell(
            initialDestination = SuSagiDestination.ACTIVITY
        )
    }
}

@Preview(name = "SuSagi Shell - Guardians Selected", showBackground = true)
@Composable
private fun PreviewSuSagiAppShellGuardians() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiAppShell(
            initialDestination = SuSagiDestination.GUARDIANS
        )
    }
}

@Preview(name = "SuSagi Shell - Settings Selected", showBackground = true)
@Composable
private fun PreviewSuSagiAppShellSettings() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiAppShell(
            initialDestination = SuSagiDestination.SETTINGS
        )
    }
}
