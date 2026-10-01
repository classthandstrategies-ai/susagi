/**
 * SuSagi Risk Semantics Contract
 *
 * NOTE: These are strictly frontend/API presentation types.
 * DO NOT implement RiskEngine logic.
 * DO NOT calculate authoritative risk in TypeScript.
 * DO NOT create numeric score thresholds that decide RiskLevel.
 * The web companion receives semantic states from authoritative sources.
 */

export type RiskLevel = "LOW" | "CAUTION" | "HIGH" | "CRITICAL";

export type ProtectiveAction =
  | "CONTINUE_MONITORING"
  | "VERIFY_IDENTITY"
  | "END_CALL"
  | "DO_NOT_SHARE_CREDENTIALS"
  | "DO_NOT_SEND_MONEY"
  | "USE_OFFICIAL_CHANNEL"
  | "DO_NOT_INSTALL_REMOTE_ACCESS";

export interface ScamSignal {
  id: string;
  title: string;
  description: string;
  severity: RiskLevel;
  timestamp?: string;
  highlightedText?: string;
  category?: string;
}

export interface ScamEvidence {
  id: string;
  title: string;
  callerOrSource: string;
  timestamp: string;
  transcriptSnippet?: string;
  severity: RiskLevel;
  tags: string[];
}

export interface RiskAssessment {
  id: string;
  riskLevel: RiskLevel;
  headline: string;
  explanation: string;
  recommendedAction: ProtectiveAction;
  actionRationale?: string;
  signals: ScamSignal[];
  evidence?: ScamEvidence[];
  timestamp: string;
  claimedIdentity?: string;
  additionalActions?: ProtectiveAction[];
  hindiHeadline?: string;
  hindiExplanation?: string;
  hindiRecommendedAction?: string;
}

export type ProtectionState =
  | "ACTIVE"
  | "PAUSED"
  | "ATTENTION_REQUIRED"
  | "OFFLINE";

/**
 * Explicit Live UI connection/stream states (NOT RiskLevel values).
 * Normal web companion runtime lands on STANDBY or UNAVAILABLE.
 */
export type LiveConnectionState =
  | "STANDBY"
  | "CONNECTING"
  | "ANALYZING"
  | "ASSESSMENT_AVAILABLE"
  | "UNAVAILABLE"
  | "ERROR";
