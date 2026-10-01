"use client";

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
      <h3
        id="signals-panel-heading"
        className="text-xs font-medium text-secondary"
      >
        What SuSagi noticed ({signals.length})
      </h3>

      {signals.length === 0 ? (
        <div className="rounded-2xl bg-surface border border-subtle p-6 text-center text-sm text-secondary shadow-sm">
          No suspicious signals detected during this call.
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
