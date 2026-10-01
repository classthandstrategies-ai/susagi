"use client";

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
          <span className="text-xs font-medium text-secondary">
            Safety history
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded-full bg-surfaceElevated text-muted border border-subtle">
              Fixture preview
            </span>
          )}
        </div>

        {typeof totalCount === "number" && totalCount > 0 && (
          <span className="text-xs text-muted bg-surface px-2.5 py-1 rounded-full border border-subtle">
            {totalCount} {totalCount === 1 ? "entry" : "entries"}
          </span>
        )}
      </div>

      <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-primary">
        Activity
      </h1>

      <p className="text-sm text-secondary leading-relaxed">
        History of calls, messages, and security verifications.
      </p>
    </header>
  );
};
