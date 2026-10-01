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
      return { label: "Ready", color: "bg-surfaceHighlight text-primary border-default" };
    case "PENDING":
      return { label: "Waiting for Confirmation", color: "bg-risk-caution/15 text-risk-caution border-risk-caution/30" };
    case "VERIFIED":
      return { label: "Confirmed by Trusted Person", color: "bg-risk-low/15 text-risk-low border-risk-low/30" };
    case "REJECTED":
      return { label: "Not Confirmed", color: "bg-risk-critical/15 text-risk-critical border-risk-critical/30" };
    case "EXPIRED":
      return { label: "Window Expired", color: "bg-surfaceElevated text-muted border-subtle" };
    case "UNAVAILABLE":
    default:
      return { label: "Unavailable", color: "bg-surfaceElevated text-secondary border-subtle" };
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
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            Identity Verification
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/40 uppercase">
              DEVELOPMENT FIXTURE — NO REMOTE REQUEST WAS SENT
            </span>
          )}
        </div>

        <div
          role="status"
          aria-label={`Verification Status: ${badge.label}`}
          className={cn(
            "px-3 py-1 rounded-full border text-xs font-mono font-semibold flex items-center gap-1.5",
            badge.color
          )}
        >
          <span className="w-2 h-2 rounded-full bg-current" aria-hidden="true" />
          <span>{badge.label}</span>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
            Identity Verification
          </h1>
          <p className="text-sm text-secondary max-w-xl leading-relaxed mt-1">
            Confirm caller claims or emergency transfer requests with your trusted circle.
          </p>
        </div>

        <Link
          href="/verification/respond"
          className="min-h-[48px] px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-brand hover:text-brandLight transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center self-start sm:self-auto"
        >
          Open Responder View →
        </Link>
      </div>
    </header>
  );
};
