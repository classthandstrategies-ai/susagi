import React from "react";
import { HomePageClient } from "@/features/home/HomePageClient";
import { HomeViewState } from "@/features/home/homeTypes";
import { protectionService } from "@/services/protectionService";
import { sampleIncidentsList } from "@/fixtures/incidentFixtures";
import { guardianCircleFixture } from "@/fixtures/guardianFixtures";
import { IncidentItem } from "@/types/activity";
import { GuardianContact } from "@/types/guardian";

interface HomePageProps {
  searchParams: Promise<{ fixture?: string }>;
}

export default async function HomePage({ searchParams }: HomePageProps) {
  const resolvedParams = await searchParams;
  const isDev = process.env.NODE_ENV === "development";
  const fixtureKey = resolvedParams.fixture;
  const isFixtureMode = isDev && Boolean(fixtureKey && fixtureKey !== "none");

  // Fetch truthful baseline protection overview & capabilities
  const baseOverview = await protectionService.getOverview();
  const capabilities = await protectionService.getCapabilities();

  // If in development fixture mode, simulate richer preview states
  let overview = baseOverview;
  let recentIncidents: IncidentItem[] = [];
  let guardians: GuardianContact[] = [];

  if (isFixtureMode) {
    if (fixtureKey === "attention") {
      overview = {
        state: "ATTENTION_REQUIRED",
        headline: "Device Attention Required (Fixture)",
        statusDescription:
          "Simulated fixture state: Device synchronization paused. Please reconnect your Android phone.",
        deviceSyncStatus: "Sync paused (simulated)",
        isDeviceConnected: false,
        activeShieldsCount: 2,
        totalShieldsCount: 4,
      };
      recentIncidents = sampleIncidentsList;
      guardians = guardianCircleFixture.guardians;
    } else if (fixtureKey === "normal" || fixtureKey === "low") {
      overview = {
        state: "READY",
        headline: "Protected & Synchronized (Fixture)",
        statusDescription:
          "Simulated fixture state: All device defense services connected and operating normally.",
        deviceSyncStatus: "Paired with Pixel 8 (simulated)",
        isDeviceConnected: true,
        activeShieldsCount: 4,
        totalShieldsCount: 4,
      };
      recentIncidents = sampleIncidentsList.slice(0, 1);
      guardians = guardianCircleFixture.guardians;
    } else {
      // General fixture fallback
      recentIncidents = sampleIncidentsList;
      guardians = guardianCircleFixture.guardians;
    }
  }

  const initialState: HomeViewState = {
    overview,
    capabilities,
    recentIncidents,
    guardians,
    isFixtureMode,
    fixtureScenarioName: isFixtureMode ? fixtureKey : undefined,
  };

  return <HomePageClient initialState={initialState} />;
}
