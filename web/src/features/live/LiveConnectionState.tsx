"use client";

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
      className="rounded-2xl bg-surface border border-subtle p-8 sm:p-10 space-y-6 text-center max-w-xl mx-auto shadow-sm"
    >
      <div className="w-14 h-14 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
        <svg
          className="w-7 h-7 text-secondary"
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
        <h2 id="standby-heading" className="text-xl sm:text-2xl font-semibold text-primary">
          No active protected call
        </h2>
        <p className="text-sm text-secondary leading-relaxed">
          When you receive or make a call on your protected Android phone, real-time safety guidance and alerts will appear here automatically.
        </p>
      </div>

      {/* Honest Status Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-left">
        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-xs font-medium text-muted mb-0.5">
            Phone protection
          </div>
          <div className="text-xs font-semibold text-primary">
            Waiting for call
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3.5 border border-subtle">
          <div className="text-xs font-medium text-muted mb-0.5">
            Identity verification
          </div>
          <div className="text-xs font-semibold text-primary">
            Ready on demand
          </div>
        </div>
      </div>

      {/* Quick Links */}
      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link
          href="/protect"
          className="w-full sm:w-auto px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center min-h-[44px] inline-flex items-center justify-center"
        >
          Review protect settings
        </Link>
        <Link
          href="/activity"
          className="w-full sm:w-auto px-4 py-2.5 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center min-h-[44px] inline-flex items-center justify-center"
        >
          View activity history
        </Link>
      </div>
    </section>
  );
};
