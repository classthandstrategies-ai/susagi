"use client";

import React, { useState } from "react";
import Link from "next/link";
import { VerificationResponderViewState, ResponderLocalResult } from "./responderTypes";
import { ResponderIdentityCard } from "./ResponderIdentityCard";
import { ResponderSafetyContext } from "./ResponderSafetyContext";
import { ResponderActions } from "./ResponderActions";
import { ResponderResultState } from "./ResponderResultState";

interface VerificationResponderPageClientProps {
  initialState: VerificationResponderViewState;
}

export const VerificationResponderPageClient: React.FC<
  VerificationResponderPageClientProps
> = ({ initialState }) => {
  const { hasRequest, session, isFixtureMode } = initialState;
  const [localResult, setLocalResult] = useState<ResponderLocalResult>("NONE");

  // Normal Production Runtime: No active incoming request
  if (!hasRequest || !session) {
    return (
      <div className="space-y-6 max-w-xl mx-auto my-6">
        <div className="flex items-center justify-between">
          <Link
            href="/verification"
            className="min-h-[44px] inline-flex items-center gap-1.5 text-xs font-medium text-secondary hover:text-primary transition-colors"
          >
            <span aria-hidden="true">‹</span>
            <span>Back to requester</span>
          </Link>
        </div>

        <section
          aria-labelledby="no-request-heading"
          className="rounded-2xl bg-surface border border-subtle p-8 text-center space-y-4 shadow-sm"
        >
          <div className="w-14 h-14 rounded-2xl bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-secondary">
            <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
            </svg>
          </div>

          <div className="space-y-1.5">
            <h1 id="no-request-heading" className="text-xl font-semibold text-primary">
              No verification request right now
            </h1>
            <p className="text-sm text-secondary max-w-md mx-auto leading-relaxed">
              When a family member or contact triggers an identity challenge, it will appear here for your confirmation.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
            <Link
              href="/guardians"
              className="min-h-[44px] px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center"
            >
              View Guardian Circle
            </Link>
            <Link
              href="/"
              className="min-h-[44px] px-4 py-2.5 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center"
            >
              Return to Home
            </Link>
          </div>
        </section>
      </div>
    );
  }

  // Development Fixture Mode or Active Request
  return (
    <div className="space-y-6 max-w-xl mx-auto my-4">
      {/* Top Nav & Fixture Badge */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          href="/verification"
          className="min-h-[44px] inline-flex items-center gap-1.5 text-xs font-medium text-secondary hover:text-primary transition-colors"
        >
          <span aria-hidden="true">‹</span>
          <span>Back to requester</span>
        </Link>

        {isFixtureMode && (
          <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded-full bg-surfaceElevated text-muted border border-subtle">
            Fixture preview
          </span>
        )}
      </div>

      <header className="space-y-1">
        <h1 className="text-2xl font-semibold tracking-tight text-primary">
          Identity check
        </h1>
        <p className="text-sm text-secondary">
          Someone is asking whether this sensitive request is really from you.
        </p>
      </header>

      {localResult !== "NONE" ? (
        <ResponderResultState
          result={localResult}
          onReset={() => setLocalResult("NONE")}
        />
      ) : (
        <div className="space-y-4">
          <ResponderIdentityCard session={session} />
          <ResponderSafetyContext />
          <ResponderActions
            onConfirmLegitimate={() => setLocalResult("VERIFIED_PREVIEW")}
            onRejectScam={() => setLocalResult("REJECTED_PREVIEW")}
          />
        </div>
      )}
    </div>
  );
};
