"use client";

import React from "react";
import Link from "next/link";
import { cn } from "@/lib/utils";

interface SecondarySafetyActionProps {
  label: string;
  onClick?: () => void;
  href?: string;
  icon?: React.ReactNode;
  disabled?: boolean;
  className?: string;
  type?: "button" | "submit" | "reset";
}

export const SecondarySafetyAction: React.FC<SecondarySafetyActionProps> = ({
  label,
  onClick,
  href,
  icon,
  disabled = false,
  className,
  type = "button",
}) => {
  const baseStyles = cn(
    "inline-flex items-center justify-center gap-2 px-4 py-2.5 min-h-[44px] rounded-xl text-xs font-medium text-secondary hover:text-primary bg-surface hover:bg-surfaceElevated border border-subtle hover:border-default active:bg-surfaceHighlight transition-colors duration-150 cursor-pointer select-none focus-visible:ring-2 focus-visible:ring-brandLight focus-visible:ring-offset-2 focus-visible:ring-offset-surface",
    disabled && "opacity-50 cursor-not-allowed pointer-events-none",
    className
  );

  if (href && !disabled) {
    return (
      <Link href={href} className={baseStyles}>
        {icon && <span aria-hidden="true">{icon}</span>}
        <span>{label}</span>
      </Link>
    );
  }

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={baseStyles}
    >
      {icon && <span aria-hidden="true">{icon}</span>}
      <span>{label}</span>
    </button>
  );
};
