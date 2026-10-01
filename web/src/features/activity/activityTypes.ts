import { RiskLevel } from "@/types/risk";
import { ActivityChannel, IncidentItem } from "@/types/activity";
import { ActivityServiceStatus } from "@/services/activityService";

export type RiskFilter = "ALL" | RiskLevel;
export type ChannelFilter = "ALL" | ActivityChannel;
export type SortOrder = "NEWEST" | "OLDEST";

export interface ActivityFilterState {
  riskFilter: RiskFilter;
  channelFilter: ChannelFilter;
  searchQuery: string;
  sortBy: SortOrder;
}

export interface ActivityViewState {
  status: ActivityServiceStatus;
  incidents: IncidentItem[];
  errorMessage?: string;
  isFixtureMode: boolean;
  fixtureKey?: string;
}
