import React from "react";
import { LiveDefensePageClient } from "@/features/live/LiveDefensePageClient";
import { LiveDefenseViewState } from "@/features/live/liveTypes";
import { riskService } from "@/services/riskService";
import { riskFixtures } from "@/fixtures/riskFixtures";
import { LiveConnectionState } from "@/types/risk";

interface LiveDefensePageProps {
  searchParams: Promise<{ fixture?: string }>;
}

export default async function LiveDefensePage({
  searchParams,
}: LiveDefensePageProps) {
  const resolvedParams = await searchParams;
  const isDev = process.env.NODE_ENV === "development";
  const fixtureKey = resolvedParams.fixture;

  // Determine if valid development fixture was requested
  const isFixtureMode =
    isDev && Boolean(fixtureKey && fixtureKey in riskFixtures);

  let assessment = null;
  let connectionState: LiveConnectionState = "STANDBY";

  if (isFixtureMode && fixtureKey && fixtureKey in riskFixtures) {
    assessment = riskFixtures[fixtureKey];
    connectionState = "ASSESSMENT_AVAILABLE";
  } else {
    // Truthful runtime service boundary
    assessment = await riskService.getCurrentAssessment();
    connectionState = assessment ? "ASSESSMENT_AVAILABLE" : "STANDBY";
  }

  const initialState: LiveDefenseViewState = {
    connectionState,
    assessment,
    isFixtureMode,
    fixtureKey: isFixtureMode ? fixtureKey : undefined,
  };

  return <LiveDefensePageClient initialState={initialState} />;
}
