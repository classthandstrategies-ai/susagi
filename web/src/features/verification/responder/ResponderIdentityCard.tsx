import React from "react";
import { VerificationSession } from "@/types/verification";

interface ResponderIdentityCardProps {
  session: VerificationSession;
}

export const ResponderIdentityCard: React.FC<ResponderIdentityCardProps> = ({
  session,
}) => {
  return (
    <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-4">
      <div className="flex items-center justify-between text-xs text-muted">
        <span className="font-semibold uppercase tracking-wider">
          Incoming Identity Challenge
        </span>
        <span className="font-mono">
          Session #{session.sessionId}
        </span>
      </div>

      <div className="space-y-2">
        <h2 className="text-lg sm:text-xl font-bold text-primary">
          Are you making or authorizing this request?
        </h2>
        <div className="rounded-xl bg-surfaceElevated p-4 border border-subtle text-sm text-primary font-medium leading-relaxed">
          &ldquo;{session.claim}&rdquo;
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1 text-xs">
        <div className="rounded-xl bg-surfaceElevated p-3 border border-subtle">
          <span className="text-[10px] uppercase font-semibold text-muted block mb-0.5">
            Requester
          </span>
          <span className="text-xs font-semibold text-primary block">
            {session.requesterName}
          </span>
          <span className="text-[11px] font-mono text-secondary">
            {session.requesterPhone}
          </span>
        </div>

        <div className="rounded-xl bg-surfaceElevated p-3 border border-subtle">
          <span className="text-[10px] uppercase font-semibold text-muted block mb-0.5">
            Timing
          </span>
          <span className="text-xs text-secondary block">
            Received: {session.createdAt}
          </span>
          <span className="text-[11px] font-mono text-risk-caution">
            Valid: {session.expiresAt}
          </span>
        </div>
      </div>
    </div>
  );
};
