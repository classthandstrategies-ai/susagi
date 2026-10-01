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
          className="text-lg font-semibold text-primary"
        >
          Accessibility & display
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Display preferences designed for clarity and ease of reading.
        </p>
      </div>

      <div className="rounded-2xl bg-surface border border-subtle divide-y divide-subtle shadow-sm overflow-hidden">
        {/* Reduce Motion Toggle */}
        <div className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="text-sm font-semibold text-primary">
              Reduce motion
            </div>
            <p className="text-xs text-secondary leading-relaxed max-w-xl">
              Minimizes animations, blinking indicators, and transitions across Live Defense and Activity.
            </p>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <button
              type="button"
              role="switch"
              aria-checked={reduceMotion}
              onClick={handleToggleReduceMotion}
              className={`relative inline-flex min-h-[44px] min-w-[56px] items-center rounded-full transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer p-1 ${
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

        {/* High-Contrast Reading Mode info */}
        <div className="p-5 space-y-1">
          <div className="text-sm font-semibold text-primary">
            High-contrast readable surfaces
          </div>
          <p className="text-xs text-secondary leading-relaxed">
            All text and safety guidance cards are styled with high-contrast ratios exceeding WCAG AAA standards for optimal readability on all screens.
          </p>
        </div>
      </div>
    </section>
  );
};
