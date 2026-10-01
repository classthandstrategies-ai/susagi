"use client";

import React from "react";
import Link from "next/link";
import { Modal } from "@/components/dialogs/Modal";
import { SecondarySafetyAction } from "@/components/actions/SecondarySafetyAction";

interface VerificationUnavailableDialogProps {
  isOpen: boolean;
  onClose: () => void;
  isFixtureMode?: boolean;
}

export const VerificationUnavailableDialog: React.FC<
  VerificationUnavailableDialogProps
> = ({ isOpen, onClose, isFixtureMode = false }) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Identity Verification Preview">
      <div className="space-y-4">
        {isFixtureMode ? (
          <div className="rounded-xl border border-brand/40 bg-brandSoft/30 p-3.5 space-y-2">
            <span className="text-[10px] font-mono font-bold tracking-wider text-brand uppercase block">
              DEVELOPMENT FIXTURE — NOT LIVE ANALYSIS
            </span>
            <p className="text-xs text-secondary leading-relaxed">
              This is a development fixture preview of the identity challenge workflow. <strong>No verification request has been sent</strong>, and no remote services were contacted.
            </p>
          </div>
        ) : (
          <div className="rounded-xl bg-surface border border-subtle p-3.5 space-y-1">
            <div className="text-xs font-semibold text-primary">
              Identity Verification Not Connected
            </div>
            <p className="text-xs text-secondary leading-relaxed">
              The companion is operating in standalone mode. Remote multi-party identity challenges require connection to the SuSagi Guardian service.
            </p>
          </div>
        )}

        <div className="bg-surface rounded-xl p-4 border border-subtle space-y-2 text-xs text-secondary leading-relaxed">
          <div className="font-semibold text-primary">How Verification Works:</div>
          <ul className="list-disc list-inside space-y-1 text-muted">
            <li>Challenges incoming callers claiming official identity.</li>
            <li>Validates pre-shared family passphrases to detect voice synthesis.</li>
            <li>Maintains audit log of challenge results in the Activity history.</li>
          </ul>
        </div>

        <div className="flex flex-col sm:flex-row gap-2 pt-2 border-t border-subtle">
          <Link
            href={isFixtureMode ? "/verification?fixture=ready" : "/verification"}
            className="flex-1 min-h-[48px] inline-flex items-center justify-center px-4 py-3 rounded-xl bg-brand text-white font-semibold text-xs hover:bg-brandLight transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center"
          >
            View Requester Flow
          </Link>
          <SecondarySafetyAction label="Close" onClick={onClose} className="min-h-[48px]" />
        </div>
      </div>
    </Modal>
  );
};
