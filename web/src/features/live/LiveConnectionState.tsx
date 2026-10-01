import React from "react";
import Link from "next/link";
import { LiveConnectionState as ConnectionStateType } from "@/types/risk";

interface LiveConnectionStateProps {
  state: ConnectionStateType;
}

export const LiveConnectionState: React.FC<LiveConnectionStateProps> = () => {
  return (
    <section
      aria-labelledby="standby-heading"
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
            d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z"
          />
        </svg>
      </div>

      <div className="space-y-2">
        <h2 id="standby-heading" className="text-xl sm:text-2xl font-bold text-primary">
          Device service not connected
        </h2>
        <p className="text-sm text-secondary max-w-lg mx-auto leading-relaxed">
          Live risk information will appear here when a supported SuSagi device or service connects to this companion.
        </p>
      </div>

      {/* Honest Connection Status Breakdown */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-left">
        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Device Service
          </div>
          <div className="text-xs font-semibold text-primary">
            Not connected
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Risk Assessment
          </div>
          <div className="text-xs font-semibold text-secondary">
            Waiting for connection
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-1">
            Identity Verification
          </div>
          <div className="text-xs font-semibold text-secondary">
            Unavailable
          </div>
        </div>
      </div>

      {/* Next Step Navigation Actions */}
      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link
          href="/protect"
          className="w-full sm:w-auto px-5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center"
        >
          Review Protect Settings
        </Link>
        <Link
          href="/activity"
          className="w-full sm:w-auto px-5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center"
        >
          View Activity
        </Link>
      </div>
    </section>
  );
};
