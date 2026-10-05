package com.guardian.app.ui.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * SuSagi Typography Tokens
 *
 * Prioritizes safety and readability:
 * - Body copy is 15-16sp minimum for effortless legibility under stress.
 * - Critical instructions are 18sp+ with strong hierarchy.
 * - Minimum caption size is 12sp (no tiny 10-11sp text for meaningful information).
 * - Line heights are comfortably spaced (>=1.35x) to accommodate Hindi (Devanagari) matras.
 * - Clean sans-serif by default; monospace only for raw phone numbers or audit hashes.
 */
@Immutable
data class SuSagiTypography(
    // Display & Major Titles
    val display: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = SuSagiColors.TextPrimary
    ),
    val headline: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = SuSagiColors.TextPrimary
    ),
    val title: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = SuSagiColors.TextPrimary
    ),

    // Critical Safety Instructions (Prominent, high legibility)
    val criticalInstruction: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = SuSagiColors.TextPrimary
    ),

    // Body Text (Primary content, >= 15sp)
    val bodyLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = SuSagiColors.TextSecondary
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = SuSagiColors.TextSecondary
    ),

    // Labels & Buttons
    val buttonLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = SuSagiColors.TextOnBrand
    ),
    val labelLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = SuSagiColors.TextSecondary
    ),
    val labelMedium: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = SuSagiColors.TextSecondary
    ),

    // Captions & Secondary Metadata (>= 12sp, never 10sp)
    val caption: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = SuSagiColors.TextMuted
    ),

    // Monospace (Strictly for phone numbers, verification codes, or technical hashes)
    val code: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = SuSagiColors.TextPrimary
    )
)

val LocalSuSagiTypography = staticCompositionLocalOf { SuSagiTypography() }
