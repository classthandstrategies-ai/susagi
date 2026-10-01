import React from "react";
import { activityService, getDevFixtureActivity } from "@/services/activityService";
import { ActivityPageClient, ActivityViewState } from "@/features/activity";

interface ActivityPageProps {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}

export default async function ActivityPage({ searchParams }: ActivityPageProps) {
  const resolvedParams = await searchParams;
  const fixtureKey =
    typeof resolvedParams.fixture === "string" ? resolvedParams.fixture : undefined;

  let state: ActivityViewState;

  // Development fixture mode is strictly guarded
  if (process.env.NODE_ENV === "development" && fixtureKey) {
    const fixtureResult = getDevFixtureActivity(fixtureKey);
    state = {
      status: fixtureResult.status,
      incidents: fixtureResult.incidents,
      errorMessage: fixtureResult.errorMessage,
      isFixtureMode: fixtureResult.isFixtureMode,
      fixtureKey,
    };
  } else {
    // Normal production companion runtime
    const incidents = await activityService.getIncidents();
    state = {
      status: incidents.length > 0 ? "LOADED" : "UNAVAILABLE",
      incidents,
      isFixtureMode: false,
    };
  }

  return <ActivityPageClient initialState={state} />;
}
