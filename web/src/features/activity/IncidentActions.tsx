"use client";

import React, { useState } from "react";
import { ProtectiveAction } from "@/types/risk";
import { IncidentDetail } from "@/types/activity";

interface IncidentActionsProps {
  incident: IncidentDetail;
}

const getActionGuidance = (action: ProtectiveAction): { title: string; description: string } => {
  switch (action) {
    case "DO_NOT_SHARE_CREDENTIALS":
      return {
        title: "Do Not Share Credentials",
        description: "Never disclose OTPs, banking passwords, PIN numbers, or security questions over unverified communications.",
      };
    case "DO_NOT_SEND_MONEY":
      return {
        title: "Do Not Send Money",
        description: "Do not execute UPI transfers, bank deposits, or gift card purchases requested urgently.",
      };
    case "USE_OFFICIAL_CHANNEL":
      return {
        title: "Use Official Channel",
        description: "Contact the institution directly using phone numbers or portal links published on their official verified domain.",
      };
    case "END_CALL":
      return {
        title: "Terminate Communication",
        description: "Hang up or cease text communication immediately if high-pressure tactics or threat demands are made.",
      };
    case "DO_NOT_INSTALL_REMOTE_ACCESS":
      return {
        title: "Do Not Install Remote Access",
        description: "Refuse requests to install third-party support applications such as AnyDesk, TeamViewer, or screen-sharing utilities.",
      };
    case "VERIFY_IDENTITY":
      return {
        title: "Verify Identity",
        description: "Confirm the caller or sender's identity through trusted mutual contacts, a secret family passphrase, or an out-of-band channel.",
      };
    case "CONTINUE_MONITORING":
    default:
      return {
        title: "Continue Monitoring",
        description: "No immediate threat detected. Remain vigilant for any subsequent unexpected requests.",
      };
  }
};

