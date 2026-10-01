import React from "react";
import { RiskSummary } from "@/components/risk/RiskSummary";
import { SignalCard } from "@/components/cards/SignalCard";
import { EvidenceCard } from "@/components/cards/EvidenceCard";
import { PrimarySafetyAction } from "@/components/actions/PrimarySafetyAction";
import { SecondarySafetyAction } from "@/components/actions/SecondarySafetyAction";
import { riskFixtures, lowRiskScenario } from "@/fixtures/riskFixtures";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

interface LiveDefensePageProps {
  searchParams: Promise<{ fixture?: string }>;
}

export default async function LiveDefensePage({
  searchParams,
}: LiveDefensePageProps) {
  const resolvedParams = await searchParams;
  const fixtureKey = resolvedParams.fixture || "none";
  const assessment =
    fixtureKey in riskFixtures ? riskFixtures[fixtureKey] : lowRiskScenario;

  const isSimulated = fixtureKey !== "none";

  return (
    <div className="space-y-6 max-w-[1440px] mx-auto">
      <OfflineBanner
        message={
          isSimulated
            ? `PREVIEWING ${assessment._fixtureNotice} (${assessment.riskLevel})`
            : "Phase W1 Product Shell — Live Defense companion standby. Pair with phone or preview simulated scenario."
        }
      />

      {/* Header */}
      <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-primary">
            Live Call Defense
          </h1>
          <p className="text-sm text-secondary">
            Continuous acoustic & linguistic scam detection during active calls.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-full bg-risk-low animate-pulse" />
          <span className="text-xs font-mono text-secondary">
            {isSimulated ? "SIMULATED TELEMETRY" : "STANDBY"}
          </span>
        </div>
      </header>

      {/* Structured Risk Summary */}
      <RiskSummary assessment={assessment} showHindi={true} />

      {/* Primary Protective Safety Actions */}
      <div className="flex flex-wrap items-center gap-3 p-4 rounded-xl bg-surface border border-subtle">
        <PrimarySafetyAction
          label="End Call Immediately"
          variant={assessment.riskLevel === "CRITICAL" ? "danger" : "neutral"}
          disabled={!isSimulated}
        />
        <SecondarySafetyAction
          label="Trigger Guardian Check"
          disabled={!isSimulated}
        />
      </div>

      {/* Wide Evidence & Signals Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Detected Signals */}
        <section aria-labelledby="signals-heading" className="space-y-3">
          <div className="flex items-center justify-between">
            <h2 id="signals-heading" className="text-sm font-semibold uppercase tracking-wider text-muted">
              Scam Signals ({assessment.signals.length})
            </h2>
            <span className="text-xs text-muted font-mono">Bilingual STT</span>
          </div>

          {assessment.signals.length === 0 ? (
            <div className="p-8 text-center rounded-xl bg-surface border border-subtle text-muted text-sm">
              No suspicious linguistic patterns or threats detected.
            </div>
          ) : (
            <div className="space-y-3">
              {assessment.signals.map((sig) => (
                <SignalCard key={sig.id} signal={sig} />
              ))}
            </div>
          )}
        </section>

        {/* Right: Captured Evidence */}
        <section aria-labelledby="evidence-heading" className="space-y-3">
          <div className="flex items-center justify-between">
            <h2 id="evidence-heading" className="text-sm font-semibold uppercase tracking-wider text-muted">
              Acoustic & Call Evidence
            </h2>
            <span className="text-xs text-muted font-mono">Audio Buffer</span>
          </div>

          {!assessment.evidence || assessment.evidence.length === 0 ? (
            <div className="p-8 text-center rounded-xl bg-surface border border-subtle text-muted text-sm">
              Audio evidence buffer is clear.
            </div>
          ) : (
            <div className="space-y-3">
              {assessment.evidence.map((evi) => (
                <EvidenceCard key={evi.id} evidence={evi} />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
