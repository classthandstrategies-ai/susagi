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
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            Protection Network
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/40 uppercase">
              DEVELOPMENT FIXTURE — NOT RUNTIME GUARDIAN DATA
            </span>
          )}
        </div>

        {guardianCount > 0 && (
          <span className="text-xs font-mono text-muted bg-surfaceElevated px-2.5 py-1 rounded-full border border-subtle">
            {guardianCount} {guardianCount === 1 ? "Guardian" : "Guardians"}
          </span>
        )}
      </div>

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
            Guardian Circle
          </h1>
          <p className="text-sm text-secondary max-w-xl leading-relaxed mt-1">
            Trusted people can help you verify unusual requests.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={onOpenEducationDialog}
            className="min-h-[48px] px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
          >
            How Guardian Circle Works
          </button>
          <button
            type="button"
            onClick={onOpenAddDialog}
            className="min-h-[48px] px-5 py-2.5 rounded-xl bg-brand hover:bg-brandLight text-white text-xs font-semibold transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-1.5 shadow-sm"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            <span>Add Trusted Person</span>
          </button>
        </div>
      </div>
    </header>
  );
};
