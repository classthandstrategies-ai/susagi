import { WebProtectionOverview, ProtectionCapability } from "@/types/protection";
import { IncidentItem } from "@/types/activity";
import { GuardianContact } from "@/types/guardian";

export interface HomeViewState {
  overview: WebProtectionOverview;
  capabilities: ProtectionCapability[];
  recentIncidents: IncidentItem[];
  guardians: GuardianContact[];
  isFixtureMode: boolean;
  fixtureScenarioName?: string;
}
