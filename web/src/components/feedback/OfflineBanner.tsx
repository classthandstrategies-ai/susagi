import React from "react";
import { cn } from "@/lib/utils";

interface OfflineBannerProps {
  message?: string;
  className?: string;
}

export const OfflineBanner: React.FC<OfflineBannerProps> = ({
  message = "SuSagi web companion is operating in local standby mode. Pair with Android phone for live call intercept.",
  className,
}) => {
  return (
    <div
      role="status"
      className={cn(
        "rounded-xl bg-surfaceElevated border border-subtle p-3.5 flex items-center gap-3 text-xs text-secondary",
        className
      )}
    >
      <div
        className="w-2.5 h-2.5 rounded-full bg-brand/80 shrink-0"
        aria-hidden="true"
      />
      <div className="flex-1">
        <span className="font-semibold text-primary">Companion Standby: </span>
        <span>{message}</span>
      </div>
    </div>
  );
};
