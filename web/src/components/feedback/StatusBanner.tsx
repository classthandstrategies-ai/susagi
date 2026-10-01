import React from "react";
import { cn } from "@/lib/utils";

interface StatusBannerProps {
  title: string;
  message: string;
  variant?: "info" | "warning" | "danger" | "success";
  className?: string;
}

export const StatusBanner: React.FC<StatusBannerProps> = ({
  title,
  message,
  variant = "info",
  className,
}) => {
  const styles = {
    info: "bg-surfaceElevated border-border text-primary",
    warning: "bg-risk-caution-soft border-risk-caution text-primary",
    danger: "bg-risk-critical-soft border-risk-critical text-primary",
    success: "bg-risk-low-soft border-risk-low text-primary",
  };

  return (
    <div
      role="alert"
      className={cn(
        "rounded-xl border p-4 flex items-start gap-3",
        styles[variant],
        className
      )}
    >
      <div className="flex-1">
        <h4 className="text-sm font-semibold">{title}</h4>
        <p className="text-xs text-secondary mt-0.5 leading-relaxed">{message}</p>
      </div>
    </div>
  );
};
