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

  // Filter out actions that are directives/rules
  const guidanceDirectives = allActions.filter(
    (act) => act !== "END_CALL" && act !== "VERIFY_IDENTITY"
  );

  return (
    <section aria-labelledby="actions-panel-title" className="space-y-4">
      <div className="flex items-center justify-between">
        <h3
          id="actions-panel-title"
          className="text-xs font-semibold uppercase tracking-wider text-muted"
        >
          Protective Directives & Controls
        </h3>
        <span className="text-[11px] font-mono text-muted">Safety Protocol</span>
      </div>

      {/* Urgent Interactive Actions / Telephony Guidance */}
      <div className="rounded-2xl bg-surface border border-subtle p-5 space-y-4">
        {/* End Call Directive */}
        {isEndCall && (
          <div className="rounded-xl border border-risk-critical/40 bg-risk-critical-soft/30 p-4 space-y-3">
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-full bg-risk-critical text-white flex items-center justify-center shrink-0 font-bold">
                !
              </div>
              <div className="space-y-0.5">
                <div className="text-sm font-bold text-primary">
                  End the call on your phone
                </div>
                <p className="text-xs text-secondary leading-relaxed">
                  Disconnect immediately. Legitimate organizations never demand urgent money transfers or OTPs over the phone.
                </p>
              </div>
            </div>

            {/* Truthful disabled browser control */}
            <div className="pt-2 border-t border-risk-critical/20 flex flex-col gap-1">
              <button
                type="button"
                disabled
                aria-disabled="true"
                className="w-full py-2.5 px-4 rounded-xl bg-risk-critical/40 text-white/70 font-semibold text-xs border border-risk-critical/30 cursor-not-allowed select-none text-center"
              >
                End Call on Device
              </button>
              <span className="text-[10px] text-muted text-center font-mono">
                Device control is not connected to browser
              </span>
            </div>
          </div>
        )}

        {/* Verify Identity Action */}
        {isVerify && (
          <div className="rounded-xl border border-default bg-surfaceElevated p-4 space-y-3">
            <div className="space-y-1">
              <div className="text-sm font-semibold text-primary">
                Verify Caller Identity
              </div>
              <p className="text-xs text-secondary leading-relaxed">
                Initiate a cryptographic or family challenge to authenticate who is calling.
              </p>
            </div>

            <button
              type="button"
              onClick={onOpenVerifyDialog}
              className="w-full py-2.5 px-4 rounded-xl bg-brand hover:bg-blue-600 text-white font-semibold text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
            >
              Verify Identity {isFixtureMode ? "(Preview)" : ""}
            </button>
          </div>
        )}

        {/* Guidance Rules (e.g., Do Not Share Credentials, Do Not Send Money, etc.) */}
        {guidanceDirectives.length > 0 && (
          <div className="space-y-2 pt-2 border-t border-subtle">
            <div className="text-[10px] font-semibold uppercase tracking-wider text-muted">
              Mandatory Safety Rules
            </div>

            <div className="space-y-2">
              {guidanceDirectives.map((directive) => {
                const isCriticalRule =
                  directive === "DO_NOT_SHARE_CREDENTIALS" ||
                  directive === "DO_NOT_SEND_MONEY" ||
                  directive === "DO_NOT_INSTALL_REMOTE_ACCESS";

                return (
                  <div
                    key={directive}
                    className={`rounded-xl p-3 border flex items-start gap-2.5 text-xs ${
                      isCriticalRule
                        ? "bg-risk-high-soft/30 border-risk-high/40 text-primary"
                        : "bg-surfaceElevated border-subtle text-secondary"
                    }`}
                  >
                    <svg
                      className="w-4 h-4 text-risk-caution shrink-0 mt-0.5"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      strokeWidth={2}
                      aria-hidden="true"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        d="M12 9v3.75m0-10.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.002A11.959 11.959 0 0112 2.714zm0 13.036h.008v.008H12v-.008z"
                      />
                    </svg>
                    <div>
                      <div className="font-semibold text-primary">
                        {formatProtectiveAction(directive)}
                      </div>
                      <p className="text-[11px] text-secondary mt-0.5">
                        {directive === "DO_NOT_SHARE_CREDENTIALS" &&
                          "Never disclose OTPs, net-banking passwords, or UPI PINs to any caller."}
                        {directive === "DO_NOT_SEND_MONEY" &&
                          "Refuse any request to move funds to a temporary, security, or holding account."}
                        {directive === "DO_NOT_INSTALL_REMOTE_ACCESS" &&
                          "Do not download screen-sharing or remote desktop utilities (AnyDesk, TeamViewer)."}
                        {directive === "USE_OFFICIAL_CHANNEL" &&
                          "End call and contact the official helpline printed on your debit card or website."}
                        {directive === "CONTINUE_MONITORING" &&
                          "Normal communication patterns detected. Companion will continue background acoustic screening."}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </section>
  );
};
