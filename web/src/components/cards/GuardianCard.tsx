import React from "react";
import { GuardianContact } from "@/types/guardian";
import { cn } from "@/lib/utils";

interface GuardianCardProps {
  guardian: GuardianContact;
  onChallenge?: (guardian: GuardianContact) => void;
  className?: string;
}

export const GuardianCard: React.FC<GuardianCardProps> = ({
  guardian,
  onChallenge,
  className,
}) => {
  return (
    <div
      className={cn(
        "rounded-xl bg-surface border border-subtle p-5 flex flex-col justify-between gap-4",
        className
      )}
    >
      <div className="flex items-start gap-3.5">
        <div
          className="w-11 h-11 rounded-full bg-brandSoft border border-brand/30 text-brand font-bold flex items-center justify-center text-sm shrink-0"
          aria-hidden="true"
        >
          {guardian.avatarInitials}
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <h4 className="text-base font-semibold text-primary truncate">
              {guardian.name}
            </h4>
            {guardian.isPrimary && (
              <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-brandSoft text-brand border border-brand/20">
                Primary
              </span>
            )}
          </div>
          <p className="text-xs text-muted truncate">{guardian.relationship}</p>
          <p className="text-xs text-secondary font-mono mt-1">
            {guardian.phone}
          </p>
        </div>
      </div>

      <div className="flex items-center justify-between pt-3 border-t border-subtle text-xs">
        <span className="text-muted">
          {guardian.lastActive || "Status: " + guardian.status}
        </span>

        {guardian.canVerifyIdentity && onChallenge && (
          <button
            type="button"
            onClick={() => onChallenge(guardian)}
            className="min-h-[48px] px-3.5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-primary text-xs font-semibold transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
          >
            Preview Verification
          </button>
        )}
      </div>
    </div>
  );
};
