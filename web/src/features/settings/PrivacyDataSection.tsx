"use client";

import React, { useState } from "react";

export const PrivacyDataSection: React.FC = () => {
  const [clearedMessage, setClearedMessage] = useState<string | null>(null);

  const handleClearPreferences = () => {
    try {
      localStorage.removeItem("susagi_reduce_motion");
      document.documentElement.removeAttribute("data-reduce-motion");
      setClearedMessage("Local browser preferences reset to defaults.");
      setTimeout(() => {
        setClearedMessage(null);
      }, 3500);
    } catch {
      setClearedMessage("Unable to access local browser storage.");
    }
  };

  return (
    <section aria-labelledby="privacy-heading" className="space-y-4">
      <div className="space-y-1">
        <h2
          id="privacy-heading"
          className="text-base sm:text-lg font-bold text-primary"
        >
          Privacy & Data Disclosures
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Guarantees regarding data collection, audio streams, and browser persistence.
        </p>
      </div>

      <div className="space-y-3">
        {/* Zero Cloud Telemetry */}
        <div className="rounded-xl bg-surface border border-subtle p-4 space-y-1">
          <div className="flex items-center justify-between gap-2">
            <h3 className="text-sm font-semibold text-primary">
              Zero Cloud Telemetry & Tracking
            </h3>
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-risk-low-soft text-risk-low border border-risk-low">
              VERIFIED LOCAL
            </span>
          </div>
          <p className="text-xs text-secondary leading-relaxed">
            The SuSagi Web Companion transmits no user tracking, analytical beacons, behavioral cookies, or diagnostic logs to third-party endpoints.
          </p>
        </div>

        {/* Zero Audio Ingestion */}
        <div className="rounded-xl bg-surface border border-subtle p-4 space-y-1">
          <div className="flex items-center justify-between gap-2">
            <h3 className="text-sm font-semibold text-primary">
              No Audio or Call Ingestion
            </h3>
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-surfaceElevated text-secondary border border-subtle">
              NO PERMISSION
            </span>
          </div>
          <p className="text-xs text-secondary leading-relaxed">
            This web interface never requests microphone or telephony permissions. Spoken audio is never recorded, buffered, or transmitted by the companion browser client.
          </p>
        </div>

        {/* Local Storage Only */}
        <div className="rounded-xl bg-surface border border-subtle p-4 space-y-3">
          <div className="space-y-1">
            <h3 className="text-sm font-semibold text-primary">
              Browser Storage Boundary
            </h3>
            <p className="text-xs text-secondary leading-relaxed">
              Companion state (such as reduced motion preferences) is stored strictly on this device inside your browser&apos;s standard local storage. No credentials or encryption keys are written.
            </p>
          </div>

          <div className="pt-1 flex flex-col sm:flex-row items-start sm:items-center gap-3">
            <button
              type="button"
              onClick={handleClearPreferences}
              className="min-h-[48px] px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center text-center"
            >
              Reset Local Preferences
            </button>
            {clearedMessage && (
              <span className="text-xs font-medium text-risk-low bg-risk-low-soft px-3 py-1.5 rounded-lg border border-risk-low">
                {clearedMessage}
              </span>
            )}
          </div>
        </div>
      </div>
    </section>
  );
};
