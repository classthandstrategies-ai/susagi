"use client";

import React from "react";
import Link from "next/link";
import { VerificationSession, VerificationStatus } from "@/types/verification";

interface VerificationStateCardProps {
  status: VerificationStatus;
  session: VerificationSession | null;
  outcomeNote?: string;
  isFixtureMode: boolean;
  onPreviewRequest?: () => void;
}

export const VerificationStateCard: React.FC<VerificationStateCardProps> = ({
  status,
  session,
  outcomeNote,
  isFixtureMode,
  onPreviewRequest,
}) => {
  const callerName = session?.requesterName || "Caller claiming identity";
  const verifierName = session?.recipientName || "Trusted guardian";
  const claimText = session?.claim || "Requesting urgent transfer of funds";

  // Person-first presentation
  const renderPersonHeader = () => (
    <div className="flex items-center gap-3.5 pb-4 border-b border-subtle">
      <div className="w-12 h-12 rounded-full bg-surfaceElevated border border-subtle flex items-center justify-center text-primary font-semibold text-base shrink-0">
        {callerName.charAt(0)}
      </div>
      <div className="space-y-0.5">
        <div className="flex items-center gap-2">
          <span className="font-semibold text-primary text-base">
            {callerName}
          </span>
          <span className="text-[11px] px-2 py-0.5 rounded-full bg-surfaceElevated text-secondary border border-subtle">
            Claimed caller
          </span>
        </div>
        <p className="text-xs text-muted">
          {session?.requesterPhone ? session.requesterPhone : "Phone call in progress"}
        </p>
      </div>
    </div>
  );

  if (status === "READY") {
    return (
      <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-5 shadow-sm">
        {session && renderPersonHeader()}

        <div className="space-y-1">
          <h2 className="text-lg font-semibold text-primary">
            Ready to verify identity
          </h2>
          <p className="text-sm text-secondary leading-relaxed">
            Ask <strong className="text-primary">{verifierName}</strong> to confirm whether this request is actually from them before sending money or sharing information.
          </p>
        </div>

        <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 space-y-1 text-xs">
          <span className="text-muted font-medium">Claimed purpose:</span>
          <p className="text-primary font-medium">{claimText}</p>
        </div>

        {isFixtureMode && onPreviewRequest && (
          <div className="pt-2">
            <button
              type="button"
              onClick={onPreviewRequest}
              className="min-h-[44px] px-5 py-2.5 rounded-xl bg-brand hover:bg-brandLight text-white font-medium text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-2"
            >
              <span>Send verification request</span>
              <span aria-hidden="true">›</span>
            </button>
          </div>
        )}
      </div>
    );
  }

  if (status === "PENDING") {
    return (
      <div className="rounded-2xl bg-surface border border-risk-caution p-6 space-y-5 shadow-sm">
        {session && renderPersonHeader()}

        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-risk-caution animate-pulse" />
            <h2 className="text-lg font-semibold text-primary">
              Waiting for confirmation
            </h2>
          </div>
          <p className="text-sm text-secondary leading-relaxed">
            Awaiting response from <strong className="text-primary">{verifierName}</strong>.
          </p>
        </div>

        <div className="rounded-xl bg-risk-caution-soft border border-risk-caution/30 p-4 text-xs text-risk-caution font-medium leading-relaxed">
          Do not send money or share sensitive information while verification is pending.
        </div>

        <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 space-y-1 text-xs">
          <span className="text-muted font-medium">Claimed purpose:</span>
          <p className="text-primary font-medium">{claimText}</p>
        </div>
      </div>
    );
  }

  if (status === "VERIFIED") {
    return (
      <div className="rounded-2xl bg-surface border border-risk-low p-6 space-y-5 shadow-sm">
        {session && renderPersonHeader()}

        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <span className="w-5 h-5 rounded-full bg-risk-low text-white text-xs flex items-center justify-center font-bold">
              ✓
            </span>
            <h2 className="text-lg font-semibold text-primary">
              Identity confirmed
            </h2>
          </div>
          <p className="text-sm text-secondary leading-relaxed">
            {outcomeNote || `${verifierName} confirmed that this request is from them.`}
          </p>
        </div>

        <p className="text-xs text-secondary leading-relaxed pt-2 border-t border-subtle">
          Identity confirmation does not by itself mean a payment or transaction is safe.
        </p>
      </div>
    );
  }

  if (status === "REJECTED") {
    return (
      <div className="rounded-2xl bg-surface border border-risk-critical p-6 space-y-5 shadow-sm">
        {session && renderPersonHeader()}

        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <span className="w-5 h-5 rounded-full bg-risk-critical text-white text-xs flex items-center justify-center font-bold">
              !
            </span>
            <h2 className="text-lg font-semibold text-primary">
              Identity could not be verified
            </h2>
          </div>
          <p className="text-sm text-secondary leading-relaxed">
            {outcomeNote || `${verifierName} says this request is not from them.`}
          </p>
        </div>

        <div className="rounded-xl bg-risk-critical-soft border border-risk-critical/30 p-4 text-xs text-risk-critical font-medium leading-relaxed">
          Do not send money or share sensitive information. End the call immediately.
        </div>
      </div>
    );
  }

  if (status === "EXPIRED") {
    return (
      <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-5 shadow-sm">
        {session && renderPersonHeader()}

        <div className="space-y-1">
          <h2 className="text-lg font-semibold text-primary">
            Verification expired
          </h2>
          <p className="text-sm text-secondary leading-relaxed">
            No response was received before the verification window ended.
          </p>
        </div>

        <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 text-xs text-secondary leading-relaxed">
          Contact the person independently using a trusted, verified phone number before continuing.
        </div>
      </div>
    );
  }

  // UNAVAILABLE (Standby)
  return (
    <div className="rounded-2xl bg-surface border border-subtle p-8 space-y-6 text-center max-w-xl mx-auto shadow-sm">
      <div className="w-14 h-14 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-secondary">
        <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
        </svg>
      </div>

      <div className="space-y-2">
        <h2 className="text-xl font-semibold text-primary">
          Identity verification is in standby
        </h2>
        <p className="text-sm text-secondary leading-relaxed">
          When identity verification is connected, you can ask someone in your Guardian Circle to confirm whether a sensitive request is really from them.
        </p>
        <p className="text-xs text-secondary leading-relaxed">
          Contact the person another way before continuing.
        </p>
      </div>

      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link
          href="/live"
          className="min-h-[44px] px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center"
        >
          View Live Defense
        </Link>
        <Link
          href="/guardians"
          className="min-h-[44px] px-4 py-2.5 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center"
        >
          Manage Guardian Circle
        </Link>
      </div>
    </div>
  );
};
