import { RiskAssessment } from "@/types/risk";
import { ProtectionStatusSummary } from "@/types/protection";

/**
 * Interface contract for receiving risk assessments from authoritative engines.
 * CRITICAL RULE: Frontend never calculates risk or evaluates numeric scores.
 */
export interface IRiskService {
  getCurrentAssessment(): Promise<RiskAssessment | null>;
  getProtectionSummary(): Promise<ProtectionStatusSummary>;
}

export class OfflineRiskService implements IRiskService {
  async getCurrentAssessment(): Promise<RiskAssessment | null> {
    // In production without live call or active device paired, live assessment is idle
    return null;
  }

  async getProtectionSummary(): Promise<ProtectionStatusSummary> {
    return {
      state: "ACTIVE",
      activeShieldsCount: 4,
      totalShieldsCount: 4,
      lastInspectionTimestamp: new Date().toISOString(),
      companionMode: "LOCAL_STANDBY",
    };
  }
}

export const riskService: IRiskService = new OfflineRiskService();
