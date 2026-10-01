import React from "react";
import Link from "next/link";
import { GuardianCard } from "@/components/cards/GuardianCard";
import { guardianCircleFixture } from "@/fixtures/guardianFixtures";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

export default function GuardiansPage() {
  return (
    <div className="space-y-6">
      <OfflineBanner
        message={`Phase W1 Product Shell — Displaying trusted circle (${guardianCircleFixture._fixtureNotice}).`}
      />

      <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-primary">
            Guardian Circle
          </h1>
          <p className="text-sm text-secondary">
            Trusted contacts who can confirm your safety or authenticate emergency requests.
          </p>
        </div>

        <Link
          href="/verification"
          className="inline-flex items-center justify-center px-4 py-2.5 rounded-xl bg-brand hover:bg-blue-600 text-white text-sm font-semibold transition-colors focus-visible:ring-2 focus-visible:ring-brandLight select-none"
        >
          Verify Someone&apos;s Identity
        </Link>
      </header>

      {/* Passphrase Status Banner */}
      <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-brandSoft text-brand flex items-center justify-center">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
          <div>
            <h3 className="text-sm font-semibold text-primary">
              Family Secret Word Configured
            </h3>
            <p className="text-xs text-secondary">
              Protects against voice cloning by requiring a pre-shared passphrase.
            </p>
          </div>
        </div>
        <span className="text-xs font-semibold px-2 py-1 rounded bg-risk-low-soft text-risk-low border border-risk-low">
          ACTIVE
        </span>
      </div>

      {/* Guardian Contacts Grid */}
      <section aria-labelledby="guardians-list-heading" className="space-y-3">
        <h2 id="guardians-list-heading" className="text-sm font-semibold uppercase tracking-wider text-muted">
          Active Guardians ({guardianCircleFixture.guardians.length})
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {guardianCircleFixture.guardians.map((guardian) => (
            <GuardianCard key={guardian.id} guardian={guardian} />
          ))}
        </div>
      </section>
    </div>
  );
}
