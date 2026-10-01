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
      <div className="flex items-center justify-between">
        <h3
          id="evidence-panel-heading"
          className="text-xs font-semibold uppercase tracking-wider text-muted"
        >
          Captured Call Context & Evidence
        </h3>
        <span className="text-[11px] font-mono text-muted">Acoustic Snippets</span>
      </div>

      {evidence.length === 0 ? (
        <div className="rounded-xl bg-surface border border-subtle p-6 text-center text-sm text-secondary">
          No sensitive requests or high-risk excerpts captured in buffer.
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
