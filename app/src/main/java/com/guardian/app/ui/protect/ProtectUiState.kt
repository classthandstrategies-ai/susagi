package com.guardian.app.ui.protect

import com.guardian.app.GuardianState
import com.guardian.app.ui.design.ProtectionState

/**
 * CapabilityStatus
 *
 * Current operational status of an individual defense capability.
 */
enum class CapabilityStatus {
    ACTIVE,
    PAUSED,
    NEEDS_ATTENTION,
    AVAILABLE
}

/**
 * CapabilityActionType
 *
 * The interaction mechanism for the capability card.
 */
enum class CapabilityActionType {
    TOGGLE,
    ACTION_BUTTON
}

/**
 * ProtectionCapabilityUiModel
 *
 * Presentation model for an individual SuSagi protection capability.
 */
data class ProtectionCapabilityUiModel(
    val id: String,
    val title: String,
    val titleHi: String,
    val description: String,
    val descriptionHi: String,
    val status: CapabilityStatus,
    val isEnabled: Boolean,
    val permissionRequiredMessage: String? = null,
    val permissionRequiredMessageHi: String? = null,
    val privacyAssurance: String? = null,
    val privacyAssuranceHi: String? = null,
    val actionType: CapabilityActionType = CapabilityActionType.TOGGLE,
    val actionLabel: String? = null,
    val actionLabelHi: String? = null
)

/**
 * ProtectUiState
 *
 * UI State for the SuSagi Protect destination screen.
 * Grounded in real GuardianState runtime capabilities.
 */
