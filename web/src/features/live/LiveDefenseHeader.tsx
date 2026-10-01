"use client";

import React from "react";
import { LiveConnectionState } from "@/types/risk";
import { cn } from "@/lib/utils";

interface LiveDefenseHeaderProps {
  connectionState: LiveConnectionState;
  isFixtureMode?: boolean;
}

export const LiveDefenseHeader: React.FC<LiveDefenseHeaderProps> = ({
  connectionState,
  isFixtureMode = false,
}) => {
  const isAnalyzing =
    connectionState === "ANALYZING" || connectionState === "ASSESSMENT_AVAILABLE";

  return (
    <header className="space-y-2">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-secondary">
            Call protection
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded-full bg-surfaceElevated text-muted border border-subtle">
              Fixture preview
            </span>
          )}
        </div>

        {/* Live UI Connection State Badge */}
        <div
          role="status"
          aria-label={`Connection status: ${connectionState}`}
          className="flex items-center gap-2 px-3 py-1 rounded-full bg-surface border border-subtle text-xs"
        >
          <span
            className={cn(
              "w-2 h-2 rounded-full",
              isAnalyzing ? "bg-risk-low animate-pulse" : "bg-muted"
            )}
            aria-hidden="true"
          />
          <span className="text-secondary font-medium">
            {isAnalyzing ? "Active call" : "Standby"}
          </span>
        </div>
      </div>

      <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-primary">
        Live call defense
      </h1>

      <p className="text-sm text-secondary leading-relaxed">
        {isFixtureMode
          ? "Demonstrating scam pattern detection, caller verification, and protective steps."
          : "Real-time safety guidance and scam detection during active phone calls on your Android phone."}
      </p>
    </header>
  );
};
