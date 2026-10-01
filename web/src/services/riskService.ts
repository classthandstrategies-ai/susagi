import { RiskAssessment } from "@/types/risk";
import { WebProtectionOverview } from "@/types/protection";

/**
 * Interface contract for receiving risk assessments from authoritative engines.
 * CRITICAL RULE: Frontend never calculates risk or evaluates numeric scores.
 */
export interface IRiskService {
  getCurrentAssessment(): Promise<RiskAssessment | null>;
  getProtectionSummary(): Promise<WebProtectionOverview>;
}

export class OfflineRiskService implements IRiskService {
  async getCurrentAssessment(): Promise<RiskAssessment | null> {
    // In production without live call or active device paired, live assessment is idle
    return null;
  }

  async getProtectionSummary(): Promise<WebProtectionOverview> {
    return {
      state: "READY",
      headline: "Web Companion Ready",
      statusDescription:
        "This companion is ready. Device protection status will appear here when connected to SuSagi services.",
      deviceSyncStatus: "Device service not connected",
      isDeviceConnected: false,
      activeShieldsCount: 0,
      totalShieldsCount: 4,
    };
  }
}

export const riskService: IRiskService = new OfflineRiskService();
