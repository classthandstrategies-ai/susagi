import React from "react";
import { OnboardingProgress } from "./OnboardingProgress";

interface OnboardingShellProps {
  children: React.ReactNode;
  showBack?: boolean;
  onBack?: () => void;
  stepNumber?: number;
  totalSteps?: number;
}

export const OnboardingShell: React.FC<OnboardingShellProps> = ({
  children,
  showBack = false,
  onBack,
  stepNumber,
  totalSteps = 5,
}) => {
  return (
    <div className="min-h-screen bg-base text-primary flex flex-col justify-between antialiased">
      {/* Quiet Onboarding Header */}
      <header className="w-full max-w-[640px] mx-auto px-5 sm:px-8 pt-6 sm:pt-8 pb-4 flex items-center justify-between gap-4">
        {/* Left: Back button or SuSagi Identity */}
        <div className="flex items-center min-h-[48px]">
          {showBack && onBack ? (
            <button
              type="button"
              onClick={onBack}
              className="inline-flex items-center gap-1.5 min-h-[48px] px-3 -ml-3 rounded-xl text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer select-none"
              aria-label="Go back to previous step"
            >
              <svg
                className="w-4 h-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M15.75 19.5L8.25 12l7.5-7.5"
                />
              </svg>
              <span>Back</span>
            </button>
          ) : (
            <div className="flex items-center gap-2 select-none">
              <div className="w-6 h-6 rounded-md bg-brand text-white flex items-center justify-center text-xs font-bold shadow-xs">
                S
              </div>
              <span className="text-sm font-semibold tracking-tight text-primary">
                SuSagi
              </span>
            </div>
          )}
        </div>

        {/* Right: Quiet Progress indicator or brand mark */}
        <div className="flex items-center min-h-[48px]">
          {typeof stepNumber === "number" ? (
            <OnboardingProgress
              currentStepNumber={stepNumber}
              totalSteps={totalSteps}
            />
          ) : (
            <span className="text-xs font-medium text-muted">
              Safety companion
            </span>
          )}
        </div>
      </header>

      {/* Main Single Reading Column */}
      <main
        className="flex-1 w-full max-w-[640px] mx-auto px-5 sm:px-8 py-6 sm:py-10 flex flex-col justify-center"
      >
        {children}
      </main>

      {/* Quiet Footer Padding / Neutral boundary */}
      <footer className="w-full max-w-[640px] mx-auto px-5 sm:px-8 py-4 sm:py-6 text-center text-xs text-muted">
        {/* Dedicated quiet space, no normal footer navigation */}
      </footer>
    </div>
  );
};
