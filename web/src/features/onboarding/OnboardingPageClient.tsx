"use client";

import React, { useState, useSyncExternalStore } from "react";
import { useRouter } from "next/navigation";
import { GuardianContact } from "@/types/guardian";
import { OnboardingStep } from "./onboardingTypes";
import {
  saveOnboardingProgress,
  getResumeStep,
  subscribeOnboarding,
  getOnboardingProgressSnapshot,
  getServerOnboardingSnapshot,
} from "./onboardingStorage";
import { OnboardingShell } from "./OnboardingShell";
import { WelcomeStep } from "./WelcomeStep";
import { HowSusagiHelpsStep } from "./HowSusagiHelpsStep";
import { ConnectProtectionStep } from "./ConnectProtectionStep";
import { GuardianStep } from "./GuardianStep";
import { PracticeScenarioStep } from "./PracticeScenarioStep";
import { ReadyStep } from "./ReadyStep";
import { GuardianFormData } from "@/features/guardians/guardianTypes";

interface OnboardingPageClientProps {
  initialStepOverride?: OnboardingStep;
  initialPhoneConnected?: boolean;
  initialGuardians?: GuardianContact[];
  isFixtureMode?: boolean;
}

export const OnboardingPageClient: React.FC<OnboardingPageClientProps> = ({
  initialStepOverride,
  initialPhoneConnected = false,
  initialGuardians = [],
  isFixtureMode = false,
}) => {
  const router = useRouter();

  const progress = useSyncExternalStore(
    subscribeOnboarding,
    getOnboardingProgressSnapshot,
    getServerOnboardingSnapshot
  );

  const isClient = useSyncExternalStore(
    () => () => {},
    () => true,
    () => false
  );

  const [userStep, setUserStep] = useState<OnboardingStep | null>(null);
  const [addedGuardians, setAddedGuardians] = useState<GuardianContact[]>([]);

  const guardians = [...initialGuardians, ...addedGuardians];

  // Derive active step
  const activeStep: OnboardingStep =
    userStep ??
    initialStepOverride ??
    (isClient ? getResumeStep(progress) : (initialStepOverride || "welcome"));

  // Step numbering: Welcome has no number; the remaining 5 steps are 1 to 5
  const getStepNumber = (s: OnboardingStep): number | undefined => {
    switch (s) {
      case "how-it-helps":
        return 1;
      case "connect-protection":
        return 2;
      case "guardian":
        return 3;
      case "practice":
        return 4;
      case "ready":
        return 5;
      case "welcome":
      default:
        return undefined;
    }
  };

  // Back Navigation Handlers
  const handleBack = () => {
    switch (activeStep) {
      case "how-it-helps":
        setUserStep("welcome");
        break;
      case "connect-protection":
        setUserStep("how-it-helps");
        break;
      case "guardian":
        setUserStep("connect-protection");
        break;
      case "practice":
        setUserStep("guardian");
        break;
      case "ready":
        setUserStep("practice");
        break;
      default:
        break;
    }
  };

  // Step Transitions
  const handleWelcomeSetup = () => {
    saveOnboardingProgress({
      introductionSeen: true,
      lastStep: "how-it-helps",
    });
    setUserStep("how-it-helps");
  };

  const handleWelcomeExploreFirst = () => {
    // Sets introductionSeen = true, but does NOT mark onboarding complete
    saveOnboardingProgress({
      introductionSeen: true,
      lastStep: "welcome",
    });
    router.push("/");
  };

  const handleHowItHelpsContinue = () => {
    saveOnboardingProgress({
      howSusagiHelpsCompleted: true,
      lastStep: "connect-protection",
    });
    setUserStep("connect-protection");
  };

  const handleConnectProtectionContinue = () => {
    saveOnboardingProgress({
      lastStep: "guardian",
    });
    setUserStep("guardian");
  };

  const handleGuardianContinue = () => {
    saveOnboardingProgress({
      lastStep: "practice",
    });
    setUserStep("practice");
  };

  const handleGuardianSkip = () => {
    saveOnboardingProgress({
      guardianStepSkipped: true,
      lastStep: "practice",
    });
    setUserStep("practice");
  };

  const handleAddFixtureGuardian = (data: GuardianFormData) => {
    const newContact: GuardianContact = {
      id: `g-${Date.now()}`,
      name: data.name,
      relationship: data.relationship || "Trusted Contact",
      phone: data.phone,
      isPrimary: guardians.length === 0,
      canVerifyIdentity: true,
      status: "ACTIVE",
      avatarInitials: data.name
        .split(" ")
        .map((p) => p[0])
        .join("")
        .toUpperCase()
        .slice(0, 2),
    };
    setAddedGuardians((prev) => [...prev, newContact]);
  };

  const handlePracticeComplete = () => {
    saveOnboardingProgress({
      practiceCompleted: true,
      lastStep: "ready",
    });
    setUserStep("ready");
  };

  const handleReadyFinish = () => {
    saveOnboardingProgress({
      onboardingCompleted: true,
      lastStep: "ready",
    });
    router.push("/");
  };

  // Render neutral shell while hydrating to prevent flash
  if (!isClient) {
    return (
      <OnboardingShell showBack={false}>
        <div className="py-20 text-center text-xs text-muted">
          Loading safety walkthrough...
        </div>
      </OnboardingShell>
    );
  }

  const showBack = activeStep !== "welcome";
  const stepNumber = getStepNumber(activeStep);

  return (
    <OnboardingShell
      showBack={showBack}
      onBack={handleBack}
      stepNumber={stepNumber}
      totalSteps={5}
    >
      {activeStep === "welcome" && (
        <WelcomeStep
          onSetup={handleWelcomeSetup}
          onExploreFirst={handleWelcomeExploreFirst}
        />
      )}

      {activeStep === "how-it-helps" && (
        <HowSusagiHelpsStep onContinue={handleHowItHelpsContinue} />
      )}

      {activeStep === "connect-protection" && (
        <ConnectProtectionStep
          isDeviceConnected={initialPhoneConnected}
          onContinue={handleConnectProtectionContinue}
        />
      )}

      {activeStep === "guardian" && (
        <GuardianStep
          guardians={guardians}
          isServiceAvailable={isFixtureMode}
          isFixtureMode={isFixtureMode}
          onAddFixtureGuardian={handleAddFixtureGuardian}
          onContinue={handleGuardianContinue}
          onSkip={handleGuardianSkip}
        />
      )}

      {activeStep === "practice" && (
        <PracticeScenarioStep onComplete={handlePracticeComplete} />
      )}

      {activeStep === "ready" && (
        <ReadyStep
          hasRealGuardian={guardians.length > 0}
          guardianStepSkipped={progress.guardianStepSkipped}
          isPhoneConnected={initialPhoneConnected}
          practiceCompleted={progress.practiceCompleted}
          onFinish={handleReadyFinish}
        />
      )}
    </OnboardingShell>
  );
};
