import React from "react";
import Link from "next/link";
import { GuardianContact } from "@/types/guardian";

interface GuardianListProps {
  guardians: GuardianContact[];
  isFixtureMode?: boolean;
  onEditGuardian: (guardian: GuardianContact) => void;
  onRemoveGuardian: (guardian: GuardianContact) => void;
}

export const GuardianList: React.FC<GuardianListProps> = ({
  guardians,
  isFixtureMode = false,
  onEditGuardian,
  onRemoveGuardian,
}) => {
  return (
    <div
      role="feed"
      aria-label="Trusted Guardian Contacts"
      className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
    >
      {guardians.map((guardian) => (
        <article
          key={guardian.id}
          className="rounded-2xl bg-surface border border-subtle hover:border-default p-5 flex flex-col justify-between gap-4 transition-colors"
        >
          {/* Contact Header */}
          <div className="flex items-start gap-3.5">
            <div
              className="w-12 h-12 rounded-full bg-brandSoft border border-brand/30 text-brand font-bold flex items-center justify-center text-sm shrink-0"
              aria-hidden="true"
            >
              {guardian.avatarInitials}
            </div>

            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <h3 className="text-base font-semibold text-primary truncate">
                  {guardian.name}
                </h3>
                {guardian.isPrimary && (
                  <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-brandSoft text-brand border border-brand/20">
                    Primary
                  </span>
                )}
              </div>
              <p className="text-xs text-muted truncate mt-0.5">{guardian.relationship}</p>
              <p className="text-xs text-secondary font-mono mt-1">
                {guardian.phone}
              </p>
            </div>
          </div>

          {/* Status and Action Buttons */}
          <div className="space-y-3 pt-3 border-t border-subtle">
            <div className="flex items-center justify-between text-xs">
              <span className="text-muted">
                {guardian.lastActive || "Status: " + guardian.status.replace(/_/g, " ")}
              </span>

              {/* Development-Only Verification Flow Preview (Step 27) */}
              {isFixtureMode && guardian.canVerifyIdentity && (
                <Link
                  href="/verification?fixture=ready"
                  className="min-h-[48px] px-3.5 inline-flex items-center justify-center rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-primary text-xs font-medium transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
                >
                  Preview Verification →
                </Link>
              )}
            </div>

            {/* Management Actions in Fixture Mode */}
            {isFixtureMode && (
              <div className="flex items-center gap-2 pt-1 border-t border-subtle/50">
                <button
                  type="button"
                  onClick={() => onEditGuardian(guardian)}
                  className="flex-1 min-h-[48px] inline-flex items-center justify-center px-3 py-2 rounded-xl text-xs font-semibold text-secondary hover:text-primary bg-surfaceElevated border border-subtle hover:border-default transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
                >
                  Edit
                </button>
                <button
                  type="button"
                  onClick={() => onRemoveGuardian(guardian)}
                  className="min-h-[48px] px-3 py-2 rounded-xl text-xs font-semibold text-risk-caution hover:text-risk-critical bg-surfaceElevated border border-subtle hover:border-risk-caution/50 transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
                  aria-label={`Remove ${guardian.name}`}
                >
                  Remove
                </button>
              </div>
            )}
          </div>
        </article>
      ))}
    </div>
  );
};
