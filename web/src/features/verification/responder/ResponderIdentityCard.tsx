"use client";

import React from "react";
import { VerificationSession } from "@/types/verification";

interface ResponderIdentityCardProps {
  session: VerificationSession;
}

export const ResponderIdentityCard: React.FC<ResponderIdentityCardProps> = ({
  session,
}) => {
  return (
    <div className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-5 shadow-sm">
      <div className="space-y-2">
        <h2 className="text-xl sm:text-2xl font-semibold text-primary">
          Is this you speaking with {session.requesterName} right now?
        </h2>
        <p className="text-sm text-secondary leading-relaxed">
          Someone claiming to be you is asking <strong className="text-primary">{session.requesterName}</strong> for an emergency transfer. Please confirm if this is really you.
        </p>
      </div>

      <div className="rounded-xl bg-surfaceElevated p-4 border border-subtle space-y-1 text-xs">
        <span className="text-muted font-medium">Claimed request:</span>
        <p className="text-sm text-primary font-medium leading-relaxed">
          &ldquo;{session.claim}&rdquo;
        </p>
      </div>

      <div className="flex items-center justify-between text-xs text-muted pt-2 border-t border-subtle">
        <span>Requester: {session.requesterName} ({session.requesterPhone})</span>
        <span>Valid until {session.expiresAt}</span>
      </div>
    </div>
  );
};
