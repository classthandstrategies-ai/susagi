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
  const isAnalyzing = connectionState === "ANALYZING" || connectionState === "ASSESSMENT_AVAILABLE";

  return (
    <header className="space-y-2">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            Live Call Defense
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/40 uppercase">
              DEVELOPMENT FIXTURE — NOT LIVE ANALYSIS
            </span>
          )}
        </div>

        {/* Live UI Connection State Badge (Distinct from RiskLevel) */}
        <div
          role="status"
          aria-label={`Connection status: ${connectionState}`}
          className="flex items-center gap-2 px-3 py-1 rounded-full bg-surfaceElevated border border-subtle text-xs"
        >
          <span
            className={cn(
              "w-2 h-2 rounded-full",
              isAnalyzing ? "bg-risk-low animate-pulse" : "bg-muted"
            )}
            aria-hidden="true"
          />
          <span className="font-mono text-secondary font-medium">
            {connectionState.replace(/_/g, " ")}
          </span>
        </div>
      </div>

      <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
        Live Call Defense
      </h1>

      <p className="text-sm text-secondary max-w-2xl leading-relaxed">
        {isFixtureMode
          ? "Demonstrating semantic risk reasoning, caller claim analysis, and protective actions."
          : "Real-time speech-to-text telemetry and scam signal detection during active phone calls."}
      </p>
    </header>
  );
};
