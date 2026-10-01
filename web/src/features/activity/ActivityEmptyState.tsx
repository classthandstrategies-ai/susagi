import React from "react";
import Link from "next/link";

interface ActivityEmptyStateProps {
  type: "DISCONNECTED" | "EMPTY_CONNECTED" | "NO_SEARCH_RESULTS";
  onResetFilters?: () => void;
}

export const ActivityEmptyState: React.FC<ActivityEmptyStateProps> = ({
  type,
  onResetFilters,
}) => {
  if (type === "NO_SEARCH_RESULTS") {
    return (
      <div className="rounded-2xl bg-surface border border-subtle p-8 text-center space-y-4 max-w-xl mx-auto my-6">
        <div className="w-12 h-12 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
          <svg
            className="w-6 h-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={1.5}
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
            />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h3 className="text-base font-semibold text-primary">
            No matching activity records
          </h3>
          <p className="text-xs sm:text-sm text-secondary leading-relaxed">
            No safety events matched your search keywords or selected risk filters.
          </p>
        </div>

        {onResetFilters && (
          <div>
            <button
              type="button"
              onClick={onResetFilters}
              className="min-h-[48px] px-5 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
            >
              Clear Filters & Search
            </button>
          </div>
        )}
      </div>
    );
  }

  if (type === "EMPTY_CONNECTED") {
    return (
      <div className="rounded-2xl bg-surface border border-subtle p-8 text-center space-y-4 max-w-xl mx-auto my-6">
        <div className="w-12 h-12 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-risk-low">
          <svg
            className="w-6 h-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={1.5}
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h3 className="text-base font-semibold text-primary">
            No incidents recorded
          </h3>
          <p className="text-xs sm:text-sm text-secondary leading-relaxed">
            Your connected device has recorded zero suspicious calls, SMS phishing attempts, or safety incidents.
          </p>
        </div>
      </div>
    );
  }

  // DISCONNECTED: Normal production companion runtime
  return (
    <section
      aria-labelledby="empty-activity-heading"
      className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-6 max-w-2xl mx-auto my-4 text-center"
    >
      <div className="w-14 h-14 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
        <svg
          className="w-7 h-7"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={1.5}
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25"
          />
        </svg>
      </div>

      <div className="space-y-2">
        <h2
          id="empty-activity-heading"
          className="text-xl sm:text-2xl font-bold text-primary"
        >
          Activity service not connected
        </h2>
        <p className="text-sm text-secondary max-w-lg mx-auto leading-relaxed">
          Safety events will appear here when this companion is connected to SuSagi activity services.
        </p>
      </div>

      {/* Honest Status Breakdown */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-left">
        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Companion Sync
          </div>
          <div className="text-xs font-semibold text-primary">
            Not connected
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Telephony Events
          </div>
          <div className="text-xs font-semibold text-secondary">
            Waiting for device
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Recorded Ledger
          </div>
          <div className="text-xs font-semibold text-secondary">
            0 incidents
          </div>
        </div>
      </div>

      {/* Useful Next Step Actions */}
      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link
          href="/protect"
          className="w-full sm:w-auto min-h-[48px] px-5 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center"
        >
          Review Protect Settings
        </Link>
        <Link
          href="/live"
          className="w-full sm:w-auto min-h-[48px] px-5 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center"
        >
          Open Live Defense
        </Link>
      </div>
    </section>
  );
};
