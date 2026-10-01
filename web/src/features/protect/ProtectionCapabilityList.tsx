"use client";

import React from "react";
import { ProtectionCapability } from "@/types/protection";
import { cn } from "@/lib/utils";

interface ProtectionCapabilityListProps {
  capabilities: ProtectionCapability[];
  onSelectCapability: (cap: ProtectionCapability) => void;
  onOpenLinkCheck: () => void;
}

const getCapabilitySummary = (cap: ProtectionCapability): string => {
  switch (cap.id) {
    case "call-protection":
      return "Active on your Android phone · Monitors live calls for pressure and fraud patterns";
    case "message-protection":
      return "Active on your Android phone · Screens incoming SMS for impersonation and malicious links";
    case "link-check":
      return "Available in web companion · Inspects URLs for deceptive domains and scam patterns";
    case "qr-shield":
      return "Available on mobile device · Scans and verifies QR codes before opening destinations";
    default:
      return cap.tagline;
  }
};

const getCapabilityBadge = (cap: ProtectionCapability): { label: string; isAvailable: boolean } => {
  if (cap.platform === "WEB_COMPANION") {
    return { label: "In companion", isAvailable: true };
  }
  return { label: "On phone", isAvailable: false };
};

export const ProtectionCapabilityList: React.FC<ProtectionCapabilityListProps> = ({
  capabilities,
  onSelectCapability,
  onOpenLinkCheck,
}) => {
  return (
    <div className="rounded-2xl bg-surface border border-subtle shadow-sm divide-y divide-subtle overflow-hidden">
      {capabilities.map((cap) => {
        const summary = getCapabilitySummary(cap);
        const badge = getCapabilityBadge(cap);
        const isLinkCheck = cap.id === "link-check";

        return (
          <div
            key={cap.id}
            className="group flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 hover:bg-surfaceElevated/50 transition-colors"
          >
            <button
              type="button"
              onClick={() => {
                if (isLinkCheck) {
                  onOpenLinkCheck();
                } else {
                  onSelectCapability(cap);
                }
              }}
              className="flex-1 flex items-start gap-4 text-left cursor-pointer min-h-[48px] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brandLight rounded-xl"
            >
              <div className="w-10 h-10 rounded-xl bg-surfaceElevated border border-subtle flex items-center justify-center text-primary shrink-0 group-hover:border-default transition-colors">
                {cap.id === "call-protection" ? (
                  <svg className="w-5 h-5 text-brand" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                ) : cap.id === "message-protection" ? (
                  <svg className="w-5 h-5 text-brand" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                ) : cap.id === "link-check" ? (
                  <svg className="w-5 h-5 text-brand" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                  </svg>
                ) : (
                  <svg className="w-5 h-5 text-brand" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
                  </svg>
                )}
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-primary text-base">
                    {cap.name}
                  </span>
                  <span
                    className={cn(
                      "text-[11px] font-medium px-2 py-0.5 rounded-full border",
                      badge.isAvailable
                        ? "bg-risk-low-soft text-risk-low border-risk-low"
                        : "bg-surfaceElevated text-secondary border-subtle"
                    )}
                  >
                    {badge.label}
                  </span>
                </div>
                <p className="text-sm text-secondary leading-relaxed">
                  {summary}
                </p>
              </div>
            </button>

            <div className="flex items-center gap-2 self-end sm:self-center shrink-0">
              {isLinkCheck ? (
                <button
                  type="button"
                  onClick={onOpenLinkCheck}
                  className="min-h-[44px] px-3.5 py-2 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors cursor-pointer inline-flex items-center gap-1.5 focus-visible:ring-2 focus-visible:ring-brandLight"
                >
                  <span>Inspect link</span>
                  <span aria-hidden="true">›</span>
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => onSelectCapability(cap)}
                  className="min-h-[44px] px-3.5 py-2 rounded-xl text-xs font-medium text-secondary hover:text-primary hover:bg-surfaceElevated transition-colors cursor-pointer inline-flex items-center gap-1 focus-visible:ring-2 focus-visible:ring-brandLight"
                  aria-label={`View details for ${cap.name}`}
                >
                  <span>Details</span>
                  <span aria-hidden="true" className="text-muted group-hover:text-primary">›</span>
                </button>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};
