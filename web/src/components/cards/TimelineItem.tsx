import React from "react";
import { IncidentTimelineItem } from "@/types/activity";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { cn } from "@/lib/utils";

interface TimelineItemProps {
  item: IncidentTimelineItem;
  isLast?: boolean;
}

export const TimelineItem: React.FC<TimelineItemProps> = ({
  item,
  isLast = false,
}) => {
  return (
    <div className="flex gap-4 relative">
      {/* Vertical line connecting nodes */}
      {!isLast && (
        <div
          className="absolute left-[15px] top-7 bottom-0 w-[2px] bg-borderSubtle"
          aria-hidden="true"
        />
      )}

      {/* Circle indicator */}
      <div className="relative z-10 w-8 h-8 rounded-full bg-surfaceElevated border border-default flex items-center justify-center shrink-0 mt-0.5">
        <span
          className={cn(
            "w-2.5 h-2.5 rounded-full",
            item.riskLevel === "LOW" && "bg-risk-low",
            item.riskLevel === "CAUTION" && "bg-risk-caution",
            item.riskLevel === "HIGH" && "bg-risk-high",
            item.riskLevel === "CRITICAL" && "bg-risk-critical"
          )}
          aria-hidden="true"
        />
      </div>

      {/* Content */}
      <div className="flex-1 pb-6">
        <div className="flex flex-wrap items-center justify-between gap-2 mb-1">
          <h4 className="text-sm font-semibold text-primary">{item.title}</h4>
          <div className="flex items-center gap-2">
            <RiskBadge level={item.riskLevel} size="sm" />
            <span className="text-xs text-muted font-mono">{item.timestamp}</span>
          </div>
        </div>
        <p className="text-sm text-secondary leading-relaxed">{item.detail}</p>
      </div>
    </div>
  );
};
