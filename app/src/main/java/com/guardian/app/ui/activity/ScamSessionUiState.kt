package com.guardian.app.ui.activity

import com.guardian.app.ui.design.SuSagiRiskLevel

/* SCAM SESSION RUNTIME AGGREGATION: NOT AVAILABLE */
/* Runtime incident detail is built strictly from individual CallHistoryEntry fields.
 * Rich multi-channel cross-event session aggregation is UI PREVIEW ONLY until
 * platform infrastructure provides multi-channel session correlation. */

/**
 * Types of chronological events that can appear within an incident timeline.
 * Presentation-only representation.
 */
enum class ScamTimelineEventType {
    CALL,
    MESSAGE,
    LINK,
    QR,
    VERIFICATION,
    ACTION,
    SYSTEM
}

/**
 * Presentation-only model for an individual step in an incident timeline.
 */
data class ScamTimelineEvent(
    val timestamp: String,
    val title: String,
    val detail: String,
    val type: ScamTimelineEventType
)

/**
 * Human-readable detail presentation model for an incident or session.
 */
data class ScamSessionDetailUiModel(
    val incidentId: Long,
    val title: String,
    val riskLevel: SuSagiRiskLevel,
    val riskScore: Int,
    val callerNumber: String,
    val timestampFormatted: String,
    val summary: String,
    val timelineEvents: List<ScamTimelineEvent>,
    val transcript: String?,
    val detectedSignals: List<String>,
    val actionTaken: String?,
    val outcome: String,
    val recommendedFollowUp: String,
    val isAggregatedSession: Boolean = false // false in runtime!
) {
    companion object {
        /**
         * Builds an honest [ScamSessionDetailUiModel] from an individual [ActivityItemUiModel]
         * using strictly available runtime fields.
         */
        fun fromActivityItem(item: ActivityItemUiModel, isHindi: Boolean = false): ScamSessionDetailUiModel {
            val events = mutableListOf<ScamTimelineEvent>()

            // Event 1: Incoming call initiation
            events.add(
                ScamTimelineEvent(
                    timestamp = item.formattedTimestamp,
                    title = if (isHindi) "इनकमिंग कॉल प्राप्त हुई" else "Incoming call received",
                    detail = if (isHindi) "कॉलर नंबर: ${item.callerNumber}" else "Caller number: ${item.callerNumber}",
                    type = ScamTimelineEventType.CALL
                )
            )

            // Event 2: Risk patterns detected (if any)
            if (item.topSignals.isNotEmpty()) {
                events.add(
                    ScamTimelineEvent(
                        timestamp = item.formattedTimestamp,
                        title = if (isHindi) "संकेतों का पता चला" else "Suspicious patterns identified",
                        detail = item.topSignals.joinToString(" · "),
                        type = ScamTimelineEventType.SYSTEM
                    )
                )
            }

            // Event 3: Defensive Action taken
            val isBlocked = item.actionTaken.contains("block", ignoreCase = true)
            val actionTitle = if (isBlocked) {
                if (isHindi) "नंबर ब्लॉक किया गया" else "Number blocked"
            } else {
                if (isHindi) "कॉल समाप्त हुई" else "Call ended"
            }
            events.add(
                ScamTimelineEvent(
                    timestamp = item.formattedTimestamp,
                    title = actionTitle,
                    detail = if (isBlocked) {
                        if (isHindi) "भविष्य की सुरक्षा के लिए नंबर को ब्लॉक सूची में जोड़ा गया" else "Number added to blocklist for future protection"
                    } else {
                        if (isHindi) "कॉल सत्र पूरा हुआ और स्थानीय रूप से सुरक्षित किया गया" else "Call session concluded and logged locally"
                    },
                    type = ScamTimelineEventType.ACTION
                )
            )

            // Recommended follow-up
            val followUp = when (item.riskLevel) {
                SuSagiRiskLevel.CRITICAL -> {
                    if (isHindi)
                        "पैसे न भेजें और संवेदनशील जानकारी साझा न करें। यदि आपने विवरण साझा किए हैं, तो तुरंत अपने बैंक से संपर्क करें।"
                    else
                        "Do not send money or share sensitive information. If you shared any banking details, contact your bank immediately."
                }
                SuSagiRiskLevel.HIGH -> {
                    if (isHindi)
                        "इस कॉलर से दोबारा संपर्क न करें। किसी भी अनुरोध की आधिकारिक हेल्पलाइन से स्वतंत्र रूप से पुष्टि करें।"
                    else
                        "Do not return calls to this number. Independently verify any claims through official published channels."
                }
                SuSagiRiskLevel.CAUTION -> {
                    if (isHindi)
                        "सावधानी बरतें। बिना पुष्टि के कोई संवेदनशील विवरण या OTP साझा न करें।"
                    else
                        "Exercise caution. Never share OTPs, PINs, or credentials with incoming callers."
                }
                SuSagiRiskLevel.LOW -> {
                    if (isHindi)
                        "किसी अनुवर्ती कार्रवाई की आवश्यकता नहीं है। कॉल सुरक्षित बातचीत के अनुरूप थी।"
                    else
                        "No action required. Call adhered to safe conversational patterns."
                }
            }

            val outcome = when (item.riskLevel) {
                SuSagiRiskLevel.CRITICAL -> {
                    if (isHindi) "पहचान सत्यापित नहीं हो सकी — उच्च जोखिम" else "Identity could not be verified — High risk"
                }
                SuSagiRiskLevel.HIGH -> {
                    if (isHindi) "संभावित प्रतिरूपण या घोटाला कॉल" else "Possible scam or impersonation call"
                }
                SuSagiRiskLevel.CAUTION -> {
                    if (isHindi) "असामान्य बातचीत पैटर्न" else "Unusual conversational patterns"
                }
                SuSagiRiskLevel.LOW -> {
                    if (isHindi) "सामान्य सुरक्षित कॉल" else "Standard protected call"
                }
            }

            return ScamSessionDetailUiModel(
                incidentId = item.id,
                title = item.title,
                riskLevel = item.riskLevel,
                riskScore = item.riskScore,
                callerNumber = item.callerNumber,
                timestampFormatted = item.formattedTimestamp,
                summary = item.contextLine,
                timelineEvents = events,
                transcript = item.transcriptSummary.ifBlank { null },
                detectedSignals = item.topSignals,
                actionTaken = item.actionTaken,
                outcome = outcome,
                recommendedFollowUp = followUp,
                isAggregatedSession = false
            )
        }
    }
}
