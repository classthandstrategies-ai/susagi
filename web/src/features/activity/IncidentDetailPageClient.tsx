"use client";

import React from "react";
import Link from "next/link";
import { IncidentDetail } from "@/types/activity";
import { IncidentSummary } from "./IncidentSummary";
import { IncidentTimeline } from "./IncidentTimeline";
import { IncidentEvidence } from "./IncidentEvidence";
import { IncidentActions } from "./IncidentActions";

interface IncidentDetailPageClientProps {
  incident: IncidentDetail | null;
  incidentId: string;
  isFixtureMode?: boolean;
  fixtureKey?: string;
}

export const IncidentDetailPageClient: React.FC<IncidentDetailPageClientProps> = ({
  incident,
  incidentId,
  isFixtureMode = false,
  fixtureKey,
}) => {
  const backHref = fixtureKey ? `/activity?fixture=${fixtureKey}` : "/activity";

  // When incident is unavailable
  if (!incident) {
    return (
      <div className="space-y-6 max-w-xl mx-auto my-6">
        <Link
          href={backHref}
          className="min-h-[44px] inline-flex items-center gap-1.5 text-xs font-medium text-secondary hover:text-primary transition-colors"
        >
          <span aria-hidden="true">‹</span>
          <span>Back to activity</span>
        </Link>

        <section
          aria-labelledby="unavailable-heading"
          className="rounded-2xl bg-surface border border-subtle p-8 text-center space-y-4 shadow-sm"
        >
          <div className="w-12 h-12 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-secondary">
            <svg
              className="w-6 h-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={1.5}
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m5.231 13.481L15 18m-4.5-6H9m4.5 3H9m-4.5 3h1.5m6-12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>

          <div className="space-y-1.5">
            <h1 id="unavailable-heading" className="text-xl font-semibold text-primary">
              Incident unavailable
            </h1>
            <p className="text-sm text-secondary max-w-md mx-auto leading-relaxed">
              This incident could not be found or is not available from the connected activity service.
            </p>
            <p className="text-xs font-mono text-muted">
              Reference: {incidentId}
            </p>
          </div>

          <div className="pt-2">
            <Link
              href={backHref}
              className="min-h-[44px] inline-flex items-center justify-center px-4 py-2 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
            >
              Back to activity
            </Link>
          </div>
        </section>
      </div>
    );
  }

  // Loaded Incident View following Story Order:
  // 1. Incident title + Risk pill + Date/time (IncidentSummary)
  // 2. What happened (IncidentSummary)
  // 3. What SuSagi noticed (IncidentEvidence)
  // 4. Evidence captured (IncidentEvidence)
  // 5. Progression timeline (IncidentTimeline)
  // 6. What to do next (IncidentActions)
  // 7. Secondary technical details tucked quietly at the bottom
  return (
    <div className="space-y-8 max-w-3xl mx-auto">
      {/* Top Navigation & Fixture Badge */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          href={backHref}
          className="min-h-[44px] inline-flex items-center gap-1.5 text-xs font-medium text-secondary hover:text-primary transition-colors"
        >
          <span aria-hidden="true">‹</span>
          <span>Back to activity</span>
        </Link>

        {isFixtureMode && (
          <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded-full bg-surfaceElevated text-muted border border-subtle">
            Fixture preview
          </span>
        )}
      </div>

      {/* Story Order Content Flow */}
      <div className="space-y-6">
        {/* 1 & 2: Incident Title, Risk, Date/Time & What Happened Narrative */}
        <IncidentSummary incident={incident} />

        {/* 3 & 4: What SuSagi Noticed & Excerpts Captured */}
        <IncidentEvidence
          signals={incident.signals}
          evidence={incident.evidence}
        />

        {/* Progression Timeline (if multi-step) */}
        {incident.timeline && incident.timeline.length > 0 && (
          <IncidentTimeline
            timeline={incident.timeline}
            isFixtureMode={isFixtureMode}
          />
        )}

        {/* 5: What to do next */}
        <IncidentActions incident={incident} />

        {/* 6: Secondary Technical Details (tucked quietly at bottom) */}
        <div className="rounded-2xl bg-surface border border-subtle p-4 text-xs text-muted space-y-1 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span>Reference ID: <strong className="font-mono text-secondary">{incident.id}</strong></span>
            <span>Channel: <strong className="text-secondary">{incident.channel || "TELEPHONY"}</strong></span>
            <span>Source: <strong className="font-mono text-secondary">{incident.source}</strong></span>
          </div>
          <p className="text-[11px] text-muted pt-1">
            Recorded by SuSagi on Android device. Stored locally for your protection audit.
          </p>
        </div>
      </div>
    </div>
  );
};
