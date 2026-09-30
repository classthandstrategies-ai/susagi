package com.guardian.app.ui.settings

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Presentation model for a specific operational permission needed for protection.
 */
data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val permissionKey: String? = null
)

/**
 * UI State for the production SuSagi Settings Screen.
 */
data class SettingsUiState(
    val isHindi: Boolean = false,
    val phoneProtectionEnabled: Boolean = true,
    val messageProtectionEnabled: Boolean = true,
    val linkProtectionEnabled: Boolean = true,
    val voiceSynthesisDetectionEnabled: Boolean = true,
    val permissions: List<PermissionItem> = emptyList(),
    val appVersion: String = "Version 1.0.0",
    val showInternalDemoTools: Boolean = false
) {
    companion object {
        /**
         * Inspects real runtime permission and preferences state without fabricating values.
         */
        fun fromRuntime(
            context: Context,
            isHindi: Boolean,
            phoneEnabled: Boolean = true,
            messageEnabled: Boolean = true
        ): SettingsUiState {
            val hasPhone = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val hasMic = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val hasOverlay = Settings.canDrawOverlays(context)

            val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true

            val perms = listOf(
                PermissionItem(
                    id = "phone",
                    title = if (isHindi) "कॉल सुरक्षा एक्सेस" else "Call Protection Access",
                    description = if (isHindi)
                        "इनकमिंग कॉल आने पर सुरक्षा शुरू करने के लिए आवश्यक"
                    else
                        "Recognizes incoming calls to activate protection",
                    isGranted = hasPhone,
                    permissionKey = android.Manifest.permission.READ_PHONE_STATE
                ),
                PermissionItem(
                    id = "mic",
                    title = if (isHindi) "माइक्रोफ़ोन एक्सेस" else "Microphone Access",
                    description = if (isHindi)
                        "स्पीकरफ़ोन कॉल के दौरान ऑन-डिवाइस आवाज़ विश्लेषण के लिए"
                    else
                        "Analyzes audio speech patterns on speakerphone",
                    isGranted = hasMic,
                    permissionKey = android.Manifest.permission.RECORD_AUDIO
                ),
                PermissionItem(
                    id = "overlay",
                    title = if (isHindi) "अन्य ऐप्स के ऊपर दिखाएं" else "Display Over Other Apps",
                    description = if (isHindi)
                        "सक्रिय कॉल के दौरान सुरक्षा मार्गदर्शन दिखाने के लिए"
                    else
                        "Shows safety guidance during active phone calls",
                    isGranted = hasOverlay
                ),
                PermissionItem(
                    id = "notif",
                    title = if (isHindi) "सूचनाएं" else "Notifications",
                    description = if (isHindi)
                        "संदिग्ध संदेश और तत्काल अलर्ट दिखाने के लिए"
                    else
                        "Delivers urgent scam and message warnings",
                    isGranted = hasNotif,
                    permissionKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        android.Manifest.permission.POST_NOTIFICATIONS
                    } else null
                )
            )

            val prefs = context.getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)
            val voiceSynth = prefs.getBoolean("voice_synthesis_enabled", true)

            return SettingsUiState(
                isHindi = isHindi,
                phoneProtectionEnabled = phoneEnabled,
                messageProtectionEnabled = messageEnabled,
                linkProtectionEnabled = true,
                voiceSynthesisDetectionEnabled = voiceSynth,
                permissions = perms
            )
        }
    }
}
