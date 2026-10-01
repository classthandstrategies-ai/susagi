import React from "react";
import { RiskAssessment } from "@/types/risk";
import { RiskBadge } from "./RiskBadge";
import { formatProtectiveAction, getRiskColorClass, cn } from "@/lib/utils";

interface RiskSummaryProps {
  assessment: RiskAssessment;
  showHindi?: boolean;
  className?: string;
}

export const RiskSummary: React.FC<RiskSummaryProps> = ({
  assessment,
  showHindi = false,
  className,
}) => {
  const { riskLevel, headline, explanation, recommendedAction, actionRationale } =
    assessment;
  const colors = getRiskColorClass(riskLevel);

  return (
    <section
      aria-labelledby="risk-summary-headline"
      className={cn(
        "rounded-2xl border p-6 transition-colors",
        colors.bgSoft,
        colors.border,
        className
      )}
    >
      {/* Risk Badge and Timestamp */}
      <div className="flex items-center justify-between gap-4 mb-4">
        <RiskBadge level={riskLevel} size="lg" showHindi={showHindi} />
        <span className="text-xs text-secondary font-mono">
          {assessment.timestamp}
        </span>
      </div>

      {/* 1. WHAT IS HAPPENING */}
      <div className="space-y-1 mb-4">
        <h2
          id="risk-summary-headline"
          className="text-xl sm:text-2xl font-semibold text-primary tracking-tight"
        >
          {headline}
        </h2>
        {showHindi && assessment.hindiHeadline && (
          <p className="text-sm text-secondary italic">
            {assessment.hindiHeadline}
          </p>
        )}
      </div>

      {/* 2. WHY IT IS RISKY */}
      <div className="bg-surface rounded-xl p-4 border border-subtle mb-4">
        <div className="text-xs font-semibold uppercase tracking-wider text-muted mb-1">
          Why This Is Risky
        </div>
        <p className="text-sm sm:text-base text-secondary leading-relaxed">
          {explanation}
        </p>
        {showHindi && assessment.hindiExplanation && (
          <p className="text-xs text-muted mt-1 italic">
            {assessment.hindiExplanation}
          </p>
        )}
      </div>

      {/* 3. WHAT THE USER SHOULD DO NEXT */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-2 border-t border-subtle">
        <div>
          <div className="text-xs font-semibold uppercase tracking-wider text-muted mb-0.5">
            Recommended Action
          </div>
          <div className="text-base font-semibold text-primary">
            {formatProtectiveAction(recommendedAction)}
          </div>
          {actionRationale && (
            <p className="text-xs text-secondary mt-0.5">
              {actionRationale}
            </p>
          )}
        </div>
      </div>
    </section>
  );
};
