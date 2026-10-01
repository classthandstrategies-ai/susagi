import React from "react";
import { VerificationSession } from "@/types/verification";

interface VerificationContextCardProps {
  session: VerificationSession;
}

export const VerificationContextCard: React.FC<VerificationContextCardProps> = ({
  session,
}) => {
  return (
    <section
      aria-labelledby="session-context-heading"
      className="rounded-2xl bg-surface border border-subtle p-5 space-y-4"
    >
      <div className="flex items-center justify-between">
        <h3
          id="session-context-heading"
          className="text-xs font-semibold uppercase tracking-wider text-muted"
        >
          Verification Context
        </h3>
        <span className="text-[11px] font-mono text-muted">
          Session #{session.sessionId}
        </span>
      </div>

      <div className="space-y-3 text-xs">
        <div className="rounded-xl bg-surfaceElevated p-3.5 border border-subtle space-y-1">
          <span className="text-[10px] uppercase font-semibold text-muted">
            Caller Claim / Purpose
          </span>
          <p className="text-sm text-primary font-medium leading-relaxed">
            {session.claim}
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="rounded-xl bg-surfaceElevated p-3 border border-subtle">
            <span className="text-[10px] uppercase font-semibold text-muted block mb-0.5">
              Guardian Asked
            </span>
            <span className="text-xs font-semibold text-primary block truncate">
              {session.recipientName}
            </span>
            <span className="text-[11px] font-mono text-secondary">
              {session.recipientPhone}
            </span>
          </div>

          <div className="rounded-xl bg-surfaceElevated p-3 border border-subtle">
            <span className="text-[10px] uppercase font-semibold text-muted block mb-0.5">
              Window Timeline
            </span>
            <span className="text-xs text-secondary block">
              Initiated: {session.createdAt}
            </span>
            <span className="text-[11px] font-mono text-muted">
              Expires: {session.expiresAt}
            </span>
          </div>
        </div>
      </div>
    </section>
  );
};