data class ProtectUiState(
    val overallStatus: ProtectionState = ProtectionState.ACTIVE,
    val activeProtectionCount: Int = 4,
    val totalProtectionCount: Int = 4,
    val isPhoneProtected: Boolean = true,
    val isMessageProtected: Boolean = true,
    val isLinkProtected: Boolean = true,
    val isQrAvailable: Boolean = true,
    val capabilities: List<ProtectionCapabilityUiModel> = emptyList()
) {
    companion object {
        fun fromGuardianState(state: GuardianState): ProtectUiState {
            val isPhoneOn = state.protectionEnabled && state.phoneProtection
            val isMessageOn = state.protectionEnabled && state.messageProtection
            val isLinkOn = state.linkProtection
            val isQrOn = true // QR scanner is always available on-device via CameraX/ML Kit

            val activeCount = listOf(isPhoneOn, isMessageOn, isLinkOn, isQrOn).count { it }

            val overall = when {
                !state.protectionEnabled -> ProtectionState.PAUSED
                !isPhoneOn || !isMessageOn -> ProtectionState.ATTENTION_REQUIRED
                else -> ProtectionState.ACTIVE
            }

            val items = listOf(
                ProtectionCapabilityUiModel(
                    id = "calls",
                    title = "Call Protection",
                    titleHi = "कॉल सुरक्षा",
                    description = "Live speech analysis during phone calls to detect impersonation, digital arrest extortion, and urgent payment pressure.",
                    descriptionHi = "फ़ोन कॉल के दौरान वॉयस विश्लेषण जो प्रतिरूपण, डिजिटल गिरफ्तारी और जबरन भुगतान के दबाव का पता लगाता है।",
                    status = if (!state.protectionEnabled) CapabilityStatus.PAUSED else if (isPhoneOn) CapabilityStatus.ACTIVE else CapabilityStatus.NEEDS_ATTENTION,
                    isEnabled = isPhoneOn,
                    permissionRequiredMessage = "Phone state and notification permissions are required to monitor incoming calls for scam patterns.",
                    permissionRequiredMessageHi = "इनकमिंग कॉल में घोटाले के पैटर्न की पहचान करने के लिए फोन और नोटिफिकेशन अनुमतियां आवश्यक हैं।",
                    privacyAssurance = "SuSagi only uses the access needed for enabled protection features.",
                    privacyAssuranceHi = "सुसागी केवल सक्षम सुरक्षा सुविधाओं के लिए आवश्यक पहुंच का उपयोग करता है।",
                    actionType = CapabilityActionType.TOGGLE
                ),
                ProtectionCapabilityUiModel(
                    id = "messages",
                    title = "Message Protection",
                    titleHi = "संदेश सुरक्षा",
                    description = "Screens incoming notifications and SMS for fraudulent payment links, fake bank alerts, and credential-harvesting scams.",
                    descriptionHi = "धोखाधड़ी वाले भुगतान लिंक, नकली बैंक अलर्ट और क्रेडेंशियल-चोरी के लिए एसएमएस और नोटिफिकेशन की जांच करता है।",
                    status = if (!state.protectionEnabled) CapabilityStatus.PAUSED else if (isMessageOn) CapabilityStatus.ACTIVE else CapabilityStatus.NEEDS_ATTENTION,
                    isEnabled = isMessageOn,
                    permissionRequiredMessage = "Notification access permission is required so SuSagi can screen SMS and app alerts.",
                    permissionRequiredMessageHi = "नोटिफिकेशन एक्सेस अनुमति आवश्यक है ताकि सुसागी एसएमएस और ऐप अलर्ट की जांच कर सके।",
                    privacyAssurance = "SuSagi only uses notification access to detect known scam patterns.",
                    privacyAssuranceHi = "सुसागी केवल ज्ञात घोटाले के पैटर्न का पता लगाने के लिए नोटिफिकेशन पहुंच का उपयोग करता है।",
                    actionType = CapabilityActionType.TOGGLE
                ),
                ProtectionCapabilityUiModel(
                    id = "links",
                    title = "Link Protection",
                    titleHi = "लिंक सुरक्षा",
                    description = "Verifies web links before opening them to block phishing portals, rogue APK downloads, and spoofed bank logins.",
                    descriptionHi = "फ़िशिंग पोर्टल, अनधिकृत एपीके डाउनलोड और नकली बैंक लॉगिन को ब्लॉक करने के लिए वेब लिंक की पुष्टि करता है।",
                    status = CapabilityStatus.AVAILABLE,
                    isEnabled = isLinkOn,
                    privacyAssurance = "SuSagi only evaluates URLs when you verify them or open external links.",
                    privacyAssuranceHi = "सुसागी केवल तभी यूआरएल का मूल्यांकन करता है जब आप उन्हें सत्यापित करते हैं।",
                    actionType = CapabilityActionType.ACTION_BUTTON,
                    actionLabel = "Verify Link",
                    actionLabelHi = "लिंक जांचें"
                ),
                ProtectionCapabilityUiModel(
                    id = "qr",
                    title = "QR Shield",
                    titleHi = "क्यूआर शील्ड",
                    description = "Scans QR codes to prevent malicious payment redirection, disguised UPI IDs, and dangerous website downloads.",
                    descriptionHi = "दुर्भावनापूर्ण भुगतान पुनर्निर्देशन, छिपी हुई यूपीआई आईडी और खतरनाक वेबसाइट डाउनलोड को रोकने के लिए क्यूआर कोड स्कैन करता है।",
                    status = CapabilityStatus.AVAILABLE,
                    isEnabled = true,
                    privacyAssurance = "Camera access is used only while the scanner is actively open.",
                    privacyAssuranceHi = "कैमरा का उपयोग केवल स्कैनर खुला होने पर ही किया जाता है।",
                    actionType = CapabilityActionType.ACTION_BUTTON,
                    actionLabel = "Scan QR",
                    actionLabelHi = "क्यूआर स्कैन करें"
                )
            )

            val backgroundActiveCount = listOf(isPhoneOn, isMessageOn).count { it }

            return ProtectUiState(
                overallStatus = overall,
                activeProtectionCount = backgroundActiveCount,
                totalProtectionCount = 2,
                isPhoneProtected = isPhoneOn,
                isMessageProtected = isMessageOn,
                isLinkProtected = isLinkOn,
                isQrAvailable = isQrOn,
                capabilities = items
            )
        }
    }
}
