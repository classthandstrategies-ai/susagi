import React from "react";

interface ActivityHeaderProps {
  isFixtureMode?: boolean;
  totalCount?: number;
}

export const ActivityHeader: React.FC<ActivityHeaderProps> = ({
  isFixtureMode = false,
  totalCount,
}) => {
  return (
    <header className="space-y-2">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            Safety Ledger
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/40 uppercase">
              DEVELOPMENT FIXTURE — NOT RUNTIME ACTIVITY
            </span>
          )}
        </div>

        {typeof totalCount === "number" && totalCount > 0 && (
          <span className="text-xs font-mono text-muted bg-surfaceElevated px-2.5 py-1 rounded-full border border-subtle">
            {totalCount} {totalCount === 1 ? "Incident" : "Incidents"}
          </span>
        )}
      </div>

      <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
        Activity
      </h1>

      <p className="text-sm text-secondary max-w-2xl leading-relaxed">
        {isFixtureMode
          ? "Demonstrating recorded scam attempts, intercepted impersonations, and verified resolutions."
          : "Historical record of safety alerts, potential scam signals, and protective guidance."}
      </p>
    </header>
  );
};
