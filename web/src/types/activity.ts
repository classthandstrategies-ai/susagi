import { RiskLevel, ProtectiveAction, ScamSignal, ScamEvidence } from "./risk";

export interface IncidentTimelineItem {
  id: string;
  timestamp: string;
  title: string;
  detail: string;
  riskLevel: RiskLevel;
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
}

export interface IncidentDetail {
  id: string;
  title: string;
  source: string;
  timestamp: string;
  riskLevel: RiskLevel;
  status: "ACTIVE" | "RESOLVED" | "BLOCKED" | "REVIEWED";
  recommendedAction: ProtectiveAction;
  summary: string;
  signals: ScamSignal[];
  evidence: ScamEvidence[];
  timeline: IncidentTimelineItem[];
}
