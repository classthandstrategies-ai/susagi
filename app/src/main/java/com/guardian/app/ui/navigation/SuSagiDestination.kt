package com.guardian.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Primary navigation destinations for SuSagi V1.
 *
 * Target product architecture:
 * - HOME: Protection overview, status summary, and quick defensive actions
 * - PROTECT: Active defense tools (Call Screening, Message Shield, Link Check, QR Scanner)
 * - ACTIVITY: Threat audit trail (Scam Sessions, Incident History, Evidence Vault)
 * - GUARDIANS: Trusted contact circle and identity verification relationships
 * - SETTINGS: Preferences, permissions, privacy, language, and diagnostics (accessible via TopBar)
 */
enum class SuSagiDestination(
    val route: String,
    val title: String,
    val hindiTitle: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME(
        route = "home",
        title = "Home",
        hindiTitle = "होम",
        icon = Icons.Default.Shield,
        contentDescription = "Home screen, protection overview"
    ),
    PROTECT(
        route = "protect",
        title = "Protect",
        hindiTitle = "सुरक्षा",
        icon = Icons.Default.Security,
        contentDescription = "Protect screen, defense tools and shields"
    ),
    ACTIVITY(
        route = "activity",
        title = "Activity",
        hindiTitle = "गतिविधि",
        icon = Icons.Default.History,
        contentDescription = "Activity screen, incident history and evidence"
    ),
    GUARDIANS(
        route = "guardians",
        title = "Guardians",
        hindiTitle = "संरक्षक",
        icon = Icons.Default.People,
        contentDescription = "Guardians screen, trusted contact circle"
    ),
    SETTINGS(
        route = "settings",
        title = "Settings",
        hindiTitle = "सेटिंग्स",
        icon = Icons.Default.Settings,
        contentDescription = "Settings screen, preferences and permissions"
    );

    companion object {
        /**
         * Primary bottom navigation items (excludes Settings which is accessible via TopBar).
         */
        val primaryBottomNavDestinations = listOf(
            HOME,
            PROTECT,
            ACTIVITY,
            GUARDIANS
        )
    }
}
