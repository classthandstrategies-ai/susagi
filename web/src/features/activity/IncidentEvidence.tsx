"use client";

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
      {/* 3. What SuSagi Noticed (Signals & Flags) */}
      {signals && signals.length > 0 && (
        <section aria-labelledby="signals-heading" className="space-y-3">
          <h2
            id="signals-heading"
            className="text-xs font-medium text-secondary"
          >
            What SuSagi noticed ({signals.length})
          </h2>

          <div className="space-y-3">
            {signals.map((sig) => (
              <SignalCard key={sig.id} signal={sig} />
            ))}
          </div>
        </section>
      )}

      {/* 4. Evidence Captured (What was said / excerpts) */}
      {evidence && evidence.length > 0 && (
        <section aria-labelledby="evidence-heading" className="space-y-3">
          <h2
            id="evidence-heading"
            className="text-xs font-medium text-secondary"
          >
            What was said during the interaction ({evidence.length})
          </h2>

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
