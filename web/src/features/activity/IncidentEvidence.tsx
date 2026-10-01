import React from "react";
import { ScamSignal, ScamEvidence } from "@/types/risk";
import { SignalCard } from "@/components/cards/SignalCard";
import { EvidenceCard } from "@/components/cards/EvidenceCard";

interface IncidentEvidenceProps {
  signals: ScamSignal[];
  evidence: ScamEvidence[];
}

export const IncidentEvidence: React.FC<IncidentEvidenceProps> = ({
  signals,
  evidence,
}) => {
  return (
    <div className="space-y-6">
      {/* Linguistic Signals */}
      {signals && signals.length > 0 && (
        <section aria-labelledby="signals-heading" className="space-y-3">
          <div className="flex items-center justify-between">
            <h2
              id="signals-heading"
              className="text-xs font-semibold uppercase tracking-wider text-muted"
            >
              Detected Linguistic Scam Patterns ({signals.length})
            </h2>
            <span className="text-[11px] font-mono text-muted">
              Semantic Markers
            </span>
          </div>

          <div className="space-y-3">
            {signals.map((sig) => (
              <SignalCard key={sig.id} signal={sig} />
            ))}
          </div>
        </section>
      )}

      {/* Intercepted Communication Evidence */}
      {evidence && evidence.length > 0 && (
        <section aria-labelledby="evidence-heading" className="space-y-3">
          <div className="flex items-center justify-between">
            <h2
              id="evidence-heading"
              className="text-xs font-semibold uppercase tracking-wider text-muted"
            >
              Captured Context & Excerpts ({evidence.length})
            </h2>
            <span className="text-[11px] font-mono text-muted">
              Evidence Log
            </span>
          </div>

          <div className="space-y-3">
            {evidence.map((evi) => (
              <EvidenceCard key={evi.id} evidence={evi} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
};
