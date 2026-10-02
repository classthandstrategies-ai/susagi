"use client";

import React, { useEffect, useRef, useState } from "react";
import { GuardianContact } from "@/types/guardian";
import { GuardianManagementDialog } from "@/features/guardians/GuardianManagementDialog";
import { GuardianFormData } from "@/features/guardians/guardianTypes";

interface GuardianStepProps {
  guardians: GuardianContact[];
  isServiceAvailable: boolean;
  isFixtureMode: boolean;
  onAddFixtureGuardian?: (data: GuardianFormData) => void;
  onContinue: () => void;
  onSkip: () => void;
}

export const GuardianStep: React.FC<GuardianStepProps> = ({
  guardians,
  isServiceAvailable,
  isFixtureMode,
  onAddFixtureGuardian,
  onContinue,
  onSkip,
}) => {
  const h1Ref = useRef<HTMLHeadingElement>(null);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);

  useEffect(() => {
    h1Ref.current?.focus({ preventScroll: true });
  }, []);

  const hasGuardians = guardians.length > 0;

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Title & Mandatory Copy */}
      <div className="space-y-3">
        <h1
          ref={h1Ref}
          tabIndex={-1}
          className="text-2xl sm:text-3xl font-bold tracking-tight text-primary outline-none"
        >
          Choose someone you trust when something doesn&apos;t feel right.
        </h1>
        <p className="text-base text-secondary leading-relaxed">
          A Guardian can help confirm whether a sensitive request really came from the person it claims to be from.
        </p>
      </div>

      {/* Guardian Status / Current List */}
      <div className="space-y-4">
        {hasGuardians ? (
          <div className="rounded-2xl bg-surface border border-subtle p-5 space-y-3 shadow-xs">
            <div className="text-xs font-semibold uppercase tracking-wider text-muted">
              Current Guardian Circle
            </div>
            <div className="divide-y divide-subtle">
              {guardians.map((g) => (
                <div key={g.id} className="py-2.5 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-brandSoft text-brand flex items-center justify-center font-bold text-xs">
                      {g.avatarInitials}
                    </div>
                    <div>
                      <div className="text-sm font-semibold text-primary">{g.name}</div>
                      <div className="text-xs text-muted">{g.relationship}</div>
                    </div>
                  </div>
                  <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-risk-low-soft text-risk-low border border-risk-low">
                    READY
                  </span>
                </div>
              ))}
            </div>
          </div>
        ) : !isServiceAvailable && !isFixtureMode ? (
          /* Normal runtime unavailable notice */
          <div className="rounded-2xl bg-surface border border-subtle p-5 space-y-2 shadow-xs">
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-risk-caution" aria-hidden="true" />
              <h2 className="text-sm font-semibold text-primary">
                Guardian Circle isn&apos;t connected yet.
              </h2>
            </div>
            <p className="text-xs text-secondary leading-relaxed">
              You can add a trusted person when the Guardian service becomes available.
            </p>
          </div>
        ) : (
          /* Ready to add or empty state */
          <div className="rounded-2xl bg-surface border border-subtle p-5 space-y-2 shadow-xs">
            <h2 className="text-sm font-semibold text-primary">
              No Guardian configured yet
            </h2>
            <p className="text-xs text-secondary leading-relaxed">
              Add a family member or trusted friend who can help verify unusual demands or calls.
            </p>
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="pt-2 flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
        {hasGuardians ? (
          <button
            type="button"
            onClick={onContinue}
            className="min-h-[48px] px-8 py-3 rounded-xl bg-brand hover:bg-brandLight active:bg-brandDark text-white font-semibold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer shadow-sm text-center inline-flex items-center justify-center"
          >
            Continue setup
          </button>
        ) : (
          <>
            <button
              type="button"
              onClick={() => setIsAddDialogOpen(true)}
              className="min-h-[48px] px-6 py-3 rounded-xl bg-brand hover:bg-brandLight active:bg-brandDark text-white font-semibold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer shadow-sm text-center inline-flex items-center justify-center"
            >
              Add a trusted person
            </button>

            <button
              type="button"
              onClick={onSkip}
              className="min-h-[48px] px-5 py-3 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle hover:border-default text-secondary hover:text-primary font-medium text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer text-center inline-flex items-center justify-center"
            >
              I&apos;ll do this later
            </button>
          </>
        )}
      </div>

      {/* Reused Guardian Management Dialog */}
      {isAddDialogOpen && (
        <GuardianManagementDialog
          mode="ADD"
          isFixtureMode={isFixtureMode}
          onClose={() => setIsAddDialogOpen(false)}
          onSaveFixtureGuardian={(data) => {
            if (onAddFixtureGuardian) {
              onAddFixtureGuardian(data);
            }
            setIsAddDialogOpen(false);
          }}
        />
      )}
    </div>
  );
};
