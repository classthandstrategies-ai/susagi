"use client";

import React from "react";
import { ResponderLocalResult } from "./responderTypes";

interface ResponderResultStateProps {
  result: ResponderLocalResult;
  onReset: () => void;
}

export const ResponderResultState: React.FC<ResponderResultStateProps> = ({
  result,
  onReset,
}) => {
  const isVerified = result === "VERIFIED_PREVIEW";

  return (
    <div
      role="status"
      className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-4 text-center shadow-sm"
    >
      <div
        className={`w-12 h-12 rounded-2xl mx-auto flex items-center justify-center ${
          isVerified
            ? "bg-risk-low-soft text-risk-low border border-risk-low"
            : "bg-risk-critical-soft text-risk-critical border border-risk-critical"
        }`}
      >
        {isVerified ? (
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
          </svg>
        ) : (
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        )}
      </div>

      <div className="space-y-1.5">
        <h3 className="text-xl font-semibold text-primary">
          {isVerified ? "Response recorded: Confirmed" : "Response recorded: Declined"}
        </h3>
        <p className="text-sm text-secondary leading-relaxed">
          {isVerified
            ? "You confirmed that this request is really from you."
            : "You indicated that this request is not from you. A warning has been recorded."}
        </p>
      </div>

      <div className="pt-2">
        <button
          type="button"
          onClick={onReset}
          className="min-h-[44px] px-4 py-2 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
        >
          Reset preview
        </button>
      </div>
    </div>
  );
};
