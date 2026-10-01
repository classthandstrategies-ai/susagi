import React from "react";
import { activityService, getDevFixtureIncidentById } from "@/services/activityService";
import { IncidentDetailPageClient } from "@/features/activity";
import { IncidentDetail } from "@/types/activity";

interface IncidentDetailPageProps {
  params: Promise<{ incidentId: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}

export default async function IncidentDetailPage({
  params,
  searchParams,
}: IncidentDetailPageProps) {
  const { incidentId } = await params;
  const resolvedParams = await searchParams;
  const fixtureKey =
    typeof resolvedParams.fixture === "string" ? resolvedParams.fixture : undefined;

  let incident: IncidentDetail | null = null;
  let isFixtureMode = false;

  // Development fixture mode strictly guarded
  if (process.env.NODE_ENV === "development" && (fixtureKey || incidentId.startsWith("inc-"))) {
    incident = getDevFixtureIncidentById(incidentId, fixtureKey);
    isFixtureMode = Boolean(incident);
  }

  // Normal runtime check if not resolved via dev fixture
  if (!incident) {
    incident = await activityService.getIncidentById(incidentId);
  }

  return (
    <IncidentDetailPageClient
      incident={incident}
      incidentId={incidentId}
      isFixtureMode={isFixtureMode}
      fixtureKey={fixtureKey}
    />
  );
}
