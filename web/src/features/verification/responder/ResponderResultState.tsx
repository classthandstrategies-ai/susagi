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
      className="rounded-2xl bg-surface border border-subtle p-6 sm:p-8 space-y-4 text-center"
    >
      <div
        className={`w-12 h-12 rounded-2xl mx-auto flex items-center justify-center ${
          isVerified
            ? "bg-risk-low/15 text-risk-low border border-risk-low/30"
            : "bg-risk-critical/15 text-risk-critical border border-risk-critical/30"
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
        <h3 className="text-xl font-bold text-primary">
          {isVerified ? "Response Recorded: Authorized" : "Response Recorded: Declined"}
        </h3>
        <p className="text-sm text-secondary leading-relaxed">
          {isVerified
            ? "You confirmed locally that this request is from you."
            : "You indicated locally that this request did not come from you."}
        </p>
      </div>

      <div className="rounded-xl bg-surfaceElevated p-3 border border-subtle text-xs text-muted font-mono max-w-md mx-auto">
        Notice: Fixture response recorded locally for UI preview. No network response was transmitted to a remote backend.
      </div>

      <div className="pt-2">
        <button
          type="button"
          onClick={onReset}
          className="min-h-[48px] px-5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
        >
          Reset Decision Preview
        </button>
      </div>
    </div>
  );
};
