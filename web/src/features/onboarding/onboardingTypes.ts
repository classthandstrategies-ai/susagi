export type OnboardingStep =
  | "welcome"
  | "how-it-helps"
  | "connect-protection"
  | "guardian"
  | "practice"
  | "ready";

export interface OnboardingProgress {
  introductionSeen: boolean;
  howSusagiHelpsCompleted: boolean;
  guardianStepSkipped: boolean;
  practiceCompleted: boolean;
  onboardingCompleted: boolean;
  lastStep: OnboardingStep;
}

export const DEFAULT_ONBOARDING_PROGRESS: OnboardingProgress = {
  introductionSeen: false,
  howSusagiHelpsCompleted: false,
  guardianStepSkipped: false,
  practiceCompleted: false,
  onboardingCompleted: false,
  lastStep: "welcome",
};
