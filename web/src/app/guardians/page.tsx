import React from "react";
import { guardianService, getDevFixtureGuardianCircle } from "@/services/guardianService";
import { GuardianCirclePageClient, GuardianCircleViewState } from "@/features/guardians";

interface GuardiansPageProps {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}

export default async function GuardiansPage({ searchParams }: GuardiansPageProps) {
  const resolvedParams = await searchParams;
  const fixtureKey =
    typeof resolvedParams.fixture === "string" ? resolvedParams.fixture : undefined;

  let state: GuardianCircleViewState;

  // Development fixture mode strictly guarded
  if (process.env.NODE_ENV === "development" && fixtureKey) {
    const fixtureResult = getDevFixtureGuardianCircle(fixtureKey);
    state = {
      status: fixtureResult.status,
      circle: fixtureResult.circle,
      errorMessage: fixtureResult.errorMessage,
      isFixtureMode: fixtureResult.isFixtureMode,
      fixtureKey,
    };
  } else {
    // Normal production companion runtime
    const circle = await guardianService.getGuardianCircle();
    state = {
      status: circle.guardians.length > 0 ? "LOADED" : "UNAVAILABLE",
      circle,
      isFixtureMode: false,
    };
  }

  return <GuardianCirclePageClient initialState={state} />;
}
