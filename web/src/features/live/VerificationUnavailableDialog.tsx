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
    <Modal isOpen={isOpen} onClose={onClose} title="Identity verification">
      <div className="space-y-4">
        {isFixtureMode ? (
          <div className="rounded-xl border border-brand/20 bg-brandSoft/20 p-3.5 space-y-1.5">
            <span className="text-xs font-semibold text-brand">
              Fixture preview
            </span>
            <p className="text-xs text-secondary leading-relaxed">
              This is a development preview of the verification challenge flow. No challenge request has been sent to family members or external services.
            </p>
          </div>
        ) : (
          <div className="rounded-xl bg-surfaceElevated border border-subtle p-3.5 space-y-1">
            <div className="text-xs font-semibold text-primary">
              Identity verification not connected
            </div>
            <p className="text-xs text-secondary leading-relaxed">
              The companion is currently operating in standalone mode. Remote verification requests require connection to your SuSagi Guardian Circle on your phone.
            </p>
          </div>
        )}

        <div className="bg-surface rounded-xl p-4 border border-subtle space-y-2 text-xs text-secondary leading-relaxed">
          <div className="font-semibold text-primary">How verification works:</div>
          <ul className="list-disc list-inside space-y-1 text-secondary">
            <li>Asks a trusted family member or contact to confirm an urgent claim.</li>
            <li>Checks pre-shared secret phrases or mutual questions to detect voice clones.</li>
            <li>Shows you the result directly on your screen so you can make a safe decision.</li>
          </ul>
        </div>

        <div className="flex flex-col sm:flex-row gap-2 pt-2 border-t border-subtle">
          <Link
            href={isFixtureMode ? "/verification?fixture=ready" : "/verification"}
            className="flex-1 min-h-[44px] inline-flex items-center justify-center px-4 py-2.5 rounded-xl bg-brand text-white font-medium text-xs hover:bg-brandLight transition-colors focus-visible:ring-2 focus-visible:ring-brandLight text-center"
          >
            View verification flow
          </Link>
          <SecondarySafetyAction label="Close" onClick={onClose} className="min-h-[44px]" />
        </div>
      </div>
    </Modal>
  );
};
