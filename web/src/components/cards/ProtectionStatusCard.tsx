import React from "react";
import { WebProtectionOverview } from "@/types/protection";
import { cn } from "@/lib/utils";

interface ProtectionStatusCardProps {
  overview: WebProtectionOverview;
  isFixture?: boolean;
  className?: string;
}

export const ProtectionStatusCard: React.FC<ProtectionStatusCardProps> = ({
  overview,
  isFixture = false,
  className,
}) => {
  const isConnected = overview.isDeviceConnected;
  const isReady = overview.state === "READY";

  return (
    <section
      aria-labelledby="overview-card-title"
      className={cn(
        "rounded-2xl border p-5 sm:p-6 bg-surface border-subtle transition-colors",
        className
      )}
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
        <div className="flex items-center gap-3">
          <div
            className={cn(
              "w-3.5 h-3.5 rounded-full shrink-0",
              isConnected
                ? "bg-risk-low"
                : isReady
                ? "bg-brand/80"
                : "bg-risk-caution"
            )}
            aria-hidden="true"
          />
          <div>
            <div className="flex items-center gap-2">
              <h3
                id="overview-card-title"
                className="text-base sm:text-lg font-semibold text-primary"
              >
                {overview.headline}
              </h3>
              {isFixture && (
                <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-brandSoft text-brand border border-brand/30 tracking-wider uppercase">
                  DEVELOPMENT FIXTURE
                </span>
              )}
            </div>
            <p className="text-xs text-muted">
              {overview.deviceSyncStatus}
            </p>
          </div>
        </div>

        <span
          className={cn(
            "inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold border self-start sm:self-auto",
            isConnected
              ? "bg-risk-low-soft text-risk-low border-risk-low"
              : isReady
              ? "bg-surfaceElevated text-secondary border-default"
              : "bg-risk-caution-soft text-risk-caution border-risk-caution"
          )}
        >
          {overview.state.replace(/_/g, " ")}
        </span>
      </div>

      <p className="text-sm text-secondary leading-relaxed mb-4">
        {overview.statusDescription}
      </p>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-3 border-t border-subtle text-xs">
        <div className="bg-surfaceElevated rounded-xl p-3">
          <div className="text-muted font-medium mb-1">Device Protection</div>
          <div className="text-sm font-semibold text-primary">
            {overview.isDeviceConnected
              ? `${overview.activeShieldsCount} of ${overview.totalShieldsCount} Shields Active`
              : "Not connected to phone"}
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3">
          <div className="text-muted font-medium mb-1">Companion Mode</div>
          <div className="text-sm font-semibold text-secondary">
            Standalone Web Companion
          </div>
        </div>
      </div>
    </section>
  );
};
