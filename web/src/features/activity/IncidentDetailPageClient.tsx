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

  // When incident is unavailable (normal production runtime or nonexistent fixture ID)
  if (!incident) {
    return (
      <div className="space-y-6 max-w-2xl mx-auto my-6">
        <Link
          href={backHref}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand hover:text-brandLight transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          <span>Back to Activity</span>
        </Link>

        <section
          aria-labelledby="unavailable-heading"
          className="rounded-2xl bg-surface border border-subtle p-8 text-center space-y-4"
        >
          <div className="w-12 h-12 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
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
            <h1 id="unavailable-heading" className="text-xl font-bold text-primary">
              Incident unavailable
            </h1>
            <p className="text-sm text-secondary max-w-md mx-auto leading-relaxed">
              This incident is not available from the connected activity service.
            </p>
            <p className="text-xs font-mono text-muted">
              Reference ID: {incidentId}
            </p>
          </div>

          <div className="pt-2">
            <Link
              href={backHref}
              className="inline-flex px-5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
            >
              Back to Activity
            </Link>
          </div>
        </section>
      </div>
    );
  }

  // Loaded Incident View
  return (
    <div className="space-y-6 max-w-[1440px] mx-auto">
      {/* Top Navigation & Fixture Badge */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          href={backHref}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand hover:text-brandLight transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          <span>Back to Activity</span>
        </Link>

        {isFixtureMode && (
          <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/40 uppercase">
            DEVELOPMENT FIXTURE — NOT RUNTIME ACTIVITY
          </span>
        )}
      </div>

      {/* Main Responsive Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column (7 cols): Summary, Progression Timeline, Evidence */}
        <div className="lg:col-span-7 space-y-6">
          <IncidentSummary incident={incident} />
          <IncidentTimeline
            timeline={incident.timeline}
            isFixtureMode={isFixtureMode}
          />
          <IncidentEvidence
            signals={incident.signals}
            evidence={incident.evidence}
          />
        </div>

        {/* Right Column (5 cols): Recommended Follow-Up & Supported Actions */}
        <div className="lg:col-span-5 space-y-6">
          <IncidentActions incident={incident} />
        </div>
      </div>
    </div>
  );
};
