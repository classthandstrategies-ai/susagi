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
        title: "Do not share credentials or OTPs",
        description: "Never disclose passwords, OTPs, or UPI PINs to anyone, even if they claim to be from a bank or authority.",
      };
    case "DO_NOT_SEND_MONEY":
      return {
        title: "Do not transfer money",
        description: "Refuse any request to send money to safe, temporary, or verification accounts.",
      };
    case "USE_OFFICIAL_CHANNEL":
      return {
        title: "Use official channels independently",
        description: "Call the company or bank back using the phone number printed on your physical card or official website.",
      };
    case "END_CALL":
      return {
        title: "End communication immediately",
        description: "Hang up or block messages whenever high pressure, legal threats, or urgent demands are made.",
      };
    case "DO_NOT_INSTALL_REMOTE_ACCESS":
      return {
        title: "Do not install screen sharing apps",
        description: "Refuse any instruction to install remote support tools like AnyDesk, TeamViewer, or screen-sharing utilities.",
      };
    case "VERIFY_IDENTITY":
      return {
        title: "Verify with a trusted family member",
        description: "Ask someone in your Guardian Circle to confirm the situation before taking any further action.",
      };
    case "CONTINUE_MONITORING":
    default:
      return {
        title: "Stay vigilant",
        description: "No immediate ongoing threat. Keep regular background protection enabled.",
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
Risk level: ${incident.riskLevel}
Timestamp: ${incident.timestamp}
Claimed identity: ${incident.claimedIdentity || "Unspecified"}
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
        // User cancelled
      }
    } else {
      try {
        await navigator.clipboard.writeText(window.location.href);
        setCopyFeedback("Link copied to clipboard");
        setTimeout(() => setCopyFeedback(null), 3000);
      } catch {
        setCopyFeedback("Could not copy link");
        setTimeout(() => setCopyFeedback(null), 3000);
      }
    }
  };

  return (
    <section aria-labelledby="actions-heading" className="space-y-4">
      <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-4 shadow-sm">
        <h2
          id="actions-heading"
          className="text-xs font-medium text-secondary"
        >
          What to do next
        </h2>

        <div className="space-y-3">
          {actionsToDisplay.map((action) => {
            const guidance = getActionGuidance(action);
            return (
              <div
                key={action}
                className="rounded-xl bg-surfaceElevated p-4 border border-subtle flex items-start gap-3 text-xs"
              >
                <span className="w-5 h-5 rounded-full bg-brandSoft text-brand text-xs font-bold flex items-center justify-center shrink-0 mt-0.5">
                  ✓
                </span>
                <div className="space-y-1">
                  <div className="font-semibold text-primary">
                    {guidance.title}
                  </div>
                  <p className="text-secondary leading-relaxed">
                    {guidance.description}
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Share & Copy Actions */}
        <div className="flex flex-col sm:flex-row gap-2.5 pt-2 border-t border-subtle">
          <button
            type="button"
            onClick={handleCopySummary}
            className="flex-1 min-h-[44px] px-4 py-2.5 rounded-xl bg-surface hover:bg-surfaceElevated border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-1.5"
          >
            <svg className="w-4 h-4 text-secondary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
            </svg>
            <span>{copyFeedback || "Copy incident summary"}</span>
          </button>

          <button
            type="button"
            onClick={handleShare}
            className="min-h-[44px] px-4 py-2.5 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-1.5"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
            </svg>
            <span>Share</span>
          </button>
        </div>
      </div>
    </section>
  );
};
