package com.guardian.app.ui.activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.guardian.app.ui.components.ErrorState
import com.guardian.app.ui.components.GxChip
import com.guardian.app.ui.components.GxChipVariant
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiRiskLevel
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme
import kotlinx.coroutines.launch

/**
 * Production SuSagi Activity Screen
 *
 * Answers:
 * - What important safety events has SuSagi seen?
 * - What happened during each incident?
 *
 * Implements:
 * - Loading, Empty, Loaded, Error states
 * - Search by caller number or context
 * - Filtering by threat posture
 * - Local routing into detailed Scam Session / Incident view
 * - Preserves link to legacy Call Audit Log for system compatibility
 */
@Composable
fun SuSagiActivityScreen(
    onOpenLegacyCallHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf(ActivityLoadingStatus.LOADING) }
    var items by remember { mutableStateOf<List<ActivityItemUiModel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedDetailItem by remember { mutableStateOf<ActivityItemUiModel?>(null) }

    fun loadData() {
        status = ActivityLoadingStatus.LOADING
        errorMessage = null
        scope.launch {
            try {
                val loaded = ActivityUiState.loadFromRuntime(context, isHindi)
                items = loaded
                status = ActivityLoadingStatus.LOADED
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load activity ledger"
                status = ActivityLoadingStatus.ERROR
            }
        }
    }

    LaunchedEffect(isHindi) {
        loadData()
    }

    // Detail View Routing
    if (selectedDetailItem != null) {
        val detailItem = selectedDetailItem!!
        ScamSessionDetailScreen(
            uiModel = ScamSessionDetailUiModel.fromActivityItem(detailItem, isHindi),
            onBack = { selectedDetailItem = null },
            isHindi = isHindi
        )
        return
    }

    // Main Activity Ledger View
    val filteredItems = items.filter { item ->
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SuSagiColors.Base)
            .padding(SuSagiSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isHindi) "सुरक्षा गतिविधि" else "Activity",
                style = SuSagiTheme.typography.display,
                color = SuSagiColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = if (isHindi)
                    "हालिया सुरक्षा घटनाएं और विश्लेषण रिकॉर्ड।"
                else
                    "Recent safety activity and incident logs.",
                style = SuSagiTheme.typography.bodyMedium,
                color = SuSagiColors.TextSecondary
            )
        }

        // Search Bar (min 48dp height)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = if (isHindi) "कॉलर नंबर या घटना खोजें..." else "Search caller number or threat...",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SuSagiColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = SuSagiShape.card,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SuSagiColors.Surface,
                unfocusedContainerColor = SuSagiColors.Surface,
                focusedBorderColor = SuSagiColors.Brand,
                unfocusedBorderColor = SuSagiColors.Border,
                focusedTextColor = SuSagiColors.TextPrimary,
                unfocusedTextColor = SuSagiColors.TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )

        // Filter Chips Row (min 48dp touch row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf(
                "All" to if (isHindi) "सभी" else "All",
                "Suspicious" to if (isHindi) "संदिग्ध" else "Suspicious",
                "Blocked" to if (isHindi) "अवरोधित" else "Blocked",
                "Protected" to if (isHindi) "सुरक्षित" else "Protected"
            )

            filterOptions.forEach { (filterKey, displayLabel) ->
                val isSelected = selectedFilter == filterKey
                GxChip(
                    text = displayLabel,
                    variant = if (isSelected) GxChipVariant.Brand else GxChipVariant.Neutral,
                    onClick = { selectedFilter = filterKey },
                    height = 36.dp
                )
            }
        }

        // Content Area State Machine
        when (status) {
            ActivityLoadingStatus.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SuSagiColors.Brand,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            ActivityLoadingStatus.ERROR -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(
                        title = if (isHindi) "गतिविधि लोड नहीं हो सकी" else "Activity couldn't be loaded",
                        description = errorMessage ?: if (isHindi)
                            "सुरक्षा रिकॉर्ड लोड करते समय एक त्रुटि हुई।"
                        else
                            "An unexpected error occurred while loading safety records.",
                        retryActionLabel = if (isHindi) "पुनः प्रयास करें" else "Try again",
                        onRetry = { loadData() }
                    )
                }
            }

            ActivityLoadingStatus.LOADED -> {
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (items.isEmpty()) {
                            // Truly empty ledger
                            EmptyState(
                                title = if (isHindi) "अभी कोई सुरक्षा गतिविधि नहीं है" else "No safety activity yet",
                                description = if (isHindi)
                                    "महत्वपूर्ण घोटाले की चेतावनियां और सत्यापन घटनाएं यहां दिखाई देंगी।"
                                else
                                    "Important scam warnings and verification events will appear here.",
                                icon = Icons.Default.Shield
                            )
                        } else {
                            // Filter returned no results
                            EmptyState(
                                title = if (isHindi) "कोई मेल नहीं मिला" else "No matching incidents",
                                description = if (isHindi)
                                    "वर्तमान फ़िल्टर या खोज मानदंड से मेल खाने वाला कोई रिकॉर्ड नहीं है।"
                                else
                                    "No safety records matched your search query or filter.",
                                icon = Icons.Default.Search
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            ActivityItemCard(
                                item = item,
                                onClick = { selectedDetailItem = item },
                                isHindi = isHindi
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS
// ============================================================================

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "PreviewActivityEmpty", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewActivityEmpty() {
    SuSagiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                title = "No safety activity yet",
                description = "Important scam warnings and verification events will appear here.",
                icon = Icons.Default.Shield
            )
        }
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "PreviewActivityWithIncidents", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewActivityWithIncidents() {
    SuSagiTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActivityItemCard(
                item = ActivityItemUiModel(
                    id = 1L,
                    callerNumber = "+91 98765 00112",
                    formattedTimestamp = "Today · 2:07 PM",
                    timestampMillis = System.currentTimeMillis(),
                    title = "Possible impersonation call",
                    riskLevel = SuSagiRiskLevel.HIGH,
                    riskScore = 82,
                    contextLine = "Caller claimed to be from State Bank of India and requested sensitive verification information.",
                    topSignals = listOf("KYC Suspension", "OTP Solicitation"),
                    transcriptSummary = "Please share 6-digit authorization code.",
                    actionTaken = "CALL_ENDED",
                    wasReported = false
                ),
                onClick = {}
            )

            ActivityItemCard(
                item = ActivityItemUiModel(
                    id = 2L,
                    callerNumber = "+91 91234 56789",
                    formattedTimestamp = "Yesterday · 4:15 PM",
                    timestampMillis = System.currentTimeMillis() - 86400000L,
                    title = "Protected call",
                    riskLevel = SuSagiRiskLevel.LOW,
                    riskScore = 12,
                    contextLine = "Adhered to safe conversational patterns.",
                    topSignals = emptyList(),
                    transcriptSummary = "Standard delivery coordination call.",
                    actionTaken = "NORMAL",
                    wasReported = false
                ),
                onClick = {}
            )
        }
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "PreviewActivityLoading", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewActivityLoading() {
    SuSagiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = SuSagiColors.Brand,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

/* UI PREVIEW DATA — NOT RUNTIME DATA */
@Preview(name = "PreviewActivityError", showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewActivityError() {
    SuSagiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SuSagiColors.Base),
            contentAlignment = Alignment.Center
        ) {
            ErrorState(
                title = "Activity couldn't be loaded",
                description = "An unexpected error occurred while loading safety records.",
                retryActionLabel = "Try again",
                onRetry = {}
            )
        }
    }
}
