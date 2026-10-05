package com.guardian.app.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * SuSagi Navigation Bar
 *
 * Calm, banking-grade bottom navigation enforcing:
 * - 48dp minimum touch target height per tab
 * - Accessible content descriptions and selection semantics
 * - Restrained brand blue indicators without cyber neon glow
 * - No red default states
 * - Hindi script and larger font compatibility
 */
@Composable
fun SuSagiNavigationBar(
    selectedDestination: SuSagiDestination,
    onDestinationSelected: (SuSagiDestination) -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    NavigationBar(
        modifier = modifier
            .border(width = 1.dp, color = SuSagiColors.BorderSubtle)
            .defaultMinSize(minHeight = SuSagiSpacing.minTouchTarget),
        containerColor = SuSagiColors.SurfaceElevated,
        contentColor = SuSagiColors.TextPrimary,
        tonalElevation = 0.dp
    ) {
        SuSagiDestination.primaryBottomNavDestinations.forEach { destination ->
            val isSelected = destination == selectedDestination
            val labelText = if (isHindi) destination.hindiTitle else destination.title

            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.contentDescription,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = labelText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SuSagiColors.Brand,
                    selectedTextColor = SuSagiColors.TextPrimary,
                    indicatorColor = SuSagiColors.BrandSoft,
                    unselectedIconColor = SuSagiColors.TextMuted,
                    unselectedTextColor = SuSagiColors.TextMuted
                ),
                modifier = Modifier
                    .defaultMinSize(minHeight = SuSagiSpacing.minTouchTarget)
                    .semantics {
                        contentDescription = "${destination.title} tab, ${if (isSelected) "selected" else "not selected"}"
                    }
            )
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS (UI PREVIEW DATA — NOT RUNTIME DATA)
// ============================================================================

@Preview(name = "SuSagi Navigation Bar - Home Selected", showBackground = true)
@Composable
private fun PreviewSuSagiNavBarHome() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiNavigationBar(
            selectedDestination = SuSagiDestination.HOME,
            onDestinationSelected = {}
        )
    }
}

@Preview(name = "SuSagi Navigation Bar - Protect Selected", showBackground = true)
@Composable
private fun PreviewSuSagiNavBarProtect() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiNavigationBar(
            selectedDestination = SuSagiDestination.PROTECT,
            onDestinationSelected = {}
        )
    }
}

@Preview(name = "SuSagi Navigation Bar - Activity Selected", showBackground = true)
@Composable
private fun PreviewSuSagiNavBarActivity() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiNavigationBar(
            selectedDestination = SuSagiDestination.ACTIVITY,
            onDestinationSelected = {}
        )
    }
}

@Preview(name = "SuSagi Navigation Bar - Guardians Selected", showBackground = true)
@Composable
private fun PreviewSuSagiNavBarGuardians() {
    SuSagiTheme {
        /* UI PREVIEW DATA — NOT RUNTIME DATA */
        SuSagiNavigationBar(
            selectedDestination = SuSagiDestination.GUARDIANS,
            onDestinationSelected = {}
        )
    }
}
