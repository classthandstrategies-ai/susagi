import React from "react";
import Link from "next/link";

interface GuardianEmptyStateProps {
  type: "DISCONNECTED" | "EMPTY_CONNECTED";
  onOpenEducationDialog: () => void;
}

export const GuardianEmptyState: React.FC<GuardianEmptyStateProps> = ({
  type,
  onOpenEducationDialog,
}) => {
  if (type === "EMPTY_CONNECTED") {
    return (
      <div className="rounded-2xl bg-surface border border-subtle p-8 text-center space-y-4 max-w-xl mx-auto my-6">
        <div className="w-12 h-12 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h3 className="text-base font-semibold text-primary">
            No Guardians configured yet
          </h3>
          <p className="text-xs sm:text-sm text-secondary leading-relaxed">
            Add trusted family members or friends who can help confirm sensitive or unverified requests.
          </p>
        </div>
      </div>
    );
  }

  // Normal production runtime disconnected state
  return (
    <section
      aria-labelledby="guardian-disconnected-heading"
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
            d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z"
          />
        </svg>
      </div>

      <div className="space-y-2">
        <h2
          id="guardian-disconnected-heading"
          className="text-xl sm:text-2xl font-bold text-primary"
        >
          Guardian service not connected
        </h2>
        <p className="text-sm text-secondary max-w-lg mx-auto leading-relaxed">
          Guardian contacts will appear here when this companion is connected to a SuSagi Guardian service.
        </p>
      </div>

      {/* Honest Breakdown Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-left">
        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Guardian Network
          </div>
          <div className="text-xs font-semibold text-primary">
            Not connected
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Contact Sync
          </div>
          <div className="text-xs font-semibold text-secondary">
            Waiting for service
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Trusted Members
          </div>
          <div className="text-xs font-semibold text-secondary">
            Not connected
          </div>
        </div>
      </div>

      {/* Educational Callout */}
      <div className="bg-surfaceElevated rounded-xl p-4 border border-subtle text-left space-y-1.5 text-xs text-secondary leading-relaxed">
        <span className="font-semibold text-primary block">
          How Guardian Circle Works:
        </span>
        <p>
          When something feels unusual, SuSagi can help you ask a trusted person to confirm a sensitive request through a connected verification service.
        </p>
      </div>

      {/* Useful Next Step Actions */}
      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <button
          type="button"
          onClick={onOpenEducationDialog}
          className="w-full sm:w-auto min-h-[48px] px-5 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center cursor-pointer"
        >
          Learn how Guardian Circle works
        </button>
        <Link
          href="/protect"
          className="w-full sm:w-auto min-h-[48px] px-5 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center"
        >
          Review Protect Settings
        </Link>
      </div>
    </section>
  );
};
