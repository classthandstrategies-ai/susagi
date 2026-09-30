package com.guardian.app.ui.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * SuSagi Color Tokens
 *
 * Design Direction:
 * - Trustworthy, calm, modern, banking-grade protective palette.
 * - Restrained dark slate/navy foundation (no neon cyber aesthetic).
 * - Clear semantic hierarchy:
 *     LOW: Calm protective emerald green
 *     CAUTION: Alert amber
 *     HIGH: Elevated warning orange
 *     CRITICAL: Emergency crimson red (strictly reserved for confirmed scams/danger)
 *
 * CRITICAL RULE: Red is NEVER the default product color.
 */
object SuSagiColors {
    // ----------------------------------------------------
    // Base Surfaces (Restrained obsidian / slate neutral dark)
    // ----------------------------------------------------
    val Void = Color(0xFF0B0E14)             // Deepest foundation background
    val Base = Color(0xFF111622)             // Primary screen background
    val Surface = Color(0xFF182030)          // Card container surface
    val SurfaceElevated = Color(0xFF202B40)  // Elevated cards, bottom sheets, modals
    val SurfaceHighlight = Color(0xFF26334D) // Hover/highlighted card state

    // Borders & Dividers
    val Border = Color(0xFF2D3A54)           // Standard component borders
    val BorderSubtle = Color(0xFF222C40)     // Low-contrast hairline dividers
    val BorderFocused = Color(0xFF3B82F6)    // Active/focused borders

    // ----------------------------------------------------
    // Brand Accent (Protective, calm, banking-grade cobalt)
    // ----------------------------------------------------
    val Brand = Color(0xFF3B82F6)            // Primary SuSagi brand blue
    val BrandLight = Color(0xFF60A5FA)       // Light brand accent
    val BrandDark = Color(0xFF2563EB)        // Dark/pressed brand accent
    val BrandSoft = Color(0x1F3B82F6)        // 12% alpha for subtle brand containers

    // ----------------------------------------------------
    // Semantic Risk Tokens
    // ----------------------------------------------------
    // LOW / Protected: Calm emerald green (not harsh lime neon)
    val RiskLow = Color(0xFF10B981)
    val RiskLowSoft = Color(0x1A10B981)      // 10% alpha container
    val RiskLowBorder = Color(0x4D10B981)    // 30% alpha border

    // CAUTION: Alert warm amber
    val RiskCaution = Color(0xFFF59E0B)
    val RiskCautionSoft = Color(0x1AF59E0B)  // 10% alpha container
    val RiskCautionBorder = Color(0x4DF59E0B)// 30% alpha border

    // HIGH: Elevated warning orange
    val RiskHigh = Color(0xFFF97316)
    val RiskHighSoft = Color(0x1AF97316)     // 10% alpha container
    val RiskHighBorder = Color(0x4DF97316)   // 30% alpha border

    // CRITICAL: Emergency crimson red (strictly for urgent threats)
    val RiskCritical = Color(0xFFEF4444)
    val RiskCriticalSoft = Color(0x24EF4444) // 14% alpha container
    val RiskCriticalBorder = Color(0x66EF4444)// 40% alpha border

    // ----------------------------------------------------
    // Typography / Content Contrast
    // ----------------------------------------------------
    val TextPrimary = Color(0xFFF8FAFC)      // Slate 50: Primary headlines & body
    val TextSecondary = Color(0xFFCBD5E1)    // Slate 300: Secondary instructions & labels
    val TextMuted = Color(0xFF94A3B8)        // Slate 400: Timestamps, captions (contrast ratio > 4.5:1)
    val TextDisabled = Color(0xFF64748B)     // Slate 500: Disabled controls
    val TextOnBrand = Color(0xFFFFFFFF)      // High-contrast text on brand buttons
    val TextOnDanger = Color(0xFFFFFFFF)     // High-contrast text on critical buttons
}

@Immutable
data class SuSagiColorScheme(
    val void: Color = SuSagiColors.Void,
    val base: Color = SuSagiColors.Base,
    val surface: Color = SuSagiColors.Surface,
    val surfaceElevated: Color = SuSagiColors.SurfaceElevated,
    val surfaceHighlight: Color = SuSagiColors.SurfaceHighlight,
    val border: Color = SuSagiColors.Border,
    val borderSubtle: Color = SuSagiColors.BorderSubtle,
    val brand: Color = SuSagiColors.Brand,
    val brandLight: Color = SuSagiColors.BrandLight,
    val brandSoft: Color = SuSagiColors.BrandSoft,
    val riskLow: Color = SuSagiColors.RiskLow,
    val riskLowSoft: Color = SuSagiColors.RiskLowSoft,
    val riskCaution: Color = SuSagiColors.RiskCaution,
    val riskCautionSoft: Color = SuSagiColors.RiskCautionSoft,
    val riskHigh: Color = SuSagiColors.RiskHigh,
    val riskHighSoft: Color = SuSagiColors.RiskHighSoft,
    val riskCritical: Color = SuSagiColors.RiskCritical,
    val riskCriticalSoft: Color = SuSagiColors.RiskCriticalSoft,
    val textPrimary: Color = SuSagiColors.TextPrimary,
    val textSecondary: Color = SuSagiColors.TextSecondary,
    val textMuted: Color = SuSagiColors.TextMuted,
    val textDisabled: Color = SuSagiColors.TextDisabled
)

val LocalSuSagiColors = staticCompositionLocalOf { SuSagiColorScheme() }
