import { RiskLevel, ProtectiveAction, ScamSignal, ScamEvidence } from "./risk";

export type ActivityChannel = "CALL" | "MESSAGE" | "LINK" | "QR" | "VERIFICATION";

export type TimelineEventType =
  | "CALL"
  | "MESSAGE"
  | "LINK"
  | "QR"
  | "VERIFICATION"
  | "ACTION"
  | "SYSTEM";

export interface IncidentTimelineItem {
  id: string;
  timestamp: string;
  title: string;
  detail: string;
  riskLevel: RiskLevel;
  eventType?: TimelineEventType;
}

export interface IncidentItem {
  id: string;
  title: string;
  source: string;
  timestamp: string;
  riskLevel: RiskLevel;
  actionTaken: ProtectiveAction;
  summary: string;
  signalCount: number;
  claimedIdentity?: string;
  channel?: ActivityChannel;
}

export interface IncidentDetail {
  id: string;
  title: string;
  source: string;
  timestamp: string;
  riskLevel: RiskLevel;
  status: "ACTIVE" | "RESOLVED" | "BLOCKED" | "REVIEWED";
  recommendedAction: ProtectiveAction;
  additionalActions?: ProtectiveAction[];
  summary: string;
  signals: ScamSignal[];
  evidence: ScamEvidence[];
  timeline: IncidentTimelineItem[];
  claimedIdentity?: string;
  channel?: ActivityChannel;
  outcome?: string;
}
