package com.guardian.app.ui.home

import androidx.compose.runtime.Immutable
import com.guardian.app.GuardianState
import com.guardian.app.ui.design.SuSagiRiskLevel

/**
 * Dominant protection status presentation state for SuSagi Home.
 *
 * NOTE: Strictly presentation-only. Derived from real runtime state.
 * Never introduces fake metrics or telemetry scoring.
 */
enum class HomeProtectionStatus(
    val title: String,
    val description: String,
    val hindiTitle: String,
    val hindiDescription: String
) {
    ACTIVE(
        title = "Protection Active",
        description = "SuSagi is actively safeguarding your calls, messages, and links.",
        hindiTitle = "सुरक्षा सक्रिय",
        hindiDescription = "सुसागी आपके कॉल, संदेशों और लिंक की सक्रिय रूप से सुरक्षा कर रहा है।"
    ),
    PARTIALLY_PROTECTED(
        title = "Partial Protection",
        description = "Some defense shields are disabled. Turn them on for complete coverage.",
        hindiTitle = "आंशिक सुरक्षा",
        hindiDescription = "कुछ सुरक्षा शील्ड बंद हैं। संपूर्ण सुरक्षा के लिए उन्हें चालू करें।"
    ),
    ATTENTION_REQUIRED(
        title = "Action Required",
        description = "Security permissions need your approval to enable full protection.",
        hindiTitle = "कार्रवाई आवश्यक",
        hindiDescription = "पूर्ण सुरक्षा सक्षम करने के लिए सुरक्षा अनुमतियों की आवश्यकता है।"
    ),
    PAUSED(
        title = "Protection Paused",
        description = "Scam defense is paused. Turn protection on to stay safeguarded.",
        hindiTitle = "सुरक्षा रुकी हुई है",
        hindiDescription = "घोटाला सुरक्षा रुकी हुई है। सुरक्षित रहने के लिए सुरक्षा चालू करें।"
    ),
    OFFLINE(
        title = "Offline Protection Active",
        description = "Local on-device rules are protecting you without an active internet connection.",
        hindiTitle = "ऑफ़लाइन सुरक्षा सक्रिय",
        hindiDescription = "इंटरनेट के बिना भी स्थानीय ऑन-डिवाइस नियम आपकी सुरक्षा कर रहे हैं।"
    )
}

/**
 * Protection capability presentation item for Call, Message, or Link defense.
 */
@Immutable
data class ProtectionCapabilityItem(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val isProtected: Boolean,
    val explanation: String,
    val hindiExplanation: String
)

/**
 * Meaningful recent safety activity (e.g. flagged scam call or confirmed threat).
 * Filtered to exclude internal developer logs (STT init, detector setup, etc.).
 */
@Immutable
data class HomeRecentActivity(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val riskLevel: SuSagiRiskLevel
)

/**
 * UI State for SuSagi Home Screen.
 * Only contains values supported by genuine application runtime state.
 */
@Immutable
data class HomeUiState(
    val userName: String = "User",
    val overallStatus: HomeProtectionStatus = HomeProtectionStatus.ACTIVE,
    val isProtectionEnabled: Boolean = true,
    val isPhoneProtected: Boolean = true,
    val isMessageProtected: Boolean = true,
    val isLinkProtected: Boolean = true,
    val capabilities: List<ProtectionCapabilityItem> = emptyList(),
    val recentActivities: List<HomeRecentActivity> = emptyList()
) {
    companion object {
        /**
         * Pure mapper from GuardianState to HomeUiState.
         * Enforces strict honesty: no fabricated statistics, threat counts, or fake percentages.
         */
        fun fromGuardianState(state: GuardianState): HomeUiState {
            val status = when {
                !state.protectionEnabled -> HomeProtectionStatus.PAUSED
                state.phoneProtection && state.messageProtection && state.linkProtection -> HomeProtectionStatus.ACTIVE
                !state.phoneProtection || !state.messageProtection || !state.linkProtection -> HomeProtectionStatus.PARTIALLY_PROTECTED
                else -> HomeProtectionStatus.ACTIVE
            }

            val capabilities = listOf(
                ProtectionCapabilityItem(
                    id = "calls",
                    title = "Call Protection",
                    hindiTitle = "कॉल सुरक्षा",
                    isProtected = state.protectionEnabled && state.phoneProtection,
                    explanation = if (state.protectionEnabled && state.phoneProtection)
                        "Live analysis for impersonation and urgency deception"
                    else "Call screening is turned off",
                    hindiExplanation = if (state.protectionEnabled && state.phoneProtection)
                        "पहचान प्रतिरूपण और दबाव के खिलाफ लाइव विश्लेषण"
                    else "कॉल स्क्रीनिंग बंद है"
                ),
                ProtectionCapabilityItem(
                    id = "messages",
                    title = "Message Protection",
                    hindiTitle = "संदेश सुरक्षा",
                    isProtected = state.protectionEnabled && state.messageProtection,
                    explanation = if (state.protectionEnabled && state.messageProtection)
                        "Detects fraudulent OTP requests and fake alerts"
                    else "Message monitoring is turned off",
                    hindiExplanation = if (state.protectionEnabled && state.messageProtection)
                        "धोखाधड़ी वाले ओटीपी अनुरोधों और फर्जी अलर्ट का पता लगाता है"
                    else "संदेश निगरानी बंद है"
                ),
                ProtectionCapabilityItem(
                    id = "links",
                    title = "Link Protection",
                    hindiTitle = "लिंक सुरक्षा",
                    isProtected = state.protectionEnabled && state.linkProtection,
                    explanation = if (state.protectionEnabled && state.linkProtection)
                        "Checks suspicious web links before you open them"
                    else "Link checking is turned off",
                    hindiExplanation = if (state.protectionEnabled && state.linkProtection)
                        "संदिग्ध वेब लिंक को खोलने से पहले जांचता है"
                    else "लिंक सत्यापन बंद है"
                )
            )

            // Filter incidents for genuine user-meaningful activities only
            // Deliberately drops developer/telemetry logs ("Continuous STT Initialized", etc.)
            val activities = state.incidents.mapIndexed { index, incident ->
                HomeRecentActivity(
                    id = "incident-$index",
                    title = incident.title,
                    description = incident.detail,
                    timestamp = incident.time,
                    riskLevel = when {
                        incident.risk.contains("high", ignoreCase = true) -> SuSagiRiskLevel.HIGH
                        incident.risk.contains("critical", ignoreCase = true) -> SuSagiRiskLevel.CRITICAL
                        else -> SuSagiRiskLevel.CAUTION
                    }
                )
            }

            return HomeUiState(
                userName = state.name.ifBlank { "User" },
                overallStatus = status,
                isProtectionEnabled = state.protectionEnabled,
                isPhoneProtected = state.phoneProtection,
                isMessageProtected = state.messageProtection,
                isLinkProtected = state.linkProtection,
                capabilities = capabilities,
                recentActivities = activities
            )
        }
    }
}
