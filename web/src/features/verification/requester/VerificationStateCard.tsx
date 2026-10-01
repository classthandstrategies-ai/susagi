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
  if (status === "READY") {
    return (
      <div className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-5">
        <div className="w-12 h-12 rounded-2xl bg-brandSoft border border-brand/30 flex items-center justify-center text-brand">
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h2 className="text-xl sm:text-2xl font-bold text-primary">
            Ready to Request Confirmation
          </h2>
          <p className="text-sm text-secondary leading-relaxed">
            You can ask a trusted person to confirm this claim before taking any action.
          </p>
        </div>

        {isFixtureMode && onPreviewRequest && (
          <div className="pt-2">
            <button
              type="button"
              onClick={onPreviewRequest}
              className="w-full sm:w-auto min-h-[48px] px-6 py-3 rounded-xl bg-brand hover:bg-brandLight text-white font-semibold text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-2"
            >
              <span>Preview Verification Flow</span>
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
              </svg>
            </button>
          </div>
        )}
      </div>
    );
  }

  if (status === "PENDING") {
    return (
      <div className="rounded-2xl bg-surface border border-risk-caution/30 p-6 sm:p-8 space-y-5">
        <div className="w-12 h-12 rounded-2xl bg-risk-caution/15 border border-risk-caution/30 flex items-center justify-center text-risk-caution">
          <svg className="w-6 h-6 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h2 className="text-xl sm:text-2xl font-bold text-primary">
            Waiting for Confirmation
          </h2>
          <p className="text-sm text-secondary leading-relaxed">
            Awaiting response from your trusted person{session?.recipientName ? ` (${session.recipientName})` : ""}.
          </p>
        </div>

        <div className="rounded-xl bg-risk-caution/10 border border-risk-caution/25 p-4 text-xs text-risk-caution font-medium leading-relaxed">
          <strong>Protective Rule:</strong> Do not send money or share sensitive information while verification is pending.
        </div>

        {session?.expiresAt && (
          <div className="text-xs font-mono text-muted">
            Window concludes: {session.expiresAt}
          </div>
        )}
      </div>
    );
  }

  if (status === "VERIFIED") {
    return (
      <div className="rounded-2xl bg-surface border border-risk-low/40 p-6 sm:p-8 space-y-5">
        <div className="w-12 h-12 rounded-2xl bg-risk-low/15 border border-risk-low/30 flex items-center justify-center text-risk-low">
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h2 className="text-xl sm:text-2xl font-bold text-primary">
            Identity Confirmed by Trusted Person
          </h2>
          <p className="text-sm text-primary font-medium leading-relaxed">
            {outcomeNote || "Your trusted contact verified that this request originated from them."}
          </p>
        </div>

        <p className="text-xs text-secondary leading-relaxed pt-2 border-t border-subtle">
          Verification confirms the human response recorded in the session. Always ensure transactions follow normal authorized channels.
        </p>
      </div>
    );
  }

  if (status === "REJECTED") {
    return (
      <div className="rounded-2xl bg-surface border border-risk-critical/40 p-6 sm:p-8 space-y-5">
        <div className="w-12 h-12 rounded-2xl bg-risk-critical/15 border border-risk-critical/30 flex items-center justify-center text-risk-critical">
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h2 className="text-xl sm:text-2xl font-bold text-primary">
            Identity Could Not Be Verified
          </h2>
          <p className="text-sm text-risk-critical font-medium leading-relaxed">
            {outcomeNote || "Your trusted contact indicated that this request is not from them."}
          </p>
        </div>

        <div className="rounded-xl bg-risk-critical/10 border border-risk-critical/20 p-4 text-xs text-risk-critical font-semibold leading-relaxed">
          Action Required: Do not send money, execute UPI transfers, or share any OTPs or credentials.
        </div>
      </div>
    );
  }

  if (status === "EXPIRED") {
    return (
      <div className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-5">
        <div className="w-12 h-12 rounded-2xl bg-surfaceElevated border border-subtle flex items-center justify-center text-muted">
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>

        <div className="space-y-1.5">
          <h2 className="text-xl sm:text-2xl font-bold text-primary">
            Verification Expired
          </h2>
          <p className="text-sm text-secondary leading-relaxed">
            No response was received before the verification window ended.
          </p>
        </div>

        <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 text-xs text-secondary leading-relaxed">
          Guidance: Contact the person independently using a trusted, verified phone number before continuing.
        </div>
      </div>
    );
  }

  // UNAVAILABLE (Normal production companion runtime)
  return (
    <div className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-6 text-center max-w-2xl mx-auto my-4">
      <div className="w-14 h-14 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
        <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
        </svg>
      </div>

      <div className="space-y-2">
        <h2 className="text-xl sm:text-2xl font-bold text-primary">
          Identity verification is unavailable right now
        </h2>
        <p className="text-sm text-secondary max-w-lg mx-auto leading-relaxed">
          Identity verification is not connected yet. Contact the person independently before continuing.
        </p>
      </div>

      <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 text-left text-xs text-secondary leading-relaxed max-w-lg mx-auto">
        <strong>Safety Guidance:</strong> Do not send money or share sensitive information while identity is uncertain.
      </div>

      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link
          href="/live"
          className="w-full sm:w-auto min-h-[48px] px-5 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center"
        >
          Back to Live Defense
        </Link>
        <Link
          href="/guardians"
          className="w-full sm:w-auto min-h-[48px] px-5 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center"
        >
          View Guardian Circle
        </Link>
      </div>
    </div>
  );
};
