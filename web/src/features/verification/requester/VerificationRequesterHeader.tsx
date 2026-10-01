"use client";

import React from "react";
import Link from "next/link";
import { VerificationStatus } from "@/types/verification";
import { cn } from "@/lib/utils";

interface VerificationRequesterHeaderProps {
  status: VerificationStatus;
  isFixtureMode?: boolean;
}

const getStatusBadge = (status: VerificationStatus) => {
  switch (status) {
    case "READY":
      return { label: "Ready", color: "bg-surfaceElevated text-secondary border-subtle" };
    case "PENDING":
      return { label: "Waiting for confirmation", color: "bg-risk-caution-soft text-risk-caution border-risk-caution" };
    case "VERIFIED":
      return { label: "Confirmed by trusted person", color: "bg-risk-low-soft text-risk-low border-risk-low" };
    case "REJECTED":
      return { label: "Could not be verified", color: "bg-risk-critical-soft text-risk-critical border-risk-critical" };
    case "EXPIRED":
      return { label: "Window expired", color: "bg-surfaceElevated text-muted border-subtle" };
    case "UNAVAILABLE":
    default:
      return { label: "Standby", color: "bg-surfaceElevated text-secondary border-subtle" };
  }
};

export const VerificationRequesterHeader: React.FC<VerificationRequesterHeaderProps> = ({
  status,
  isFixtureMode = false,
}) => {
  const badge = getStatusBadge(status);

  return (
    <header className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-secondary">
            Identity verification
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded-full bg-surfaceElevated text-muted border border-subtle">
              Fixture preview
            </span>
          )}
        </div>

        <div
          role="status"
          aria-label={`Verification status: ${badge.label}`}
          className={cn(
            "px-3 py-1 rounded-full border text-xs font-medium flex items-center gap-1.5",
            badge.color
          )}
        >
          <span className="w-2 h-2 rounded-full bg-current" aria-hidden="true" />
          <span>{badge.label}</span>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-primary">
            Identity verification
          </h1>
          <p className="text-sm text-secondary leading-relaxed mt-1">
            Confirm caller claims or emergency transfer requests with your trusted circle.
          </p>
        </div>

        <Link
          href="/verification/respond"
          className="min-h-[44px] px-3.5 py-2 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center self-start sm:self-auto gap-1"
        >
          <span>Responder view</span>
          <span aria-hidden="true">›</span>
        </Link>
      </div>
    </header>
  );
};
