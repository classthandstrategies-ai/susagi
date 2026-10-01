import React from "react";
import { cn } from "@/lib/utils";

interface PermissionCardProps {
  title: string;
  description: string;
  status: "GRANTED" | "NOT_CONFIGURED" | "OPTIONAL" | "NOT_SUPPORTED";
  className?: string;
}

export const PermissionCard: React.FC<PermissionCardProps> = ({
  title,
  description,
  status,
  className,
}) => {
  const isGranted = status === "GRANTED";

  return (
    <div
      className={cn(
        "rounded-xl bg-surface border border-subtle p-4 flex items-start justify-between gap-4",
        className
      )}
    >
      <div className="space-y-1">
        <h4 className="text-sm font-semibold text-primary">{title}</h4>
        <p className="text-xs text-secondary leading-relaxed">{description}</p>
      </div>

      <span
        className={cn(
          "text-xs px-2.5 py-1 rounded-md font-medium shrink-0 border",
          isGranted
            ? "bg-risk-low-soft text-risk-low border-risk-low"
            : "bg-surfaceElevated text-muted border-subtle"
        )}
      >
        {status.replace(/_/g, " ")}
      </span>
    </div>
  );
};
