import React from "react";

export const ActivityLoadingSkeleton: React.FC = () => {
  return (
    <div
      role="status"
      aria-label="Loading activity records"
      className="space-y-4 max-w-4xl mx-auto"
    >
      <div className="flex items-center justify-between animate-pulse">
        <div className="h-4 w-40 bg-surfaceElevated rounded" />
        <div className="h-4 w-20 bg-surfaceElevated rounded" />
      </div>

      <div className="space-y-3">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="rounded-xl bg-surface border border-subtle p-5 space-y-3 animate-pulse"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="h-5 w-16 bg-surfaceElevated rounded-full" />
                <div className="h-3 w-28 bg-surfaceElevated rounded" />
              </div>
              <div className="h-3 w-32 bg-surfaceElevated rounded" />
            </div>

            <div className="h-5 w-3/4 bg-surfaceElevated rounded" />
            <div className="space-y-1.5 pt-1">
              <div className="h-3.5 w-full bg-surfaceElevated rounded" />
              <div className="h-3.5 w-2/3 bg-surfaceElevated rounded" />
            </div>

            <div className="flex items-center justify-between pt-3 border-t border-subtle">
              <div className="h-3 w-24 bg-surfaceElevated rounded" />
              <div className="h-3 w-20 bg-surfaceElevated rounded" />
            </div>
          </div>
        ))}
      </div>
      <span className="sr-only">Loading activity records...</span>
    </div>
  );
};
