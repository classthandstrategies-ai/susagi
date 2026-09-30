package com.guardian.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.guardian.app.ui.design.SuSagiColors

// Base surfaces (layered banking-grade neutral dark)
val GxVoid = SuSagiColors.Void                  // Deepest background (#0B0E14)
val GxBase = SuSagiColors.Base                  // Screen background (#111622)
val GxSurface = SuSagiColors.Surface            // Cards (#182030)
val GxSurfaceAlt = SuSagiColors.SurfaceElevated // Elevated cards (#202B40)
val GxBorder = SuSagiColors.Border              // Dividers (#2D3A54)

// Brand
val GxPrimary = SuSagiColors.Brand              // Trustworthy brand blue (#3B82F6)
val GxPrimarySoft = SuSagiColors.BrandSoft      // Subtle brand container
val GxPrimaryGlow = Color(0x333B82F6)           // Refined subtle shadow

// Semantic (Calm, high-contrast, not cyber neon)
val GxSafe = SuSagiColors.RiskLow               // Emerald (#10B981)
val GxSafeSoft = SuSagiColors.RiskLowSoft
val GxWarning = SuSagiColors.RiskCaution        // Amber (#F59E0B)
val GxWarningSoft = SuSagiColors.RiskCautionSoft
val GxDanger = SuSagiColors.RiskCritical        // Crimson (#EF4444)
val GxDangerSoft = SuSagiColors.RiskCriticalSoft

// Text (Accessible contrast ratios)
val GxTextHi = SuSagiColors.TextPrimary         // Crisp headline/body (#F8FAFC)
val GxTextMid = SuSagiColors.TextSecondary      // Secondary body (#CBD5E1)
val GxTextLo = SuSagiColors.TextMuted           // Metadata & timestamps (#94A3B8)
