"use client";

import React, { useState } from "react";
import Link from "next/link";
import { HomeViewState } from "./homeTypes";
import { HomeOverview } from "./HomeOverview";
import { QuickActions } from "./QuickActions";
import { ProtectionOverview } from "./ProtectionOverview";
import { RecentActivityPreview } from "./RecentActivityPreview";
import { GuardianCirclePreview } from "./GuardianCirclePreview";
import { LinkCheckDialog } from "@/features/protect/LinkCheckDialog";

interface HomePageClientProps {
  initialState: HomeViewState;
}

export const HomePageClient: React.FC<HomePageClientProps> = ({
  initialState,
}) => {
  const [isLinkCheckOpen, setIsLinkCheckOpen] = useState(false);
  const { overview, capabilities, recentIncidents, guardians, isFixtureMode } =
    initialState;

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Top Welcome / Orientation */}
      <header className="space-y-1">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            SuSagi Companion
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/30 uppercase">
              DEVELOPMENT FIXTURE MODE
            </span>
          )}
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
          Security & Call Defense
        </h1>
        <p className="text-sm text-secondary max-w-2xl leading-relaxed">
          Autonomous protection companion coordinating live call defense, identity checks, and fraud prevention.
        </p>
      </header>

      {/* Balanced 2-Column Responsive Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Main Column (8 cols on lg) */}
        <div className="lg:col-span-8 space-y-6">
          {/* 1. Protection Overview */}
          <HomeOverview overview={overview} isFixtureMode={isFixtureMode} />

          {/* 2. Quick Actions */}
          <QuickActions onOpenLinkCheck={() => setIsLinkCheckOpen(true)} />

          {/* 3. Protection Capabilities Summary */}
          <ProtectionOverview
            capabilities={capabilities}
            onOpenLinkCheck={() => setIsLinkCheckOpen(true)}
          />

          {/* 4. Recent Activity Preview */}
          <RecentActivityPreview
            incidents={recentIncidents}
            isFixtureMode={isFixtureMode}
          />
        </div>

        {/* Secondary Column (4 cols on lg) */}
        <div className="lg:col-span-4 space-y-6">
          {/* Protection Readiness & Setup Guide */}
          <section aria-labelledby="readiness-title" className="rounded-2xl bg-surface border border-subtle p-5 space-y-3">
            <h2 id="readiness-title" className="text-xs font-semibold uppercase tracking-wider text-muted">
              Companion Readiness
            </h2>

            <div className="space-y-2.5 text-xs text-secondary leading-relaxed">
              <div className="flex items-start gap-2">
                <span className="w-4 h-4 rounded-full bg-brandSoft text-brand flex items-center justify-center font-bold text-[10px] shrink-0 mt-0.5">
                  1
                </span>
                <p>
                  <strong className="text-primary">Standalone Web Companion:</strong> Inspect suspicious links, coordinate guardians, and review activity audits independently.
                </p>
              </div>

              <div className="flex items-start gap-2">
                <span className="w-4 h-4 rounded-full bg-surfaceElevated text-muted flex items-center justify-center font-bold text-[10px] shrink-0 mt-0.5">
                  2
                </span>
                <p>
                  <strong className="text-primary">Device Integration:</strong> Pair with the SuSagi Android app to enable autonomous call interception and on-device acoustic analysis.
                </p>
              </div>
            </div>

            <div className="pt-2 border-t border-subtle">
              <Link
                href="/settings"
                className="text-xs font-semibold text-brand hover:text-brandLight transition-colors"
              >
                Configure Pairing & Settings →
              </Link>
            </div>
          </section>

          {/* Guardian Circle Preview */}
          <GuardianCirclePreview
            guardians={guardians}
            isFixtureMode={isFixtureMode}
          />

          {/* Interactive Link Check Quick Card */}
          <section aria-labelledby="link-card-title" className="rounded-2xl bg-surface border border-subtle p-5 space-y-3">
            <div className="flex items-center gap-2 text-primary font-semibold text-sm">
              <svg className="w-4 h-4 text-brand" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
              </svg>
              <h2 id="link-card-title">Received a strange link?</h2>
            </div>
            <p className="text-xs text-secondary leading-relaxed">
              Before opening SMS or WhatsApp links from unknown senders, verify domain structure in the companion link inspector.
            </p>
            <button
              type="button"
              onClick={() => setIsLinkCheckOpen(true)}
              className="w-full py-2.5 px-4 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
            >
              Inspect Link Now
            </button>
          </section>
        </div>
      </div>

      {/* Accessible Interactive Link Check Dialog */}
      <LinkCheckDialog
        isOpen={isLinkCheckOpen}
        onClose={() => setIsLinkCheckOpen(false)}
        isFixtureMode={isFixtureMode}
      />
    </div>
  );
};
