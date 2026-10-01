"use client";

import React from "react";

interface GuardianCircleHeaderProps {
  isFixtureMode?: boolean;
  guardianCount?: number;
  onOpenAddDialog: () => void;
  onOpenEducationDialog: () => void;
}

export const GuardianCircleHeader: React.FC<GuardianCircleHeaderProps> = ({
  isFixtureMode = false,
  guardianCount = 0,
  onOpenAddDialog,
  onOpenEducationDialog,
}) => {
  return (
    <header className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-secondary">
            Trusted contacts
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded-full bg-surfaceElevated text-muted border border-subtle">
              Fixture preview
            </span>
          )}
        </div>

        {guardianCount > 0 && (
          <span className="text-xs text-muted bg-surface px-2.5 py-1 rounded-full border border-subtle">
            {guardianCount} {guardianCount === 1 ? "person" : "people"}
          </span>
        )}
      </div>

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-primary">
            Guardian Circle
          </h1>
          <p className="text-sm text-secondary max-w-xl leading-relaxed mt-1">
            Trusted people who can help verify your identity or step in during suspected scams.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={onOpenEducationDialog}
            className="min-h-[44px] px-3.5 py-2 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
          >
            How it works
          </button>
          <button
            type="button"
            onClick={onOpenAddDialog}
            className="min-h-[44px] px-4 py-2 rounded-xl bg-brand hover:bg-brandLight text-white text-xs font-medium transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-1.5 shadow-sm"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            <span>Add trusted person</span>
          </button>
        </div>
      </div>
    </header>
  );
};
