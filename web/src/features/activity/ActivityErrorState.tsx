import React from "react";
import Link from "next/link";

interface ActivityErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export const ActivityErrorState: React.FC<ActivityErrorStateProps> = ({
  message = "The activity service is unavailable.",
  onRetry,
}) => {
  return (
    <section
      aria-labelledby="error-activity-heading"
      className="rounded-2xl bg-surface border border-risk-caution/30 p-6 sm:p-8 space-y-5 max-w-xl mx-auto my-6 text-center"
    >
      <div className="w-12 h-12 rounded-2xl bg-risk-caution/10 border border-risk-caution/30 mx-auto flex items-center justify-center text-risk-caution">
        <svg
          className="w-6 h-6"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={1.5}
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"
          />
        </svg>
      </div>

      <div className="space-y-1.5">
        <h2
          id="error-activity-heading"
          className="text-lg sm:text-xl font-bold text-primary"
        >
          Activity couldn&apos;t be loaded
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed max-w-md mx-auto">
          {message}
        </p>
      </div>

      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        {onRetry ? (
          <button
            type="button"
            onClick={onRetry}
            className="w-full sm:w-auto px-5 py-2.5 rounded-xl bg-brand hover:bg-brandLight text-xs font-semibold text-white transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
          >
            Try Again
          </button>
        ) : (
          <Link
            href="/protect"
            className="w-full sm:w-auto px-5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center"
          >
            Review Protection Settings
          </Link>
        )}
        <Link
          href="/"
          className="w-full sm:w-auto px-5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center"
        >
          Return to Home
        </Link>
      </div>
    </section>
  );
};
