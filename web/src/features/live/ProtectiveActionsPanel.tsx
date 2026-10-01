"use client";

import React from "react";
import { ProtectiveAction } from "@/types/risk";
import { formatProtectiveAction } from "@/lib/utils";

interface ProtectiveActionsPanelProps {
  primaryAction: ProtectiveAction;
  additionalActions?: ProtectiveAction[];
  onOpenVerifyDialog: () => void;
  isFixtureMode?: boolean;
}

export const ProtectiveActionsPanel: React.FC<ProtectiveActionsPanelProps> = ({
  primaryAction,
  additionalActions = [],
  onOpenVerifyDialog,
  isFixtureMode = false,
}) => {
  const allActions = Array.from(new Set([primaryAction, ...additionalActions]));

  const isEndCall = allActions.includes("END_CALL");
  const isVerify = allActions.includes("VERIFY_IDENTITY");

  const guidanceDirectives = allActions.filter(
    (act) => act !== "END_CALL" && act !== "VERIFY_IDENTITY"
  );

  return (
    <div className="space-y-4">
      {/* 4. IMMEDIATE ACTIONS */}
      <section aria-labelledby="immediate-actions-heading" className="space-y-3">
        <h3
          id="immediate-actions-heading"
          className="text-xs font-medium text-secondary"
        >
          Immediate actions
        </h3>

        <div className="space-y-3">
          {/* End Call Guidance Card */}
          {isEndCall && (
            <div className="rounded-2xl border border-risk-high/30 bg-risk-high-soft/30 p-5 space-y-3 shadow-sm">
              <div className="flex items-start gap-3.5">
                <div className="w-9 h-9 rounded-xl bg-risk-high text-white flex items-center justify-center shrink-0">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M16 8l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M5 3a2 2 0 00-2 2v1c0 8.284 6.716 15 15 15h1a2 2 0 002-2v-3.28a1 1 0 00-.684-.948l-4.493-1.498a1 1 0 00-1.21.502l-1.13 2.257a11.042 11.042 0 01-5.516-5.517l2.257-1.128a1 1 0 00.502-1.21L9.228 3.683A1 1 0 008.279 3H5z" />
                  </svg>
                </div>
                <div className="space-y-1">
                  <div className="text-base font-semibold text-primary">
                    End the call on your phone
                  </div>
                  <p className="text-xs text-secondary leading-relaxed">
                    Use the red button on your phone handset to hang up. Browsers cannot hang up phone calls.
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* Verify Identity Action */}
          {isVerify && (
            <div className="rounded-2xl border border-subtle bg-surface p-5 space-y-3 shadow-sm">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-0.5">
                  <div className="text-sm font-semibold text-primary">
                    Verify caller identity
                  </div>
                  <p className="text-xs text-secondary leading-relaxed">
                    Send a quick verification request to check if this caller is who they say they are.
                  </p>
                </div>
                <button
                  type="button"
                  onClick={onOpenVerifyDialog}
                  className="min-h-[44px] px-4 py-2.5 rounded-xl bg-brand hover:bg-brandLight text-white font-medium text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer shrink-0 inline-flex items-center justify-center"
                >
                  Verify identity {isFixtureMode ? "(Preview)" : ""}
                </button>
              </div>
            </div>
          )}
        </div>
      </section>

      {/* 6. SAFETY GUIDANCE */}
      {guidanceDirectives.length > 0 && (
        <section aria-labelledby="guidance-heading" className="space-y-3 pt-2">
          <h3
            id="guidance-heading"
            className="text-xs font-medium text-secondary"
          >
            Safety guidance for this call
          </h3>

          <div className="rounded-2xl bg-surface border border-subtle p-5 divide-y divide-subtle shadow-sm">
            {guidanceDirectives.map((directive) => (
              <div key={directive} className="py-3 first:pt-0 last:pb-0 flex items-start gap-3 text-xs">
                <span className="w-5 h-5 rounded-full bg-surfaceElevated border border-subtle flex items-center justify-center shrink-0 text-brand text-xs font-bold mt-0.5">
                  ✓
                </span>
                <div className="space-y-0.5">
                  <div className="font-semibold text-primary">
                    {formatProtectiveAction(directive)}
                  </div>
                  <p className="text-secondary leading-relaxed">
                    {directive === "DO_NOT_SHARE_CREDENTIALS" &&
                      "Never disclose OTPs, banking passwords, or UPI PINs to anyone over the phone."}
                    {directive === "DO_NOT_SEND_MONEY" &&
                      "Refuse any request to transfer money to a temporary, safe, or verification account."}
                    {directive === "DO_NOT_INSTALL_REMOTE_ACCESS" &&
                      "Do not install screen sharing apps (AnyDesk, TeamViewer) requested by the caller."}
                    {directive === "USE_OFFICIAL_CHANNEL" &&
                      "Hang up and contact the official helpline printed on your card or the official website."}
                    {directive === "CONTINUE_MONITORING" &&
                      "Communication pattern appears normal. SuSagi continues to monitor in the background."}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
};
