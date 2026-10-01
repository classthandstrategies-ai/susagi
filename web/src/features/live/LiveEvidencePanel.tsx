"use client";

import React from "react";
import { ScamEvidence } from "@/types/risk";
import { EvidenceCard } from "@/components/cards/EvidenceCard";

interface LiveEvidencePanelProps {
  evidence?: ScamEvidence[];
}

export const LiveEvidencePanel: React.FC<LiveEvidencePanelProps> = ({
  evidence = [],
}) => {
  return (
    <section aria-labelledby="evidence-panel-heading" className="space-y-3">
      <h3
        id="evidence-panel-heading"
        className="text-xs font-medium text-secondary"
      >
        What was said during the call
      </h3>

      {evidence.length === 0 ? (
        <div className="rounded-2xl bg-surface border border-subtle p-6 text-center text-sm text-secondary shadow-sm">
          No sensitive excerpts captured during this call.
        </div>
      ) : (
        <div className="space-y-3">
          {evidence.map((item) => (
            <EvidenceCard key={item.id} evidence={item} />
          ))}
        </div>
      )}
    </section>
  );
};
