package com.guardian.app.ui.design

import androidx.compose.runtime.Immutable

/**
 * Presentation-Only Semantic Risk Level for SuSagi UI.
 *
 * NOTE: This is purely for display and contains NO risk scoring or calculation logic.
 * It will map directly to the shared Intelligence contract (RiskLevel) once integrated.
 */
enum class SuSagiRiskLevel(
    val title: String,
    val hindiTitle: String
) {
    LOW(
        title = "Protected",
        hindiTitle = "सुरक्षित"
    ),
    CAUTION(
        title = "Caution",
        hindiTitle = "सावधान"
    ),
    HIGH(
        title = "High Risk",
        hindiTitle = "उच्च जोखिम"
    ),
    CRITICAL(
        title = "Critical Scam Alert",
        hindiTitle = "गंभीर घोटाला चेतावनी"
    )
}

/**
 * Presentation model for displaying a structured risk summary adhering to the principle:
 * WHAT IS HAPPENING -> WHY IT IS RISKY -> WHAT THE USER SHOULD DO NEXT
 */
@Immutable
data class SuSagiRiskSummaryModel(
    val riskLevel: SuSagiRiskLevel,
    val headline: String,
    val explanation: String,
    val recommendedAction: String,
    val hindiHeadline: String? = null,
    val hindiExplanation: String? = null,
    val hindiRecommendedAction: String? = null
)

/**
 * Presentation model for displaying an individual scam signal in SignalCard.
 */
@Immutable
data class ScamSignalItem(
    val id: String,
    val title: String,
    val description: String,
    val severity: SuSagiRiskLevel = SuSagiRiskLevel.CAUTION,
    val timestamp: String? = null,
    val highlightedText: String? = null
)

/**
 * Presentation model for scam evidence captured during calls or interactions.
 */
@Immutable
data class ScamEvidenceItem(
    val id: String,
    val title: String,
    val callerOrSource: String,
    val timestamp: String,
    val transcriptSnippet: String? = null,
    val severity: SuSagiRiskLevel = SuSagiRiskLevel.CAUTION,
    val tags: List<String> = emptyList()
)

/**
 * Defense system status presentation state.
 */
enum class ProtectionState {
    ACTIVE,
    PAUSED,
    ATTENTION_REQUIRED,
    OFFLINE
}
