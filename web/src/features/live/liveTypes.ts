import { RiskAssessment, LiveConnectionState } from "@/types/risk";

export interface LiveDefenseViewState {
  connectionState: LiveConnectionState;
  assessment: RiskAssessment | null;
  isFixtureMode: boolean;
  fixtureKey?: string;
}
