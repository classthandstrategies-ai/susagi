import React from "react";
import { cn } from "@/lib/utils";

interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
  className?: string;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title = "Something went wrong",
  message,
  onRetry,
  className,
}) => {
  return (
    <div
      role="alert"
      className={cn(
        "rounded-2xl border border-risk-critical/30 p-8 text-center bg-risk-critical-soft/30 max-w-lg mx-auto my-6",
        className
      )}
    >
      <div className="w-12 h-12 rounded-full bg-surfaceElevated border border-risk-critical/40 mx-auto mb-3 flex items-center justify-center text-risk-critical">
        <svg
          className="w-6 h-6"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"
          />
        </svg>
      </div>

      <h3 className="text-base font-semibold text-primary mb-1">{title}</h3>
      <p className="text-sm text-secondary leading-relaxed mb-4 max-w-sm mx-auto">
        {message}
      </p>

      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="px-4 py-2 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-sm font-medium text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
        >
          Try Again
        </button>
      )}
    </div>
  );
};
