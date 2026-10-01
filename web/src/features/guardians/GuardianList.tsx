"use client";

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
      className="grid grid-cols-1 md:grid-cols-2 gap-4"
    >
      {guardians.map((guardian) => (
        <article
          key={guardian.id}
          className="rounded-2xl bg-surface border border-subtle hover:border-default p-5 flex flex-col justify-between gap-4 transition-colors shadow-sm"
        >
          {/* People-first Header: Avatar + Name + Relationship */}
          <div className="flex items-start gap-3.5">
            <div
              className="w-12 h-12 rounded-full bg-surfaceElevated border border-subtle text-primary font-semibold flex items-center justify-center text-sm shrink-0"
              aria-hidden="true"
            >
              {guardian.avatarInitials}
            </div>

            <div className="flex-1 min-w-0 space-y-1">
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="text-base font-semibold text-primary truncate">
                  {guardian.name}
                </h3>
                {guardian.relationship && (
                  <span className="text-[11px] px-2 py-0.5 rounded-full bg-surfaceElevated text-secondary border border-subtle">
                    {guardian.relationship}
                  </span>
                )}
                {guardian.isPrimary && (
                  <span className="text-[11px] font-medium px-2 py-0.5 rounded-full bg-brandSoft text-brand border border-brand/20">
                    Primary Guardian
                  </span>
                )}
              </div>

              <p className="text-xs text-secondary font-mono">
                {guardian.phone}
              </p>
            </div>
          </div>

          {/* Capabilities & Status Details */}
          <div className="space-y-3 pt-3 border-t border-subtle text-xs">
            <div className="flex items-center justify-between text-secondary">
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-risk-low" aria-hidden="true" />
                <span>
                  {guardian.status === "ACTIVE"
                    ? "Active verifier"
                    : guardian.status.replace(/_/g, " ")}
                </span>
              </div>

              {/* Verification Preview Link in Fixture Mode */}
              {isFixtureMode && guardian.canVerifyIdentity && (
                <Link
                  href="/verification?fixture=ready"
                  className="min-h-[36px] px-2.5 inline-flex items-center justify-center rounded-lg text-brand hover:text-brandLight text-xs font-medium transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer gap-1"
                >
                  <span>Test verify</span>
                  <span aria-hidden="true">›</span>
                </Link>
              )}
            </div>

            {/* Management Actions in Fixture Mode */}
            {isFixtureMode && (
              <div className="flex items-center gap-2 pt-2 border-t border-subtle">
                <button
                  type="button"
                  onClick={() => onEditGuardian(guardian)}
                  className="flex-1 min-h-[40px] inline-flex items-center justify-center px-3 py-1.5 rounded-xl text-xs font-medium text-secondary hover:text-primary bg-surface hover:bg-surfaceElevated border border-subtle transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
                >
                  Edit details
                </button>
                <button
                  type="button"
                  onClick={() => onRemoveGuardian(guardian)}
                  className="min-h-[40px] px-3 py-1.5 rounded-xl text-xs font-medium text-muted hover:text-risk-critical hover:bg-surfaceElevated border border-subtle transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
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
