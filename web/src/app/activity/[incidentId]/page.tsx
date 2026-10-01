import React from "react";
import Link from "next/link";
import { bankImpersonationIncident, familyImpersonationIncident } from "@/fixtures/incidentFixtures";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { TimelineItem } from "@/components/cards/TimelineItem";
import { SignalCard } from "@/components/cards/SignalCard";
import { EvidenceCard } from "@/components/cards/EvidenceCard";
import { formatProtectiveAction } from "@/lib/utils";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

interface IncidentDetailPageProps {
  params: Promise<{ incidentId: string }>;
}

export default async function IncidentDetailPage({
  params,
}: IncidentDetailPageProps) {
  const { incidentId } = await params;

  const incident =
    incidentId === bankImpersonationIncident.id
      ? bankImpersonationIncident
      : incidentId === familyImpersonationIncident.id
      ? familyImpersonationIncident
      : null;

  if (!incident) {
    return (
      <div className="space-y-6">
        <Link
          href="/activity"
          className="inline-flex items-center gap-2 text-sm text-brand hover:text-brandLight"
        >
          ← Back to Activity
        </Link>
        <div className="p-8 text-center rounded-2xl bg-surface border border-subtle space-y-2">
          <h1 className="text-xl font-semibold text-primary">
            Incident Not Found
          </h1>
          <p className="text-sm text-secondary">
            No incident details found for ID: {incidentId}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <OfflineBanner
        message={`Phase W1 Product Shell — Viewing incident detail (${incident._fixtureNotice}).`}
      />

      <div className="flex items-center gap-2">
        <Link
          href="/activity"
          className="inline-flex items-center gap-1.5 text-xs font-medium text-brand hover:text-brandLight transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          <span>Back to Activity Log</span>
        </Link>
      </div>

      {/* Main Header */}
      <header className="rounded-2xl bg-surface border border-subtle p-6 space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <RiskBadge level={incident.riskLevel} size="md" />
          <span className="text-xs text-muted font-mono">{incident.timestamp}</span>
        </div>

        <h1 className="text-2xl font-bold text-primary tracking-tight">
          {incident.title}
        </h1>

        <div className="flex flex-wrap items-center gap-4 text-xs text-secondary font-mono pt-1">
          <span>Source: {incident.source}</span>
          <span>•</span>
          <span>
            Action: {formatProtectiveAction(incident.recommendedAction)}
          </span>
          <span>•</span>
          <span className="text-brand">Status: {incident.status}</span>
        </div>

        <p className="text-sm text-secondary leading-relaxed pt-2 border-t border-subtle">
          {incident.summary}
        </p>
      </header>

      {/* Incident Progression Timeline */}
      <section aria-labelledby="timeline-heading" className="rounded-2xl bg-surface border border-subtle p-6 space-y-4">
        <h2 id="timeline-heading" className="text-base font-semibold text-primary">
          Incident Timeline
        </h2>

        <div className="pt-2">
          {incident.timeline.map((tl, index) => (
            <TimelineItem
              key={tl.id}
              item={tl}
              isLast={index === incident.timeline.length - 1}
            />
          ))}
        </div>
      </section>

      {/* Signals & Evidence Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <section aria-labelledby="detail-signals" className="space-y-3">
          <h2 id="detail-signals" className="text-sm font-semibold uppercase tracking-wider text-muted">
            Linguistic Scam Signals ({incident.signals.length})
          </h2>
          <div className="space-y-3">
            {incident.signals.map((sig) => (
              <SignalCard key={sig.id} signal={sig} />
            ))}
          </div>
        </section>

        <section aria-labelledby="detail-evidence" className="space-y-3">
          <h2 id="detail-evidence" className="text-sm font-semibold uppercase tracking-wider text-muted">
            Intercepted Audio Evidence
          </h2>
          <div className="space-y-3">
            {incident.evidence.map((evi) => (
              <EvidenceCard key={evi.id} evidence={evi} />
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