export const IncidentActions: React.FC<IncidentActionsProps> = ({ incident }) => {
  const [copyFeedback, setCopyFeedback] = useState<string | null>(null);

  const actionsToDisplay: ProtectiveAction[] = [
    incident.recommendedAction,
    ...(incident.additionalActions || []),
  ].filter((act, idx, arr) => arr.indexOf(act) === idx);

  const handleCopySummary = async () => {
    const textToCopy = `[SuSagi Incident Record]
Title: ${incident.title}
Risk Level: ${incident.riskLevel}
Timestamp: ${incident.timestamp}
Claimed Identity: ${incident.claimedIdentity || "Unspecified"}
Source: ${incident.source}
Summary: ${incident.summary}
${incident.outcome ? `Outcome: ${incident.outcome}` : ""}`.trim();

    try {
      if (typeof navigator !== "undefined" && navigator.clipboard) {
        await navigator.clipboard.writeText(textToCopy);
        setCopyFeedback("Summary copied to clipboard");
        setTimeout(() => setCopyFeedback(null), 3000);
      }
    } catch {
      setCopyFeedback("Failed to copy automatically");
      setTimeout(() => setCopyFeedback(null), 3000);
    }
  };

  const handleShare = async () => {
    if (typeof navigator !== "undefined" && navigator.share) {
      try {
        await navigator.share({
          title: `SuSagi Incident: ${incident.title}`,
          text: incident.summary,
          url: window.location.href,
        });
      } catch {
        // User cancelled or share dismissed
      }
    } else {
      // Fallback: Copy current URL
      try {
        await navigator.clipboard.writeText(window.location.href);
        setCopyFeedback("Incident link copied to clipboard");
        setTimeout(() => setCopyFeedback(null), 3000);
      } catch {
        setCopyFeedback("Could not copy link");
        setTimeout(() => setCopyFeedback(null), 3000);
      }
    }
  };

  const handlePrint = () => {
    if (typeof window !== "undefined") {
      window.print();
    }
  };

  return (
    <div className="space-y-6">
      {/* Recommended Follow-up Section */}
      <section
        aria-labelledby="followup-heading"
        className="rounded-2xl bg-surface border border-subtle p-5 space-y-4"
      >
        <div className="flex items-center justify-between">
          <h2
            id="followup-heading"
            className="text-xs font-semibold uppercase tracking-wider text-muted"
          >
            Recommended Follow-Up Directives
          </h2>
          <span className="text-[11px] font-mono text-muted">
            Safety Guidance
          </span>
        </div>

        <div className="space-y-3">
          {actionsToDisplay.map((action) => {
            const guidance = getActionGuidance(action);
            return (
              <div
                key={action}
                className="rounded-xl bg-surfaceElevated p-3.5 border border-subtle space-y-1"
              >
                <div className="flex items-center gap-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-brand" aria-hidden="true" />
                  <h3 className="text-xs font-semibold text-primary">
                    {guidance.title}
                  </h3>
                </div>
                <p className="text-xs text-secondary leading-relaxed pl-3.5">
                  {guidance.description}
                </p>
              </div>
            );
          })}
        </div>
      </section>

      {/* Incident Actions Section */}
      <section
        aria-labelledby="actions-heading"
        className="rounded-2xl bg-surface border border-subtle p-5 space-y-4"
      >
        <h2
          id="actions-heading"
          className="text-xs font-semibold uppercase tracking-wider text-muted"
        >
          Incident Actions
        </h2>

        {copyFeedback && (
          <div
            role="status"
            className="px-3 py-2 rounded-lg bg-brandSoft border border-brand/40 text-xs font-medium text-brand text-center"
          >
            {copyFeedback}
          </div>
        )}

        <div className="flex flex-col gap-2.5">
          {/* Copy Summary Button */}
          <button
            type="button"
            onClick={handleCopySummary}
            className="w-full min-h-[48px] flex items-center justify-center gap-2 px-4 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
          >
            <svg className="w-4 h-4 text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.666 3.888A2.25 2.25 0 0013.5 2.25h-3c-1.03 0-1.9.693-2.166 1.638m7.332 0c.055.194.084.4.084.612v0a.75.75 0 01-.75.75H9a.75.75 0 01-.75-.75v0c0-.212.03-.418.084-.612m7.332 0c.646.049 1.288.11 1.927.184 1.1.128 1.907 1.077 1.907 2.185V19.5a2.25 2.25 0 01-2.25 2.25H6.75A2.25 2.25 0 014.5 19.5V6.257c0-1.108.806-2.057 1.907-2.185a48.208 48.208 0 011.927-.184" />
            </svg>
            <span>Copy Incident Summary</span>
          </button>

          {/* Share Button */}
          <button
            type="button"
            onClick={handleShare}
            className="w-full min-h-[48px] flex items-center justify-center gap-2 px-4 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
          >
            <svg className="w-4 h-4 text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M7.217 10.907a2.25 2.25 0 100 2.186m0-2.186c.18.324.283.696.283 1.093s-.103.77-.283 1.093m0-2.186l9.566-5.314m-9.566 7.5l9.566 5.314m0 0a2.25 2.25 0 103.935 2.186 2.25 2.25 0 00-3.935-2.186zm0-12.814a2.25 2.25 0 103.933-2.185 2.25 2.25 0 00-3.933 2.185z" />
            </svg>
            <span>Share Incident</span>
          </button>

          {/* Official Cybercrime Portal Link (External) */}
          <a
            href="https://cybercrime.gov.in"
            target="_blank"
            rel="noopener noreferrer"
            className="w-full min-h-[48px] flex items-center justify-center gap-2 px-4 py-3 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-brand transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center"
          >
            <span>Open cybercrime portal</span>
            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 6H5.25A2.25 2.25 0 003 8.25v10.5A2.25 2.25 0 005.25 21h10.5A2.25 2.25 0 0018 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
            </svg>
          </a>

          {/* Print Record Button */}
          <button
            type="button"
            onClick={handlePrint}
            className="w-full min-h-[48px] flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl text-xs font-medium text-muted hover:text-secondary transition-colors cursor-pointer"
          >
            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6.72 13.829c-.24-1.076-.673-2.13-1.28-3.16M6.72 13.829l3.18-3.18m-3.18 3.18l-3.18 3.18m13.829-6.72c1.076-.24 2.13-.673 3.16-1.28m-3.16 1.28l3.18-3.18m-3.18 3.18l-3.18 3.18" />
            </svg>
            <span>Print Ledger Record</span>
          </button>
        </div>

        <p className="text-[11px] text-muted leading-relaxed pt-2 border-t border-subtle">
          * SuSagi provides protective awareness. Official cybercrime reports are submitted directly by citizens to the national portal at cybercrime.gov.in.
        </p>
      </section>
    </div>
  );
};
