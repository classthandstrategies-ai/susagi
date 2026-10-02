import React from "react";
import type { Metadata } from "next";
import { OnboardingPageClient, OnboardingStep } from "@/features/onboarding";
import { protectionService } from "@/services/protectionService";
import {
  guardianService,
  getDevFixtureGuardianCircle,
} from "@/services/guardianService";

export const metadata: Metadata = {
  title: "Get Started — SuSagi Safety Walkthrough",
  description:
    "Learn how SuSagi helps you review suspicious requests, coordinate identity checks, and inspect unfamiliar links.",
};

interface OnboardingPageProps {
  searchParams: Promise<{ fixture?: string; step?: string }>;
}

export default async function OnboardingPage({
  searchParams,
}: OnboardingPageProps) {
  const resolvedParams = await searchParams;
  const isDev = process.env.NODE_ENV === "development";
  const fixtureKey = resolvedParams.fixture;
  const isFixtureMode = isDev && Boolean(fixtureKey && fixtureKey !== "none");
  const stepParam = isValidStep(resolvedParams.step)
    ? resolvedParams.step
    : undefined;

  // Retrieve truthful baseline state
  const overview = await protectionService.getOverview();
  let initialGuardians = (await guardianService.getGuardianCircle()).guardians;

  if (isFixtureMode) {
    const fixtureResult = getDevFixtureGuardianCircle(fixtureKey);
    initialGuardians = fixtureResult.circle.guardians;
  }

  return (
    <OnboardingPageClient
      initialStepOverride={stepParam}
      initialPhoneConnected={overview.isDeviceConnected}
      initialGuardians={initialGuardians}
      isFixtureMode={isFixtureMode}
    />
  );
}

function isValidStep(step?: string): step is OnboardingStep {
  return (
    typeof step === "string" &&
    [
      "welcome",
      "how-it-helps",
      "connect-protection",
      "guardian",
      "practice",
      "ready",
    ].includes(step)
  );
}
