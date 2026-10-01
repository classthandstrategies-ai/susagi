/**
 * SuSagi Web Design Tokens
 *
 * Visual Direction:
 * - Calm, premium, protective, trustworthy, serious, banking-grade.
 * - Restrained dark slate/navy foundation (no neon cyber aesthetic).
 * - Clear semantic risk hierarchy (LOW, CAUTION, HIGH, CRITICAL).
 * - CRITICAL RULE: Red is NEVER the default product color.
 */

export const colors = {
  // Base Surfaces (Restrained obsidian / slate neutral dark)
  void: "#0B0E14", // Deepest foundation background
  base: "#111622", // Primary screen background
  surface: "#182030", // Card container surface
  surfaceElevated: "#202B40", // Elevated cards, modals, sheets
  surfaceHighlight: "#26334D", // Hover/active card state

  // Borders & Dividers
  borderSubtle: "#222C40", // Low-contrast hairline dividers
  border: "#2D3A54", // Standard component borders
  borderFocused: "#3B82F6", // Active/focused borders

  // Brand Accent (Protective, calm, banking-grade cobalt)
  brand: "#3B82F6",
  brandLight: "#60A5FA",
  brandDark: "#2563EB",
  brandSoft: "rgba(59, 130, 246, 0.12)",

  // Semantic Risk Tokens
  riskLow: "#10B981", // Calm protective emerald green
  riskLowSoft: "rgba(16, 185, 129, 0.12)",
  riskLowBorder: "rgba(16, 185, 129, 0.35)",

  riskCaution: "#F59E0B", // Alert warm amber
  riskCautionSoft: "rgba(245, 158, 11, 0.12)",
  riskCautionBorder: "rgba(245, 158, 11, 0.35)",

  riskHigh: "#F97316", // Elevated warning orange
  riskHighSoft: "rgba(249, 115, 22, 0.12)",
  riskHighBorder: "rgba(249, 115, 22, 0.35)",

  riskCritical: "#EF4444", // Emergency crimson red (strictly for urgent threats)
  riskCriticalSoft: "rgba(239, 68, 68, 0.16)",
  riskCriticalBorder: "rgba(239, 68, 68, 0.45)",

  // Typography / Content Contrast
  textPrimary: "#F8FAFC", // Slate 50: Primary headlines & body
  textSecondary: "#CBD5E1", // Slate 300: Secondary instructions & labels
  textMuted: "#94A3B8", // Slate 400: Timestamps, captions (contrast > 4.5:1)
  textDisabled: "#64748B", // Slate 500: Disabled controls
  textOnBrand: "#FFFFFF", // High-contrast text on brand buttons
  textOnDanger: "#FFFFFF", // High-contrast text on critical buttons
} as const;

export const spacing = {
  xxs: "2px",
  xs: "4px",
  sm: "8px",
  md: "12px",
  lg: "16px",
  xl: "20px",
  xxl: "24px",
  section: "32px",
  hero: "40px",
  minTouchTarget: "48px",
  cardPadding: "20px",
  screenPadding: "24px",
  topBarHeight: "64px",
  sideNavWidth: "260px",
  bottomNavHeight: "64px",
} as const;

export const radii = {
  xs: "6px",
  sm: "8px",
  md: "12px",
  lg: "16px",
  xl: "24px",
  pill: "9999px",
  card: "16px",
  button: "12px",
  badge: "8px",
  dialog: "20px",
} as const;

export const shadows = {
  none: "none",
  sm: "0 1px 2px 0 rgba(0, 0, 0, 0.3)",
  md: "0 4px 6px -1px rgba(0, 0, 0, 0.4), 0 2px 4px -2px rgba(0, 0, 0, 0.3)",
  lg: "0 10px 15px -3px rgba(0, 0, 0, 0.5), 0 4px 6px -4px rgba(0, 0, 0, 0.4)",
  card: "0 4px 12px rgba(0, 0, 0, 0.25)",
  elevated: "0 12px 28px rgba(0, 0, 0, 0.4)",
  riskCriticalGlow: "0 0 20px rgba(239, 68, 68, 0.15)",
} as const;

export const motion = {
  durationFast: "150ms",
  durationNormal: "250ms",
  durationSlow: "350ms",
  easeStandard: "cubic-bezier(0.2, 0.0, 0.0, 1.0)",
  easeEmphasized: "cubic-bezier(0.05, 0.7, 0.1, 1.0)",
} as const;

export const breakpoints = {
  mobile: 375,
  tablet: 768,
  desktop: 1024,
  wide: 1440,
  maxContentWidth: "1280px",
  wideContentWidth: "1440px",
} as const;

export const typography = {
  fontSans:
    '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
  fontMono:
    'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace',
  sizes: {
    display: { size: "28px", lineHeight: "36px", weight: "700" },
    headline: { size: "22px", lineHeight: "28px", weight: "600" },
    title: { size: "18px", lineHeight: "24px", weight: "600" },
    criticalInstruction: { size: "18px", lineHeight: "26px", weight: "600" },
    bodyLarge: { size: "16px", lineHeight: "24px", weight: "400" },
    bodyMedium: { size: "15px", lineHeight: "22px", weight: "400" },
    labelLarge: { size: "14px", lineHeight: "20px", weight: "500" },
    labelMedium: { size: "13px", lineHeight: "18px", weight: "500" },
    caption: { size: "12px", lineHeight: "16px", weight: "400" },
    code: { size: "14px", lineHeight: "20px", weight: "400" },
  },
} as const;

export const tokens = {
  colors,
  spacing,
  radii,
  shadows,
  motion,
  breakpoints,
  typography,
} as const;

export default tokens;
