import React from "react";
import { IncidentCard } from "@/components/cards/IncidentCard";
import { sampleIncidentsList } from "@/fixtures/incidentFixtures";
import { FIXTURE_NOTICE } from "@/fixtures/metadata";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

export default function ActivityPage() {
  return (
    <div className="space-y-6">
      <OfflineBanner
        message={`Phase W1 Product Shell — Displaying sample incident log (${FIXTURE_NOTICE}).`}
      />

      <header className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-primary">
          Security Activity & Incident Audit
        </h1>
        <p className="text-sm text-secondary">
          Historical log of intercepted calls, scam flags, and verified guardian interactions.
        </p>
      </header>

      <section aria-labelledby="incidents-list-heading" className="space-y-4">
        <h2 id="incidents-list-heading" className="sr-only">
          Recent Incidents
        </h2>

        <div className="space-y-3">
          {sampleIncidentsList.map((incident) => (
            <IncidentCard key={incident.id} incident={incident} />
          ))}
        </div>
      </section>
    </div>
  );
}
