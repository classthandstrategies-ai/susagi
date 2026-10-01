"use client";

import React, { useState } from "react";
import Link from "next/link";
import { HomeViewState } from "./homeTypes";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { LinkCheckDialog } from "@/features/protect/LinkCheckDialog";

interface HomePageClientProps {
  initialState: HomeViewState;
}

export const HomePageClient: React.FC<HomePageClientProps> = ({
  initialState,
}) => {
  const [isLinkCheckOpen, setIsLinkCheckOpen] = useState(false);
  const [linkInput, setLinkInput] = useState("");
  const { overview, recentIncidents, guardians, isFixtureMode } = initialState;

  const handleLinkSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsLinkCheckOpen(true);
  };

  return (
    <div className="space-y-10 max-w-3xl mx-auto">
      {/* 1. Greeting / Simple Opening */}
      <header className="space-y-1.5 pt-2">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            Safety Companion
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/20 uppercase">
              Development fixture
            </span>
          )}
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
          Your protection
        </h1>
        <p className="text-sm text-secondary leading-relaxed">
          SuSagi helps you review suspicious activity, coordinate identity checks, and inspect links before you open them.
        </p>
      </header>

      {/* 2. DOMINANT STATE: Your Protection Card */}
      <section
        aria-labelledby="protection-dominant-title"
        className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-5 shadow-sm"
      >
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <span
                className={`w-2.5 h-2.5 rounded-full ${
                  overview.isDeviceConnected
                    ? "bg-risk-low"
                    : "bg-risk-caution"
                }`}
                aria-hidden="true"
              />
              <h2
                id="protection-dominant-title"
                className="text-lg sm:text-xl font-bold text-primary tracking-tight"
              >
                {overview.isDeviceConnected
                  ? overview.headline
                  : "Device protection isn't connected"}
              </h2>
            </div>
            <p className="text-sm text-secondary leading-relaxed max-w-xl">
              {overview.isDeviceConnected
                ? overview.statusDescription
                : "Connect SuSagi on your phone to see live protection status here."}
            </p>
          </div>
        </div>

        <div className="pt-2">
          <Link
            href="/protect"
            className="inline-flex items-center justify-center min-h-[44px] px-5 py-2.5 rounded-xl bg-brand text-white font-medium text-sm hover:bg-brandLight transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer shadow-sm"
          >
            Review protection
          </Link>
        </div>
      </section>

      {/* 3. Recent Safety Activity */}
      <section aria-labelledby="activity-section-title" className="space-y-3.5">
        <div className="flex items-center justify-between">
          <h2
            id="activity-section-title"
            className="text-base font-semibold text-primary"
          >
            Recent safety activity
          </h2>
          <Link
            href="/activity"
            className="text-xs font-medium text-secondary hover:text-primary transition-colors"
          >
            View all activity →
          </Link>
        </div>

        {recentIncidents.length === 0 ? (
          <div className="rounded-2xl bg-surface border border-subtle p-6 text-center space-y-3">
            <p className="text-sm text-secondary">
              No recent activity connected yet.
            </p>
            <Link
              href="/activity"
              className="inline-flex items-center justify-center min-h-[40px] px-4 py-2 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors"
            >
              View activity
            </Link>
          </div>
        ) : (
          <div className="space-y-2.5">
            {recentIncidents.slice(0, 3).map((incident) => (
              <Link
                key={incident.id}
                href={`/activity/${incident.id}`}
                className="block rounded-xl bg-surface border border-subtle hover:border-default p-4 sm:p-4.5 transition-colors group focus-visible:ring-2 focus-visible:ring-brandLight"
              >
                <div className="flex items-center justify-between gap-3 mb-1">
                  <div className="flex items-center gap-2">
                    <RiskBadge level={incident.riskLevel} size="sm" />
                    <span className="text-xs text-muted font-medium">
                      {incident.timestamp}
                    </span>
                  </div>
                  <span className="text-xs text-muted group-hover:text-primary transition-colors">
                    ›
                  </span>
                </div>
                <h3 className="text-sm font-semibold text-primary group-hover:text-brand transition-colors">
                  {incident.title}
                </h3>
                <p className="text-xs text-secondary mt-0.5 line-clamp-1">
                  {incident.summary}
                </p>
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* 4. Guardian Circle */}
      <section aria-labelledby="guardians-section-title" className="space-y-3.5">
        <div className="flex items-center justify-between">
          <h2
            id="guardians-section-title"
            className="text-base font-semibold text-primary"
          >
            Guardian Circle
          </h2>
          <Link
            href="/guardians"
            className="text-xs font-medium text-secondary hover:text-primary transition-colors"
          >
            Open Guardian Circle →
          </Link>
        </div>

        {guardians.length === 0 ? (
          <div className="rounded-2xl bg-surface border border-subtle p-6 text-center space-y-3">
            <p className="text-sm text-secondary">
              Trusted people will appear here when Guardian Circle is connected.
            </p>
            <Link
              href="/guardians"
              className="inline-flex items-center justify-center min-h-[40px] px-4 py-2 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors"
            >
              Add trusted person
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {guardians.slice(0, 4).map((guardian) => (
              <div
                key={guardian.id}
                className="rounded-xl bg-surface border border-subtle p-4 flex items-center gap-3.5"
              >
                <div
                  className="w-10 h-10 rounded-full bg-surfaceElevated text-primary font-semibold text-sm flex items-center justify-center shrink-0 border border-subtle"
                  aria-hidden="true"
                >
                  {guardian.avatarInitials}
                </div>
                <div className="min-w-0">
                  <div className="text-sm font-semibold text-primary truncate">
                    {guardian.name}
                  </div>
                  <div className="text-xs text-muted truncate">
                    {guardian.relationship}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* 5. Check a Suspicious Link */}
      <section
        aria-labelledby="link-check-section-title"
        className="rounded-2xl bg-surface border border-subtle p-6 sm:p-7 space-y-3 shadow-sm"
      >
        <div className="space-y-1">
          <h2
            id="link-check-section-title"
            className="text-base font-semibold text-primary"
          >
            Check a suspicious link
          </h2>
          <p className="text-xs sm:text-sm text-secondary">
            Paste a link before you open it to inspect its address and structure.
          </p>
        </div>

        <form onSubmit={handleLinkSubmit} className="flex flex-col sm:flex-row gap-2.5 pt-1">
          <input
            type="text"
            value={linkInput}
            onChange={(e) => setLinkInput(e.target.value)}
            placeholder="https://example.com/verify..."
            className="flex-1 min-h-[44px] px-4 py-2 rounded-xl bg-surfaceElevated border border-default text-sm text-primary placeholder:text-muted focus:outline-none focus:ring-2 focus:ring-brand"
            aria-label="Paste suspicious link here"
          />
          <button
            type="submit"
            className="min-h-[44px] px-5 py-2 rounded-xl bg-brand text-white font-medium text-sm hover:bg-brandLight transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer shrink-0"
          >
            Check link
          </button>
        </form>
      </section>

      {/* Accessible Interactive Link Check Dialog */}
      <LinkCheckDialog
        isOpen={isLinkCheckOpen}
        onClose={() => setIsLinkCheckOpen(false)}
        initialUrl={linkInput}
        isFixtureMode={isFixtureMode}
      />
    </div>
  );
};
