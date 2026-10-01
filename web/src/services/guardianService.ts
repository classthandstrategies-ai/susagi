import { GuardianCircle } from "@/types/guardian";
import { guardianCircleFixture } from "@/fixtures/guardianFixtures";

export type GuardianServiceStatus =
  | "LOADED"
  | "EMPTY"
  | "LOADING"
  | "ERROR"
  | "UNAVAILABLE";

export interface GuardianFetchResult {
  status: GuardianServiceStatus;
  circle: GuardianCircle;
  errorMessage?: string;
  isFixtureMode: boolean;
}

export interface IGuardianService {
  getGuardianCircle(): Promise<GuardianCircle>;
}

export class OfflineGuardianService implements IGuardianService {
  async getGuardianCircle(): Promise<GuardianCircle> {
    // Normal production companion runtime: no connected guardian backend
    return {
      guardians: [],
      activeAlertCount: 0,
    };
  }
}

export const guardianService: IGuardianService = new OfflineGuardianService();

/**
 * Development-only fixture provider for previewing Guardian Circle states.
 * Only active when process.env.NODE_ENV === "development".
 */
export function getDevFixtureGuardianCircle(
  fixtureKey?: string
): GuardianFetchResult {
  if (process.env.NODE_ENV !== "development" || !fixtureKey) {
    return {
      status: "UNAVAILABLE",
      circle: {
        guardians: [],
        activeAlertCount: 0,
      },
      isFixtureMode: false,
    };
  }

  const key = fixtureKey.toLowerCase();

  switch (key) {
    case "loading":
      return {
        status: "LOADING",
        circle: {
          guardians: [],
          activeAlertCount: 0,
        },
        isFixtureMode: true,
      };

    case "error":
      return {
        status: "ERROR",
        circle: {
          guardians: [],
          activeAlertCount: 0,
        },
        errorMessage: "Guardian service is currently unavailable. Could not fetch trusted circle.",
        isFixtureMode: true,
      };

    case "empty":
      return {
        status: "EMPTY",
        circle: {
          guardians: [],
          activeAlertCount: 0,
        },
        isFixtureMode: true,
      };

    case "guardians":
    default:
      return {
        status: "LOADED",
        circle: guardianCircleFixture,
        isFixtureMode: true,
      };
  }
}
