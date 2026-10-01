"use client";

import React, { useState } from "react";
import { useRouter, useSearchParams, usePathname } from "next/navigation";

export const DevFixtureBar: React.FC = () => {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const currentFixture = searchParams.get("fixture") || "none";
  const [isOpen, setIsOpen] = useState(false);

  // Render ONLY in local development
  if (process.env.NODE_ENV !== "development") {
    return null;
  }

  const setFixture = (name: string) => {
    const params = new URLSearchParams(searchParams.toString());
    if (name === "none") {
      params.delete("fixture");
    } else {
      params.set("fixture", name);
    }
    const query = params.toString() ? `?${params.toString()}` : "";
    router.push(`${pathname}${query}`);
  };

  return (
    <aside
      aria-label="Development fixture controls"
      className="fixed bottom-20 md:bottom-4 right-4 z-50 text-xs font-mono select-none"
    >
      {isOpen ? (
        <div className="bg-surfaceElevated border border-brand/50 rounded-2xl p-4 shadow-2xl space-y-3 max-w-xs">
          <div className="flex items-center justify-between gap-2 border-b border-subtle pb-2">
            <div>
              <span className="text-brand font-bold text-[11px] uppercase tracking-wider block">
                DEVELOPMENT FIXTURES
              </span>
              <span className="text-[10px] text-muted">
                Preview simulated state transitions
              </span>
            </div>
            <button
              type="button"
              onClick={() => setIsOpen(false)}
              className="text-muted hover:text-primary px-1.5 py-0.5 rounded bg-surface border border-subtle"
              aria-label="Close fixture panel"
            >
              ✕
            </button>
          </div>

          <div className="space-y-1.5">
            <div className="text-[10px] uppercase font-semibold text-muted">
              Home / System States
            </div>
            <div className="grid grid-cols-2 gap-1.5">
              <button
                type="button"
                onClick={() => setFixture("none")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "none"
                    ? "bg-brand text-white font-semibold"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                Runtime (Default)
              </button>
              <button
                type="button"
                onClick={() => setFixture("normal")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "normal"
                    ? "bg-risk-low text-void font-bold"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                Normal Sync
              </button>
              <button
                type="button"
                onClick={() => setFixture("attention")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "attention"
                    ? "bg-risk-caution text-void font-bold"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                Attention Req.
              </button>
              <button
                type="button"
                onClick={() => setFixture("offline")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "offline"
                    ? "bg-surfaceHighlight text-primary font-bold border border-default"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                Offline Standby
              </button>
            </div>
          </div>

          <div className="space-y-1.5 pt-1 border-t border-subtle">
            <div className="text-[10px] uppercase font-semibold text-muted">
              Risk Scenarios (Live Defense)
            </div>
            <div className="grid grid-cols-2 gap-1.5">
              <button
                type="button"
                onClick={() => setFixture("low")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "low"
                    ? "bg-risk-low text-void font-bold"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                Low Risk
              </button>
              <button
                type="button"
                onClick={() => setFixture("caution")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "caution"
                    ? "bg-risk-caution text-void font-bold"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                Caution
              </button>
              <button
                type="button"
                onClick={() => setFixture("high")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "high"
                    ? "bg-risk-high text-void font-bold"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                High Risk
              </button>
              <button
                type="button"
                onClick={() => setFixture("critical")}
                className={`px-2 py-1 rounded text-left transition-colors text-[11px] ${
                  currentFixture === "critical"
                    ? "bg-risk-critical text-white font-bold"
                    : "bg-surface text-secondary hover:text-primary"
                }`}
              >
                Critical Alert
              </button>
            </div>
          </div>
        </div>
      ) : (
        <button
          type="button"
          onClick={() => setIsOpen(true)}
          className="bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-brand px-3.5 py-1.5 rounded-full shadow-lg text-[11px] font-semibold flex items-center gap-1.5 focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
        >
          <span className="w-2 h-2 rounded-full bg-brand animate-pulse" />
          <span>Dev Fixture: {currentFixture}</span>
        </button>
      )}
    </aside>
  );
};
