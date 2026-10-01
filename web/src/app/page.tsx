import React from "react";
import Link from "next/link";
import { ProtectionStatusCard } from "@/components/cards/ProtectionStatusCard";
import { riskService } from "@/services/riskService";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

export default async function HomePage() {
  const protectionSummary = await riskService.getProtectionSummary();

  return (
    <div className="space-y-8">
      {/* Shell Phase Banner */}
      <OfflineBanner message="Phase W1 Product Shell Active. Production Home experience arriving in Phase W2." />

      {/* Hero Welcome Header */}
      <header className="space-y-2">
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
          SuSagi Web Companion
        </h1>
        <p className="text-sm sm:text-base text-secondary max-w-2xl leading-relaxed">
          Real-time protective intelligence, call defense monitoring, and trusted
          guardian coordination.
        </p>
      </header>

      {/* Companion Protection Status */}
      <section aria-labelledby="status-section-title" className="space-y-3">
        <h2 id="status-section-title" className="text-sm font-semibold uppercase tracking-wider text-muted">
          Current Protection State
        </h2>
        <ProtectionStatusCard summary={protectionSummary} />
      </section>

      {/* Quick Navigation Cards */}
      <section aria-labelledby="destinations-title" className="space-y-4">
        <h2 id="destinations-title" className="text-sm font-semibold uppercase tracking-wider text-muted">
          Primary Product Modules
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Link
            href="/protect"
            className="rounded-xl bg-surface border border-subtle hover:border-default p-5 transition-colors group focus-visible:ring-2 focus-visible:ring-brandLight"
          >
            <div className="w-10 h-10 rounded-lg bg-brandSoft text-brand flex items-center justify-center mb-3 group-hover:scale-105 transition-transform">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.002A11.959 11.959 0 0112 2.714z" />
              </svg>
            </div>
            <h3 className="text-base font-semibold text-primary group-hover:text-brand transition-colors">
              Protection Shields
            </h3>
            <p className="text-xs text-secondary mt-1">
              Active defense capabilities & scam detection.
            </p>
          </Link>

          <Link
            href="/live"
            className="rounded-xl bg-surface border border-subtle hover:border-default p-5 transition-colors group focus-visible:ring-2 focus-visible:ring-brandLight"
          >
            <div className="w-10 h-10 rounded-lg bg-risk-critical-soft text-risk-critical flex items-center justify-center mb-3 group-hover:scale-105 transition-transform">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />
              </svg>
            </div>
            <h3 className="text-base font-semibold text-primary group-hover:text-brand transition-colors">
              Live Call Defense
            </h3>
            <p className="text-xs text-secondary mt-1">
              Real-time transcript analysis & risk reasoning.
            </p>
          </Link>

          <Link
            href="/activity"
            className="rounded-xl bg-surface border border-subtle hover:border-default p-5 transition-colors group focus-visible:ring-2 focus-visible:ring-brandLight"
          >
            <div className="w-10 h-10 rounded-lg bg-surfaceHighlight text-secondary flex items-center justify-center mb-3 group-hover:scale-105 transition-transform">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5M9 11.25v1.5M12 9v3.75m3-6v6" />
              </svg>
            </div>
            <h3 className="text-base font-semibold text-primary group-hover:text-brand transition-colors">
              Security Activity
            </h3>
            <p className="text-xs text-secondary mt-1">
              Historical incident logs & evidence audits.
            </p>
          </Link>

          <Link
            href="/guardians"
            className="rounded-xl bg-surface border border-subtle hover:border-default p-5 transition-colors group focus-visible:ring-2 focus-visible:ring-brandLight"
          >
            <div className="w-10 h-10 rounded-lg bg-brandSoft text-brand flex items-center justify-center mb-3 group-hover:scale-105 transition-transform">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
              </svg>
            </div>
            <h3 className="text-base font-semibold text-primary group-hover:text-brand transition-colors">
              Guardian Circle
            </h3>
            <p className="text-xs text-secondary mt-1">
              Family safety network & identity challenges.
            </p>
          </Link>
        </div>
      </section>
    </div>
  );
}
