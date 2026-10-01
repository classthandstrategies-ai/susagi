import React from "react";
import { RiskAssessment } from "@/types/risk";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { getRiskColorClass, formatProtectiveAction, cn } from "@/lib/utils";

interface LiveRiskHeroProps {
  assessment: RiskAssessment;
}

export const LiveRiskHero: React.FC<LiveRiskHeroProps> = ({ assessment }) => {
  const {
    riskLevel,
    headline,
    explanation,
    recommendedAction,
    actionRationale,
    claimedIdentity,
  } = assessment;

  const colors = getRiskColorClass(riskLevel);

  return (
    <section
      aria-labelledby="live-hero-headline"
      className={cn(
        "rounded-2xl border p-5 sm:p-7 transition-colors",
        colors.bgSoft,
        colors.border
      )}
    >
      {/* Top Meta Bar: Risk Badge, Claimed Identity, Timestamp */}
      <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div className="flex flex-wrap items-center gap-2.5">
          <RiskBadge level={riskLevel} size="lg" />
          {claimedIdentity && (
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-surface border border-subtle text-xs text-secondary font-medium">
              <span className="text-muted">Claimed identity:</span>
              <span className="text-primary font-semibold">{claimedIdentity}</span>
            </div>
          )}
        </div>

        <span className="text-xs font-mono text-muted">
          {assessment.timestamp}
        </span>
      </div>

      {/* 1. WHAT IS HAPPENING (Headline) */}
      <div className="space-y-1 mb-4">
        <h2
          id="live-hero-headline"
          className="text-xl sm:text-2xl lg:text-3xl font-bold text-primary tracking-tight"
        >
          {headline}
        </h2>
        {assessment.hindiHeadline && (
          <p className="text-sm text-secondary italic">
            {assessment.hindiHeadline}
          </p>
        )}
      </div>

      {/* 2. WHY IT IS RISKY (Explanation) */}
      <div className="rounded-xl bg-surface border border-subtle p-4 sm:p-5 mb-5">
        <div className="text-[11px] font-semibold uppercase tracking-wider text-muted mb-1">
          Why It Matters / Risk Context
        </div>
        <p className="text-sm sm:text-base text-secondary leading-relaxed">
          {explanation}
        </p>
        {assessment.hindiExplanation && (
          <p className="text-xs text-muted mt-1.5 italic">
            {assessment.hindiExplanation}
          </p>
        )}
      </div>

      {/* 3. WHAT THE USER SHOULD DO NEXT (Recommended Action Directive) */}
      <div className="rounded-xl bg-surfaceElevated border border-default p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="space-y-0.5">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted">
            Primary Recommended Directive
          </div>
          <div className="text-base sm:text-lg font-bold text-primary">
            {formatProtectiveAction(recommendedAction)}
          </div>
          {actionRationale && (
            <p className="text-xs text-secondary leading-relaxed">
              {actionRationale}
            </p>
          )}
        </div>
      </div>
    </section>
  );
};
