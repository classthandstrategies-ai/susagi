import React from "react";
import { getDevFixtureVerificationSession } from "@/services/verificationService";
import {
  VerificationResponderPageClient,
  VerificationResponderViewState,
} from "@/features/verification/responder";

interface VerificationResponderPageProps {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}

export default async function VerificationResponderPage({
  searchParams,
}: VerificationResponderPageProps) {
  const resolvedParams = await searchParams;
  const fixtureKey =
    typeof resolvedParams.fixture === "string" ? resolvedParams.fixture : undefined;

  let state: VerificationResponderViewState;

  // Development fixture mode is strictly guarded
  if (process.env.NODE_ENV === "development" && fixtureKey === "request") {
    const fixtureResult = getDevFixtureVerificationSession("request");
    state = {
      hasRequest: Boolean(fixtureResult.session),
      session: fixtureResult.session,
      isFixtureMode: true,
      fixtureKey,
    };
  } else {
    // Normal production companion runtime: no active incoming request
    state = {
      hasRequest: false,
      session: null,
      isFixtureMode: false,
    };
  }

  return <VerificationResponderPageClient initialState={state} />;
}
