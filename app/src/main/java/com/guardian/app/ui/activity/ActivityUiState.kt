package com.guardian.app.ui.activity

import android.content.Context
import android.text.format.DateUtils
import com.guardian.app.callprotect.CallHistoryEntry
import com.guardian.app.callprotect.NumberReputationRepository
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.live.LiveDefenseUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Loading status of Activity ledger.
 */
enum class ActivityLoadingStatus {
    LOADING,
    LOADED,
    ERROR
}

/**
 * Human-readable presentation model for an activity ledger item.
 *
 * Mapped strictly from real runtime [CallHistoryEntry] data:
 * - What happened (human title)
 * - When (accessible timestamp)
 * - Severity (semantic risk level via centralized mapping)
 * - Context line (human-readable explanation of risk/action)
 */
data class ActivityItemUiModel(
    val id: Long,
    val callerNumber: String,
    val formattedTimestamp: String,
    val timestampMillis: Long,
    val title: String,
    val riskLevel: SuSagiRiskLevel,
    val riskScore: Int,
    val contextLine: String,
    val topSignals: List<String>,
    val transcriptSummary: String,
    val actionTaken: String,
    val wasReported: Boolean
)

/**
 * UI State for the Activity destination.
 */
data class ActivityUiState(
    val status: ActivityLoadingStatus = ActivityLoadingStatus.LOADING,
    val items: List<ActivityItemUiModel> = emptyList(),
    val selectedFilter: String = "All",
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val filteredItems: List<ActivityItemUiModel>
        get() {
            return items.filter { item ->
                val matchesSearch = searchQuery.isBlank() ||
                    item.callerNumber.contains(searchQuery, ignoreCase = true) ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.contextLine.contains(searchQuery, ignoreCase = true)

                val matchesFilter = when (selectedFilter) {
                    "Suspicious" -> item.riskLevel == SuSagiRiskLevel.HIGH || item.riskLevel == SuSagiRiskLevel.CRITICAL
                    "Blocked" -> item.actionTaken.contains("block", ignoreCase = true)
                    "Protected" -> item.riskLevel == SuSagiRiskLevel.LOW
                    else -> true
                }
                matchesSearch && matchesFilter
            }
        }

    val isEmpty: Boolean
        get() = status == ActivityLoadingStatus.LOADED && items.isEmpty()

    companion object {
        /**
         * Converts real [CallHistoryEntry] entities from [NumberReputationRepository]
         * into human-readable [ActivityItemUiModel] objects.
         *
         * DOES NOT FABRICATE DATA:
         * If no records exist in the local database, returns an empty list.
         */
        suspend fun loadFromRuntime(context: Context, isHindi: Boolean = false): List<ActivityItemUiModel> {
            val repo = NumberReputationRepository(context)
            val entries = repo.getAllHistory()
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            return entries.map { entry ->
                // Centralized risk score mapping (no duplicate threshold logic)
                val riskLevel = LiveDefenseUiState.mapRiskScoreToPresentationLevel(entry.riskScore)

                // Human-readable timestamp calculation
                val now = System.currentTimeMillis()
                val isToday = DateUtils.isToday(entry.timestamp)
                val timeStr = timeFormat.format(Date(entry.timestamp))
                val formattedTime = if (isToday) {
                    if (isHindi) "आज · $timeStr" else "Today · $timeStr"
                } else {
                    val dateStr = dateFormat.format(Date(entry.timestamp))
                    "$dateStr · $timeStr"
                }

                // Parse signals list
                val signalsList = if (entry.topSignals.isNotBlank()) {
                    entry.topSignals.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }

                // Human-readable title
                val isBlocked = entry.actionTaken.contains("block", ignoreCase = true)
                val title = when {
                    isBlocked -> {
                        if (isHindi) "अवरोधित कॉलर" else "Blocked caller"
                    }
                    riskLevel == SuSagiRiskLevel.CRITICAL -> {
                        if (isHindi) "संभावित प्रतिरूपण कॉल" else "Possible impersonation call"
                    }
                    riskLevel == SuSagiRiskLevel.HIGH -> {
                        if (isHindi) "संदिग्ध कॉलर" else "Suspicious caller"
                    }
                    riskLevel == SuSagiRiskLevel.CAUTION -> {
                        if (isHindi) "सावधानी संकेत वाली कॉल" else "Call with cautionary signals"
                    }
                    else -> {
                        if (isHindi) "सुरक्षित कॉल" else "Protected call"
                    }
                }

                // One useful context line
                val contextLine = when {
                    isBlocked -> {
                        if (isHindi) "सुरक्षा के लिए नंबर को ब्लॉक कर दिया गया था।" else "Number was blocked for your safety."
                    }
                    signalsList.isNotEmpty() -> {
                        val firstSignal = signalsList.first()
                        if (isHindi) "संकेत मिले: $firstSignal" else "Detected pattern: $firstSignal"
                    }
                    entry.transcriptSummary.isNotBlank() -> {
                        val snippet = entry.transcriptSummary.take(60)
                        if (isHindi) "\"$snippet...\"" else "\"$snippet...\""
                    }
                    riskLevel == SuSagiRiskLevel.LOW -> {
                        if (isHindi) "सामान्य बातचीत के पैटर्न देखे गए।" else "Adhered to safe conversational patterns."
                    }
                    else -> {
                        if (isHindi) "संभावित जोखिम कारक पाए गए।" else "Potential risk factors were identified."
                    }
                }

                ActivityItemUiModel(
                    id = entry.id,
                    callerNumber = entry.number.ifBlank { "Unknown Caller" },
                    formattedTimestamp = formattedTime,
                    timestampMillis = entry.timestamp,
                    title = title,
                    riskLevel = riskLevel,
                    riskScore = entry.riskScore,
                    contextLine = contextLine,
                    topSignals = signalsList,
                    transcriptSummary = entry.transcriptSummary,
                    actionTaken = entry.actionTaken,
                    wasReported = entry.wasUserReported
                )
            }
        }
    }
}
