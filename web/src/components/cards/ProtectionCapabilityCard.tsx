import React from "react";
import { ProtectionCapability } from "@/types/protection";
import { cn } from "@/lib/utils";

interface ProtectionCapabilityCardProps {
  capability: ProtectionCapability;
  className?: string;
}

export const ProtectionCapabilityCard: React.FC<ProtectionCapabilityCardProps> = ({
  capability,
  className,
}) => {
  const isAvailable = capability.status === "ACTIVE";

  return (
    <div
      className={cn(
        "rounded-xl bg-surface border border-subtle p-5 hover:border-default transition-colors",
        className
      )}
    >
      <div className="flex items-start justify-between gap-3 mb-2">
        <h4 className="text-base font-semibold text-primary">
          {capability.name}
        </h4>
        <span
          className={cn(
            "text-xs px-2 py-0.5 rounded font-medium border",
            isAvailable
              ? "bg-risk-low-soft text-risk-low border-risk-low"
              : "bg-surfaceHighlight text-muted border-subtle"
          )}
        >
          {capability.status}
        </span>
      </div>

      <p className="text-sm text-secondary leading-relaxed mb-3">
        {capability.description}
      </p>

      <div className="text-xs text-muted flex items-center gap-1.5 pt-2 border-t border-subtle">
        <span
          className={cn(
            "w-2 h-2 rounded-full",
            capability.supportedOnWeb ? "bg-brand" : "bg-muted"
          )}
          aria-hidden="true"
        />
        <span>
          {capability.supportedOnWeb
            ? "Available on Web Companion"
            : "Requires Paired Android Device"}
        </span>
      </div>
    </div>
  );
};
