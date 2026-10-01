"use client";

import React, { useEffect, useSyncExternalStore } from "react";

const STORAGE_KEY = "susagi_reduce_motion";

function subscribe(callback: () => void) {
  window.addEventListener("storage", callback);
  return () => window.removeEventListener("storage", callback);
}

function getSnapshot(): boolean {
  if (typeof window === "undefined") return false;
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored !== null) {
    return stored === "true";
  }
  return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

function getServerSnapshot(): boolean {
  return false;
}

export const AccessibilitySection: React.FC = () => {
  const reduceMotion = useSyncExternalStore(
    subscribe,
    getSnapshot,
    getServerSnapshot
  );

  useEffect(() => {
    document.documentElement.setAttribute(
      "data-reduce-motion",
      reduceMotion ? "true" : "false"
    );
  }, [reduceMotion]);

  const handleToggleReduceMotion = () => {
    const nextState = !reduceMotion;
    localStorage.setItem(STORAGE_KEY, String(nextState));
    document.documentElement.setAttribute(
      "data-reduce-motion",
      nextState ? "true" : "false"
    );
    window.dispatchEvent(new Event("storage"));
  };

  return (
    <section aria-labelledby="accessibility-heading" className="space-y-4">
      <div className="space-y-1">
        <h2
          id="accessibility-heading"
          className="text-base sm:text-lg font-bold text-primary"
        >
          Accessibility & Display Preferences
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Browser-level ergonomic preferences for high-stress defense scenarios.
        </p>
      </div>

      <div className="space-y-3">
        {/* Reduce Motion Toggle */}
        <div className="rounded-xl bg-surface border border-subtle p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="text-sm font-semibold text-primary">
              Reduce Motion
            </div>
            <p className="text-xs text-secondary leading-relaxed max-w-xl">
              Minimizes radar sweeps, blinking indicators, and transitions across Live Defense, Activity, and Verification screens.
            </p>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <span className="text-xs font-mono text-muted">
              {reduceMotion ? "REDUCED" : "STANDARD"}
            </span>
            <button
              type="button"
              role="switch"
              aria-checked={reduceMotion}
              onClick={handleToggleReduceMotion}
              className={`relative inline-flex min-h-[48px] min-w-[56px] items-center rounded-full transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer p-1 ${
                reduceMotion
                  ? "bg-brand"
                  : "bg-surfaceElevated border border-subtle"
              }`}
              aria-label="Toggle reduce motion preference"
            >
              <span
                className={`inline-block h-6 w-6 rounded-full bg-white shadow-md transform transition-transform ${
                  reduceMotion ? "translate-x-6" : "translate-x-0"
                }`}
              />
            </button>
          </div>
        </div>

        {/* High-Contrast Defense Theme */}
        <div className="rounded-xl bg-surface border border-subtle p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-1">
            <div className="text-sm font-semibold text-primary">
              High-contrast interface
            </div>
            <p className="text-xs text-secondary leading-relaxed max-w-xl">
              Designed for strong text contrast, visible focus states, and distinct semantic alert colors to support rapid comprehension under stress.
            </p>
          </div>
          <div className="shrink-0 self-start sm:self-auto">
            <span className="text-xs px-2.5 py-1 rounded-md font-semibold bg-risk-low-soft text-risk-low border border-risk-low">
              ACTIVE
            </span>
          </div>
        </div>

        {/* Touch Target Standards */}
        <div className="rounded-xl bg-surface border border-subtle p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-1">
            <div className="text-sm font-semibold text-primary">
              48px minimum important control target
            </div>
            <p className="text-xs text-secondary leading-relaxed max-w-xl">
              All primary interactive controls across mobile, tablet, and desktop companion viewports maintain an enforced minimum 48×48px clickable target area.
            </p>
          </div>
          <div className="shrink-0 self-start sm:self-auto">
            <span className="text-xs px-2.5 py-1 rounded-md font-semibold bg-surfaceElevated text-secondary border border-subtle">
              48PX ENFORCED
            </span>
          </div>
        </div>
      </div>
    </section>
  );
};
