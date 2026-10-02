import {
  OnboardingProgress,
  OnboardingStep,
  DEFAULT_ONBOARDING_PROGRESS,
} from "./onboardingTypes";

const STORAGE_KEY = "susagi_onboarding_progress";

let cachedRaw: string | null = null;
let cachedProgress: OnboardingProgress = { ...DEFAULT_ONBOARDING_PROGRESS };

/**
 * Safely retrieves onboarding progress from browser localStorage.
 * Returns safe defaults if SSR, missing, or malformed.
 */
export function getOnboardingProgress(): OnboardingProgress {
  if (typeof window === "undefined") {
    return { ...DEFAULT_ONBOARDING_PROGRESS };
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { ...DEFAULT_ONBOARDING_PROGRESS };
    }

    const parsed = JSON.parse(raw);
    if (!parsed || typeof parsed !== "object") {
      return { ...DEFAULT_ONBOARDING_PROGRESS };
    }

    return {
      introductionSeen: Boolean(parsed.introductionSeen),
      howSusagiHelpsCompleted: Boolean(parsed.howSusagiHelpsCompleted),
      guardianStepSkipped: Boolean(parsed.guardianStepSkipped),
      practiceCompleted: Boolean(parsed.practiceCompleted),
      onboardingCompleted: Boolean(parsed.onboardingCompleted),
      lastStep: isValidStep(parsed.lastStep)
        ? parsed.lastStep
        : DEFAULT_ONBOARDING_PROGRESS.lastStep,
    };
  } catch {
    return { ...DEFAULT_ONBOARDING_PROGRESS };
  }
}

/**
 * Snapshot for useSyncExternalStore with memoization to maintain stable references.
 */
export function getOnboardingProgressSnapshot(): OnboardingProgress {
  if (typeof window === "undefined") {
    return DEFAULT_ONBOARDING_PROGRESS;
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw === cachedRaw) {
      return cachedProgress;
    }

    cachedRaw = raw;
    if (!raw) {
      cachedProgress = { ...DEFAULT_ONBOARDING_PROGRESS };
      return cachedProgress;
    }

    const parsed = JSON.parse(raw);
    if (!parsed || typeof parsed !== "object") {
      cachedProgress = { ...DEFAULT_ONBOARDING_PROGRESS };
      return cachedProgress;
    }

    cachedProgress = {
      introductionSeen: Boolean(parsed.introductionSeen),
      howSusagiHelpsCompleted: Boolean(parsed.howSusagiHelpsCompleted),
      guardianStepSkipped: Boolean(parsed.guardianStepSkipped),
      practiceCompleted: Boolean(parsed.practiceCompleted),
      onboardingCompleted: Boolean(parsed.onboardingCompleted),
      lastStep: isValidStep(parsed.lastStep)
        ? parsed.lastStep
        : DEFAULT_ONBOARDING_PROGRESS.lastStep,
    };
    return cachedProgress;
  } catch {
    cachedProgress = { ...DEFAULT_ONBOARDING_PROGRESS };
    return cachedProgress;
  }
}

export function getServerOnboardingSnapshot(): OnboardingProgress {
  return DEFAULT_ONBOARDING_PROGRESS;
}

export function subscribeOnboarding(callback: () => void): () => void {
  if (typeof window === "undefined") return () => {};
  window.addEventListener("storage", callback);
  return () => window.removeEventListener("storage", callback);
}

/**
 * Safely persists partial onboarding progress updates to localStorage.
 */
export function saveOnboardingProgress(
  update: Partial<OnboardingProgress>
): OnboardingProgress {
  if (typeof window === "undefined") {
    return { ...DEFAULT_ONBOARDING_PROGRESS, ...update };
  }

  try {
    const current = getOnboardingProgress();
    const next: OnboardingProgress = {
      ...current,
      ...update,
    };

    const serialized = JSON.stringify(next);
    localStorage.setItem(STORAGE_KEY, serialized);
    cachedRaw = serialized;
    cachedProgress = next;

    window.dispatchEvent(new Event("storage"));
    return next;
  } catch {
    return { ...DEFAULT_ONBOARDING_PROGRESS, ...update };
  }
}

/**
 * Clears stored onboarding progress for testing or reset.
 */
export function resetOnboardingProgress(): void {
  if (typeof window === "undefined") return;
  try {
    localStorage.removeItem(STORAGE_KEY);
    cachedRaw = null;
    cachedProgress = { ...DEFAULT_ONBOARDING_PROGRESS };
    window.dispatchEvent(new Event("storage"));
  } catch {
    // ignore
  }
}

/**
 * Computes the first unfinished meaningful step to resume when opening /onboarding.
 */
export function getResumeStep(progress: OnboardingProgress): OnboardingStep {
  if (!progress.introductionSeen) {
    return "welcome";
  }

  if (!progress.howSusagiHelpsCompleted) {
    return "how-it-helps";
  }

  if (progress.onboardingCompleted) {
    return "ready";
  }

  if (progress.practiceCompleted) {
    return "ready";
  }

  if (progress.lastStep === "connect-protection") {
    return "connect-protection";
  }

  if (progress.lastStep === "guardian" && !progress.guardianStepSkipped) {
    return "guardian";
  }

  if (progress.lastStep === "how-it-helps") {
    return "connect-protection";
  }

  return "practice";
}

function isValidStep(step: unknown): step is OnboardingStep {
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
