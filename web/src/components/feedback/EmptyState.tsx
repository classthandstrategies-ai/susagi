import React from "react";
import { cn } from "@/lib/utils";

interface EmptyStateProps {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  description,
  actionLabel,
  onAction,
  className,
}) => {
  return (
    <div
      className={cn(
        "rounded-2xl border border-dashed border-subtle p-8 text-center bg-surface/50 max-w-lg mx-auto my-6",
        className
      )}
    >
      <div className="w-12 h-12 rounded-full bg-surfaceElevated border border-default mx-auto mb-3 flex items-center justify-center text-muted">
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
            d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.002A11.959 11.959 0 0112 2.714z"
          />
        </svg>
      </div>

      <h3 className="text-base font-semibold text-primary mb-1">{title}</h3>
      <p className="text-sm text-secondary leading-relaxed mb-4 max-w-sm mx-auto">
        {description}
      </p>

      {actionLabel && onAction && (
        <button
          type="button"
          onClick={onAction}
          className="px-4 py-2 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-sm font-medium text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
        >
          {actionLabel}
        </button>
      )}
    </div>
  );
};
