"use client";

import React from "react";
import { RiskAssessment } from "@/types/risk";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { getRiskColorClass, cn } from "@/lib/utils";

interface LiveRiskHeroProps {
  assessment: RiskAssessment;
}

export const LiveRiskHero: React.FC<LiveRiskHeroProps> = ({ assessment }) => {
  const {
    riskLevel,
    headline,
    explanation,
    claimedIdentity,
  } = assessment;

  const colors = getRiskColorClass(riskLevel);

  return (
    <section
      aria-labelledby="live-hero-headline"
      className={cn(
        "rounded-2xl border p-6 sm:p-7 transition-colors shadow-sm",
        colors.bgSoft,
        colors.border
      )}
    >
      {/* 1. RISK STATE & IDENTITY */}
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

      {/* 2. HUMAN SENTENCE (Headline) */}
      <div className="space-y-1.5 mb-4">
        <h2
          id="live-hero-headline"
          className="text-xl sm:text-2xl font-semibold text-primary tracking-tight"
        >
          {headline}
        </h2>
        {assessment.hindiHeadline && (
          <p className="text-sm text-secondary italic">
            {assessment.hindiHeadline}
          </p>
        )}
      </div>

      {/* 3. SHORT EXPLANATION (Why it's risky) */}
      <div className="rounded-xl bg-surface border border-subtle p-4 sm:p-5">
        <p className="text-sm sm:text-base text-secondary leading-relaxed">
          {explanation}
        </p>
        {assessment.hindiExplanation && (
          <p className="text-xs text-muted mt-2 italic">
            {assessment.hindiExplanation}
          </p>
        )}
      </div>
    </section>
  );
};
