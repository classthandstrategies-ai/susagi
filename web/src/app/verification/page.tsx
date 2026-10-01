import React from "react";
import { getDevFixtureVerificationSession } from "@/services/verificationService";
import {
  VerificationRequesterPageClient,
  VerificationRequesterViewState,
} from "@/features/verification/requester";

interface VerificationPageProps {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}

export default async function VerificationPage({
  searchParams,
}: VerificationPageProps) {
  const resolvedParams = await searchParams;
  const fixtureKey =
    typeof resolvedParams.fixture === "string" ? resolvedParams.fixture : undefined;

  let state: VerificationRequesterViewState;

  // Development fixture mode is strictly guarded
  if (process.env.NODE_ENV === "development" && fixtureKey) {
    const fixtureResult = getDevFixtureVerificationSession(fixtureKey);
    state = {
      status: fixtureResult.status,
      session: fixtureResult.session,
      outcomeNote: fixtureResult.outcomeNote,
      isFixtureMode: fixtureResult.isFixtureMode,
      fixtureKey,
    };
  } else {
    // Normal production companion runtime: always honestly reports UNAVAILABLE
    state = {
      status: "UNAVAILABLE",
      session: null,
      isFixtureMode: false,
    };
  }

  return <VerificationRequesterPageClient initialState={state} />;
}
