package com.guardian.app.ui.design

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Accessor object for SuSagi Design System tokens in Composable scope.
 */
object SuSagiTheme {
    val colors: SuSagiColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalSuSagiColors.current

    val typography: SuSagiTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalSuSagiTypography.current

    val spacing: SuSagiSpacing
        get() = SuSagiSpacing

    val shapes: SuSagiShape
        get() = SuSagiShape

    val elevation: SuSagiElevation
        get() = SuSagiElevation
}

private val SuSagiMaterialDarkColorScheme = darkColorScheme(
    primary = SuSagiColors.Brand,
    onPrimary = SuSagiColors.TextOnBrand,
    primaryContainer = SuSagiColors.SurfaceElevated,
    onPrimaryContainer = SuSagiColors.TextPrimary,
    secondary = SuSagiColors.RiskLow,
    onSecondary = SuSagiColors.Void,
    background = SuSagiColors.Base,
    onBackground = SuSagiColors.TextPrimary,
    surface = SuSagiColors.Surface,
    onSurface = SuSagiColors.TextPrimary,
    surfaceVariant = SuSagiColors.SurfaceElevated,
    onSurfaceVariant = SuSagiColors.TextSecondary,
    outline = SuSagiColors.Border,
    outlineVariant = SuSagiColors.BorderSubtle,
    error = SuSagiColors.RiskCritical,
    onError = SuSagiColors.TextOnDanger
)

/**
 * Root theme for SuSagi V1.
 * Establishes calm banking-grade aesthetics, accessibility baseline, and system bars.
 */
@Composable
fun SuSagiTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SuSagiColors.Base.toArgb()
                window.navigationBarColor = SuSagiColors.Void.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalSuSagiColors provides SuSagiColorScheme(),
        LocalSuSagiTypography provides SuSagiTypography()
    ) {
        MaterialTheme(
            colorScheme = SuSagiMaterialDarkColorScheme,
            content = content
        )
    }
}
