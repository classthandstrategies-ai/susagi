package com.guardian.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.guardian.app.ui.navigation.SuSagiAppShell

enum class AppScreen(val title: String) {
    Home("Home"),
    Events("Audit Log"),
    Incidents("Threats"),
    Settings("Settings")
}

data class GuardianEvent(
    val title: String,
    val detail: String,
    val time: String,
    val safe: Boolean,
    val category: String = "SYSTEM"
)

data class GuardianIncident(
    val title: String,
    val detail: String,
    val time: String,
    val risk: String,
    val threatType: String = "Fraud Attempt"
)

data class GuardianState(
    val name: String = "",
    val signedIn: Boolean = false,
    val onboardingComplete: Boolean = false,
    val protectionEnabled: Boolean = true,
    val phoneProtection: Boolean = true,
    val messageProtection: Boolean = true,
    val linkProtection: Boolean = true,
    val events: List<GuardianEvent> = listOf(
        GuardianEvent("Real-Time Shield Online", "Speakerphone & SMS threat detectors are running", "Just now", true, "SHIELD"),
        GuardianEvent("Continuous STT Initialized", "English, Hindi & Hinglish models ready", "Just now", true, "SPEECH"),
        GuardianEvent("Background Protection Active", "Monitoring incoming calls and message payloads", "Today", true, "MONITOR")
    ),
    val incidents: List<GuardianIncident> = emptyList()
)

/**
 * Root Composable Application Controller
 *
 * Hosts the SuSagi product navigation shell, authentication gate, and working entry point wires.
 */
@Composable
fun GuardianApp(
    onOpenCallRisk: () -> Unit = {},
    onOpenQrScanner: () -> Unit = {},
    onPhoneProtectionToggle: (Boolean) -> Unit = {},
    onMessageProtectionToggle: (Boolean) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var state by remember {
        mutableStateOf(
            GuardianState(
                name = SessionManager.getUserName(context).ifBlank { "SuSagi User" },
                signedIn = SessionManager.isLoggedIn(context),
                onboardingComplete = SessionManager.isOnboardingComplete(context)
            )
        )
    }
    var showCallHistory by remember { mutableStateOf(false) }

    when {
        !state.signedIn -> AuthScreen(
            onContinue = { name ->
                val finalName = name.ifBlank { "SuSagi User" }
                SessionManager.saveSession(context, finalName, "user@susagi.defense")
                state = state.copy(name = finalName, signedIn = true)
            }
        )

        !state.onboardingComplete -> OnboardingScreen(
            name = state.name,
            onComplete = {
                SessionManager.setOnboardingComplete(context, true)
                state = state.copy(onboardingComplete = true)
            }
        )

        showCallHistory -> com.guardian.app.callprotect.CallHistoryScreen(
            onBack = { showCallHistory = false }
        )

        else -> SuSagiAppShell(
            state = state,
            onProtectionToggle = { enabled ->
                state = state.copy(
                    protectionEnabled = enabled,
                    events = listOf(
                        GuardianEvent(
                            if (enabled) "Shield Protection Re-enabled" else "Shield Protection Paused",
                            if (enabled) "Real-time scam filtering active across all channels"
                            else "On-phone sensor listeners are in standby",
                            "Just now",
                            enabled,
                            "STATE"
                        )
                    ) + state.events
                )
                onPhoneProtectionToggle(enabled)
                onMessageProtectionToggle(enabled)
            },
            onOpenCallRisk = onOpenCallRisk,
            onOpenQrScanner = onOpenQrScanner,
            onOpenCallHistory = { showCallHistory = true },
            onPhoneProtectionToggle = { enabled ->
                state = state.copy(phoneProtection = enabled)
                onPhoneProtectionToggle(enabled)
            },
            onMessageProtectionToggle = { enabled ->
                state = state.copy(messageProtection = enabled)
                onMessageProtectionToggle(enabled)
            }
        )
    }
}