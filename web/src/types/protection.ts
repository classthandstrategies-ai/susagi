/**
 * SuSagi Web Companion — Protection Semantics
 *
 * CRITICAL RULE:
 * Capability and connection statuses are distinct from RiskLevel (LOW, CAUTION, HIGH, CRITICAL).
 * Capability status reflects environment and platform availability, never scam risk.
 */

export type CapabilityPlatform = "WEB_COMPANION" | "ANDROID_DEVICE" | "HYBRID";

export type CapabilityStatus =
  | "AVAILABLE"
  | "NOT_CONNECTED"
  | "DEVICE_ONLY"
  | "UNAVAILABLE"
  | "SETUP_REQUIRED";

export interface ProtectionCapability {
  id: string;
  name: string;
  tagline: string;
  description: string;
  platform: CapabilityPlatform;
  status: CapabilityStatus;
  statusLabel: string;
  whereItRuns: string;
  whatItDoes: string;
  whatYouCanDo: string;
  primaryActionLabel?: string;
  primaryActionHref?: string;
  isInteractiveOnWeb?: boolean;
}

export type WebCompanionState =
  | "READY"
  | "NOT_CONNECTED"
  | "ATTENTION_REQUIRED"
  | "OFFLINE";

export interface WebProtectionOverview {
  state: WebCompanionState;
  headline: string;
  statusDescription: string;
  deviceSyncStatus: string;
  isDeviceConnected: boolean;
  activeShieldsCount: number;
  totalShieldsCount: number;
}
