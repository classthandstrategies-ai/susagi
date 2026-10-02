import React from "react";

interface OnboardingProgressProps {
  currentStepNumber: number;
  totalSteps?: number;
}

export const OnboardingProgress: React.FC<OnboardingProgressProps> = ({
  currentStepNumber,
  totalSteps = 5,
}) => {
  return (
    <div
      aria-label={`Step ${currentStepNumber} of ${totalSteps}`}
      className="text-xs font-medium text-muted tracking-wide select-none"
    >
      <span>
        {currentStepNumber} of {totalSteps}
      </span>
    </div>
  );
};
