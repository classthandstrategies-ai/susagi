"use client";

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
      className="rounded-2xl bg-surface border border-subtle p-6 sm:p-7 space-y-5 shadow-sm"
    >
      {/* 1. Incident Title + Risk Pill + Date/time */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <RiskBadge level={incident.riskLevel} size="md" />
          <span className="text-xs px-2.5 py-0.5 rounded-full font-medium bg-surfaceElevated border border-subtle text-secondary">
            {incident.status === "ACTIVE"
              ? "Active incident"
              : incident.status.replace(/_/g, " ")}
          </span>
        </div>
        <span className="text-xs text-muted font-mono">{incident.timestamp}</span>
      </div>

      <div className="space-y-2">
        <h1
          id="incident-summary-heading"
          className="text-2xl sm:text-3xl font-semibold text-primary tracking-tight"
        >
          {incident.title}
        </h1>

        {incident.claimedIdentity && (
          <div className="flex items-center gap-2 text-xs text-secondary">
            <span className="text-muted">Claimed identity:</span>
            <span className="font-semibold text-primary">{incident.claimedIdentity}</span>
          </div>
        )}
      </div>

      {/* 2. What Happened (Plain language narrative) */}
      <div className="space-y-1.5 pt-3 border-t border-subtle">
        <h2 className="text-xs font-medium text-secondary">
          What happened
        </h2>
        <p className="text-sm sm:text-base text-primary leading-relaxed bg-surfaceElevated rounded-xl p-4 border border-subtle">
          {incident.summary}
        </p>
      </div>

      {/* Outcome if present */}
      {incident.outcome && (
        <div className="space-y-1.5 pt-1">
          <h2 className="text-xs font-medium text-secondary">
            Protection result
          </h2>
          <p className="text-sm text-secondary leading-relaxed bg-surfaceElevated rounded-xl p-4 border border-subtle">
            {incident.outcome}
          </p>
        </div>
      )}
    </article>
  );
};
