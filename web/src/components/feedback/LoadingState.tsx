import React from "react";
import { cn } from "@/lib/utils";

interface LoadingStateProps {
  label?: string;
  className?: string;
}

export const LoadingState: React.FC<LoadingStateProps> = ({
  label = "Loading security status...",
  className,
}) => {
  return (
    <div
      role="status"
      aria-live="polite"
      className={cn(
        "flex flex-col items-center justify-center p-8 space-y-3",
        className
      )}
    >
      <div className="w-8 h-8 rounded-full border-2 border-brand/20 border-t-brand animate-spin" />
      <span className="text-xs text-muted font-medium">{label}</span>
    </div>
  );
};
