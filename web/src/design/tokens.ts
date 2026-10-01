/**
 * SuSagi Web Design Tokens
 *
 * Visual Direction:
 * - Calm, consumer fintech/safety companion (1Password, Revolut, Wise, Stripe, Airbnb verification).
 * - Warm off-white page, crisp white cards, soft warm gray secondary surfaces.
 * - Deep ink navy brand accents, restrained semantic color.
 * - Whitespace replacing borders everywhere.
 * - "SuSagi should look quieter as the product becomes smarter, and become simpler—not denser—as risk becomes higher."
 */

export const colors = {
  // Base Surfaces (Warm off-white / light neutral)
  void: "#F4F4F2",
  base: "#FAF9F6", // Primary screen background
  surface: "#FFFFFF", // Primary card container surface
  surfaceElevated: "#F5F5F3", // Soft warm gray secondary surface
  surfaceHighlight: "#EAEAE6", // Subtle hover/active surface

  // Borders & Dividers
  borderSubtle: "#F0EFEA", // Soft hairline dividers
  border: "#E5E5DF", // Standard subtle borders
  borderFocused: "#0F172A", // Active/focused borders

  // Brand Accent (Deep ink navy)
  brand: "#0F172A",
  brandLight: "#334155",
  brandDark: "#020617",
  brandSoft: "rgba(15, 23, 42, 0.05)",

  // Semantic Risk Tokens (Restrained, calm, consumer)
  riskLow: "#15803D", // Calm green
  riskLowSoft: "#F0FDF4",
  riskLowBorder: "#DCFCE7",

  riskCaution: "#B45309", // Amber
  riskCautionSoft: "#FFFBEB",
  riskCautionBorder: "#FEF3C7",

  riskHigh: "#C2410C", // Warm orange
  riskHighSoft: "#FFF7ED",
  riskHighBorder: "#FFEDD5",

  riskCritical: "#B91C1C", // Controlled red (reserved for immediate high-stakes actions)
  riskCriticalSoft: "#FEF2F2",
  riskCriticalBorder: "#FEE2E2",

  // Typography / Content Contrast
  textPrimary: "#18181B", // Near-black ink: Primary headlines & body
  textSecondary: "#52525B", // Neutral gray: Secondary descriptions & labels
  textMuted: "#71717A", // Secondary meta & timestamps
  textDisabled: "#A1A1AA", // Disabled controls
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
  cardPadding: "24px",
  screenPadding: "24px",
  topBarHeight: "64px",
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
  sm: "0 1px 2px 0 rgba(0, 0, 0, 0.04)",
  md: "0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.03)",
  lg: "0 10px 15px -3px rgba(0, 0, 0, 0.06), 0 4px 6px -4px rgba(0, 0, 0, 0.03)",
  card: "0 1px 3px 0 rgba(0, 0, 0, 0.04), 0 1px 2px -1px rgba(0, 0, 0, 0.03)",
  elevated: "0 10px 25px -5px rgba(0, 0, 0, 0.06), 0 8px 10px -6px rgba(0, 0, 0, 0.03)",
} as const;

export const motion = {
  durationFast: "150ms",
  durationNormal: "200ms",
  durationSlow: "300ms",
  easeStandard: "cubic-bezier(0.2, 0.0, 0.0, 1.0)",
  easeEmphasized: "cubic-bezier(0.05, 0.7, 0.1, 1.0)",
} as const;

export const breakpoints = {
  mobile: 375,
  tablet: 768,
  desktop: 1024,
  wide: 1440,
  maxContentWidth: "1040px",
  readingWidth: "680px",
} as const;

export const typography = {
  fontSans:
    '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
  fontMono:
    'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace',
  sizes: {
    display: { size: "32px", lineHeight: "40px", weight: "700" },
    headline: { size: "24px", lineHeight: "32px", weight: "600" },
    title: { size: "18px", lineHeight: "26px", weight: "600" },
    bodyLarge: { size: "16px", lineHeight: "24px", weight: "400" },
    bodyMedium: { size: "15px", lineHeight: "22px", weight: "400" },
    labelLarge: { size: "14px", lineHeight: "20px", weight: "500" },
    labelMedium: { size: "13px", lineHeight: "18px", weight: "500" },
    caption: { size: "12px", lineHeight: "16px", weight: "400" },
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
