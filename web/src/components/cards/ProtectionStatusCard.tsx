import React from "react";
import { ProtectionStatusSummary } from "@/types/protection";
import { cn } from "@/lib/utils";

interface ProtectionStatusCardProps {
  summary: ProtectionStatusSummary;
  className?: string;
}

export const ProtectionStatusCard: React.FC<ProtectionStatusCardProps> = ({
  summary,
  className,
}) => {
  const isHealthy = summary.state === "ACTIVE";

  return (
    <div
      className={cn(
        "rounded-2xl border p-5 sm:p-6 bg-surface border-subtle",
        className
      )}
    >
      <div className="flex items-center justify-between gap-4 mb-4">
        <div className="flex items-center gap-3">
          <div
            className={cn(
              "w-3 h-3 rounded-full shrink-0",
              isHealthy ? "bg-risk-low animate-pulse" : "bg-risk-caution"
            )}
            aria-hidden="true"
          />
          <div>
            <h3 className="text-base sm:text-lg font-semibold text-primary">
              Companion Defense Shield
            </h3>
            <p className="text-xs text-muted">
              {isHealthy ? "Local Intelligence Active" : "Attention Required"}
            </p>
          </div>
        </div>

        <span
          className={cn(
            "text-xs px-2.5 py-1 rounded-full font-semibold border",
            isHealthy
              ? "bg-risk-low-soft text-risk-low border-risk-low"
              : "bg-risk-caution-soft text-risk-caution border-risk-caution"
          )}
        >
          {summary.state}
        </span>
      </div>

      <div className="grid grid-cols-2 gap-3 pt-3 border-t border-subtle">
        <div className="bg-surfaceElevated rounded-xl p-3">
          <div className="text-xs text-muted font-medium mb-1">
            Active Defenses
          </div>
          <div className="text-lg font-bold text-primary">
            {summary.activeShieldsCount} / {summary.totalShieldsCount}
          </div>
        </div>

        <div className="bg-surfaceElevated rounded-xl p-3">
          <div className="text-xs text-muted font-medium mb-1">Mode</div>
          <div className="text-sm font-semibold text-secondary">
            {summary.companionMode === "LOCAL_STANDBY"
              ? "Local Standby"
              : summary.companionMode}
          </div>
        </div>
      </div>
    </div>
  );
};
