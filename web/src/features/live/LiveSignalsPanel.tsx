import React from "react";
import { ScamSignal } from "@/types/risk";
import { SignalCard } from "@/components/cards/SignalCard";

interface LiveSignalsPanelProps {
  signals: ScamSignal[];
}

export const LiveSignalsPanel: React.FC<LiveSignalsPanelProps> = ({
  signals,
}) => {
  return (
    <section aria-labelledby="signals-panel-heading" className="space-y-3">
      <div className="flex items-center justify-between">
        <h3
          id="signals-panel-heading"
          className="text-xs font-semibold uppercase tracking-wider text-muted"
        >
          Why SuSagi Is Concerned ({signals.length})
        </h3>
        <span className="text-[11px] font-mono text-muted">
          Semantic Linguistic Patterns
        </span>
      </div>

      {signals.length === 0 ? (
        <div className="rounded-xl bg-surface border border-subtle p-6 text-center text-sm text-secondary">
          No suspicious signals found in the current assessment.
        </div>
      ) : (
        <div className="space-y-3">
          {signals.slice(0, 4).map((signal) => (
            <SignalCard key={signal.id} signal={signal} />
          ))}
        </div>
      )}
    </section>
  );
};
