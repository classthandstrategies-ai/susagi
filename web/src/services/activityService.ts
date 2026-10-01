import { IncidentItem, IncidentDetail } from "@/types/activity";
import {
  allFixtureIncidents,
  sampleIncidentsList,
  bankImpersonationIncident,
  familyImpersonationIncident,
} from "@/fixtures/incidentFixtures";

export type ActivityServiceStatus =
  | "LOADED"
  | "EMPTY"
  | "LOADING"
  | "ERROR"
  | "UNAVAILABLE";

export interface ActivityFetchResult {
  status: ActivityServiceStatus;
  incidents: IncidentItem[];
  errorMessage?: string;
  isFixtureMode: boolean;
}

export interface IActivityService {
  getIncidents(): Promise<IncidentItem[]>;
  getIncidentById(id: string): Promise<IncidentDetail | null>;
}

/**
 * Normal production runtime service.
 * Returns honest disconnected/empty state without calling fake APIs.
 */
export class OfflineActivityService implements IActivityService {
  async getIncidents(): Promise<IncidentItem[]> {
    return [];
  }

  async getIncidentById(_id: string): Promise<IncidentDetail | null> {
    void _id;
    return null;
  }
}

export const activityService: IActivityService = new OfflineActivityService();

/**
 * Development-only fixture provider for previewing UI states.
 * Only invoked when process.env.NODE_ENV === "development".
 */
export function getDevFixtureActivity(fixtureKey?: string): ActivityFetchResult {
  if (process.env.NODE_ENV !== "development" || !fixtureKey) {
    return {
      status: "UNAVAILABLE",
      incidents: [],
      isFixtureMode: false,
    };
  }

  const key = fixtureKey.toLowerCase();

  switch (key) {
    case "loading":
      return {
        status: "LOADING",
        incidents: [],
        isFixtureMode: true,
      };

    case "error":
      return {
        status: "ERROR",
        incidents: [],
        errorMessage: "The activity service is unavailable. Could not fetch security event ledger.",
        isFixtureMode: true,
      };

    case "empty":
      return {
        status: "EMPTY",
        incidents: [],
        isFixtureMode: true,
      };

    case "bank":
      return {
        status: "LOADED",
        incidents: sampleIncidentsList.filter(
          (inc) => inc.id === bankImpersonationIncident.id
        ),
        isFixtureMode: true,
      };

    case "family":
      return {
        status: "LOADED",
        incidents: sampleIncidentsList.filter(
          (inc) => inc.id === familyImpersonationIncident.id
        ),
        isFixtureMode: true,
      };

    case "mixed":
    default:
      return {
        status: "LOADED",
        incidents: sampleIncidentsList,
        isFixtureMode: true,
      };
  }
}

/**
 * Development-only fixture detail getter.
 */
export function getDevFixtureIncidentById(
  id: string,
  fixtureKey?: string
): IncidentDetail | null {
  if (process.env.NODE_ENV !== "development") {
    return null;
  }

  // If explicit fixtureKey is passed, check that first
  if (fixtureKey) {
    const key = fixtureKey.toLowerCase();
    if (key === "bank") return bankImpersonationIncident;
    if (key === "family") return familyImpersonationIncident;
  }

  // Otherwise check by ID in the fixture dictionary
  return allFixtureIncidents[id] || null;
}
