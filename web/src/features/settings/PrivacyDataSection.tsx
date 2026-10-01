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
          className="text-lg font-semibold text-primary"
        >
          Privacy & data
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Guarantees regarding data collection, audio streams, and browser persistence.
        </p>
      </div>

      <div className="rounded-2xl bg-surface border border-subtle divide-y divide-subtle shadow-sm overflow-hidden">
        {/* Product Analytics Disclosures */}
        <div className="p-5 space-y-1">
          <div className="text-sm font-semibold text-primary">
            No analytics or third-party trackers
          </div>
          <p className="text-xs text-secondary leading-relaxed">
            This web companion operates locally and does not contain advertising pixels, behavioral trackers, or third-party analytics scripts.
          </p>
        </div>

        {/* Microphone and Audio Access */}
        <div className="p-5 space-y-1">
          <div className="text-sm font-semibold text-primary">
            No browser microphone access
          </div>
          <p className="text-xs text-secondary leading-relaxed">
            The web companion does not record or request access to your computer&apos;s microphone. All audio screening happens on-device on your Android phone.
          </p>
        </div>

        {/* Local Storage Only */}
        <div className="p-5 space-y-3">
          <div className="space-y-1">
            <div className="text-sm font-semibold text-primary">
              Local device storage
            </div>
            <p className="text-xs text-secondary leading-relaxed">
              Your preferences (such as reduced motion) are stored locally in your browser. No passwords or cryptographic keys are ever persisted unencrypted.
            </p>
          </div>

          <div className="pt-1 flex flex-col sm:flex-row items-start sm:items-center gap-3">
            <button
              type="button"
              onClick={handleClearPreferences}
              className="min-h-[44px] px-4 py-2 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center text-center"
            >
              Reset local preferences
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
