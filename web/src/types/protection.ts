import { ProtectionState } from "./risk";

export interface ProtectionCapability {
  id: string;
  name: string;
  description: string;
  status: "ACTIVE" | "PAUSED" | "CONFIGURED" | "UNAVAILABLE";
  iconName: "phone" | "shield" | "qr" | "link" | "users";
  supportedOnWeb: boolean;
}

export interface ProtectionStatusSummary {
  state: ProtectionState;
  activeShieldsCount: number;
  totalShieldsCount: number;
  lastInspectionTimestamp: string;
  companionMode: "LOCAL_STANDBY" | "PAIRED" | "UNAVAILABLE";
}
