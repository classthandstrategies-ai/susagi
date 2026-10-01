import React from "react";
import Link from "next/link";
import { cn } from "@/lib/utils";

interface PrimarySafetyActionProps {
  label: string;
  onClick?: () => void;
  href?: string;
  variant?: "brand" | "danger" | "neutral";
  icon?: React.ReactNode;
  disabled?: boolean;
  className?: string;
  type?: "button" | "submit" | "reset";
}

export const PrimarySafetyAction: React.FC<PrimarySafetyActionProps> = ({
  label,
  onClick,
  href,
  variant = "brand",
  icon,
  disabled = false,
  className,
  type = "button",
}) => {
  const variantStyles = {
    brand:
      "bg-brand text-white hover:bg-blue-600 active:bg-blue-700 shadow-sm focus-visible:ring-2 focus-visible:ring-brandLight focus-visible:ring-offset-2 focus-visible:ring-offset-void",
    danger:
      "bg-risk-critical text-white hover:bg-red-600 active:bg-red-700 shadow-sm focus-visible:ring-2 focus-visible:ring-red-400 focus-visible:ring-offset-2 focus-visible:ring-offset-void",
    neutral:
      "bg-surfaceElevated text-primary border border-default hover:bg-surfaceHighlight active:bg-surface focus-visible:ring-2 focus-visible:ring-brandLight focus-visible:ring-offset-2 focus-visible:ring-offset-void",
  };

  const baseStyles = cn(
    "inline-flex items-center justify-center gap-2.5 px-6 py-3 min-h-[48px] rounded-xl text-base font-semibold transition-colors duration-150 cursor-pointer select-none",
    disabled && "opacity-50 cursor-not-allowed pointer-events-none",
    variantStyles[variant],
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
