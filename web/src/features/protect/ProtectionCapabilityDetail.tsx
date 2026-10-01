"use client";

import React from "react";
import Link from "next/link";
import { ProtectionCapability } from "@/types/protection";
import { Modal } from "@/components/dialogs/Modal";
import { PrimarySafetyAction } from "@/components/actions/PrimarySafetyAction";
import { SecondarySafetyAction } from "@/components/actions/SecondarySafetyAction";
import { cn } from "@/lib/utils";

interface ProtectionCapabilityDetailProps {
  capability: ProtectionCapability | null;
  isOpen: boolean;
  onClose: () => void;
  onOpenLinkCheck?: () => void;
}

export const ProtectionCapabilityDetail: React.FC<
  ProtectionCapabilityDetailProps
> = ({ capability, isOpen, onClose, onOpenLinkCheck }) => {
  if (!capability) return null;

  const isWebAvailable = capability.status === "AVAILABLE";

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={capability.name}>
      <div className="space-y-5">
        {/* Status & Platform Header */}
        <div className="flex flex-wrap items-center justify-between gap-2 p-3.5 rounded-xl bg-surface border border-subtle">
          <div>
            <div className="text-[10px] font-semibold uppercase tracking-wider text-muted">
              Connection Status
            </div>
            <div className="text-sm font-semibold text-primary">
              {capability.statusLabel}
            </div>
          </div>

          <span
            className={cn(
              "text-xs px-2.5 py-1 rounded-full font-semibold border",
              isWebAvailable
                ? "bg-risk-low-soft text-risk-low border-risk-low"
                : "bg-surfaceElevated text-secondary border-default"
            )}
          >
            {capability.status.replace(/_/g, " ")}
          </span>
        </div>

        {/* 1. WHAT IT DOES */}
        <div className="space-y-1.5">
          <h3 className="text-xs font-semibold uppercase tracking-wider text-muted">
            What It Does
          </h3>
          <p className="text-sm text-secondary leading-relaxed bg-surface rounded-xl p-3.5 border border-subtle">
            {capability.whatItDoes}
          </p>
        </div>

        {/* 2. WHERE IT RUNS */}
        <div className="space-y-1.5">
          <h3 className="text-xs font-semibold uppercase tracking-wider text-muted">
            Where It Runs
          </h3>
          <div className="flex items-center gap-2.5 bg-surface rounded-xl p-3.5 border border-subtle text-xs text-secondary">
            <svg
              className="w-4 h-4 text-brand shrink-0"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"
              />
            </svg>
            <span className="font-medium text-primary">
              {capability.whereItRuns}
            </span>
          </div>
        </div>

        {/* 3. WHAT YOU CAN DO */}
        <div className="space-y-1.5">
          <h3 className="text-xs font-semibold uppercase tracking-wider text-muted">
            What You Can Do
          </h3>
          <p className="text-sm text-secondary leading-relaxed bg-surface rounded-xl p-3.5 border border-subtle">
            {capability.whatYouCanDo}
          </p>
        </div>

        {/* Actions Footer */}
        <div className="pt-2 border-t border-subtle flex flex-col sm:flex-row gap-2.5">
          {capability.id === "link-check" && onOpenLinkCheck ? (
            <PrimarySafetyAction
              label="Launch Link Inspector"
              onClick={() => {
                onClose();
                onOpenLinkCheck();
              }}
              className="flex-1"
            />
          ) : capability.primaryActionHref ? (
            <Link
              href={capability.primaryActionHref}
              className="flex-1 inline-flex items-center justify-center px-6 py-3 min-h-[48px] rounded-xl text-base font-semibold bg-brand text-white hover:bg-blue-600 transition-colors select-none focus-visible:ring-2 focus-visible:ring-brandLight"
            >
              {capability.primaryActionLabel || "View Details"}
            </Link>
          ) : null}

          <SecondarySafetyAction label="Close" onClick={onClose} />
        </div>
      </div>
    </Modal>
  );
};
