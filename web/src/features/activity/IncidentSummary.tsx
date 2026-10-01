import React from "react";
import { IncidentDetail } from "@/types/activity";
import { RiskBadge } from "@/components/risk/RiskBadge";

interface IncidentSummaryProps {
  incident: IncidentDetail;
}

export const IncidentSummary: React.FC<IncidentSummaryProps> = ({ incident }) => {
  return (
    <article
      aria-labelledby="incident-summary-heading"
      className="rounded-2xl bg-surface border border-subtle p-6 space-y-4"
    >
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <RiskBadge level={incident.riskLevel} size="md" />
          <span className="text-xs px-2.5 py-0.5 rounded-full font-mono font-semibold bg-surfaceElevated border border-subtle text-secondary">
            {incident.status}
          </span>
        </div>
        <span className="text-xs text-muted font-mono">{incident.timestamp}</span>
      </div>

      <h1
        id="incident-summary-heading"
        className="text-2xl sm:text-3xl font-bold text-primary tracking-tight"
      >
        {incident.title}
      </h1>

      {/* Claimed Identity and Source Metadata */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
        <div className="rounded-xl bg-surfaceElevated p-3 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-0.5">
            Claimed Identity
          </div>
          <div className="text-sm font-semibold text-primary">
            {incident.claimedIdentity || "Unspecified / Anonymous"}
          </div>
        </div>

        <div className="rounded-xl bg-surfaceElevated p-3 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-0.5">
            Source Channel & Number
          </div>
          <div className="text-sm font-mono text-secondary truncate">
            {incident.source}
          </div>
        </div>
      </div>

      {/* Summary Narrative */}
      <div className="space-y-1.5 pt-2 border-t border-subtle">
        <h2 className="text-xs font-semibold uppercase tracking-wider text-muted">
          Event Summary
        </h2>
        <p className="text-sm text-secondary leading-relaxed">
          {incident.summary}
        </p>
      </div>

      {/* Outcome if present */}
      {incident.outcome && (
        <div className="space-y-1.5 pt-2 border-t border-subtle">
          <h2 className="text-xs font-semibold uppercase tracking-wider text-muted">
            Protection Outcome
          </h2>
          <p className="text-sm text-primary font-medium leading-relaxed">
            {incident.outcome}
          </p>
        </div>
      )}
    </article>
  );
};
