import React from "react";
import Link from "next/link";
import { ProtectionCapability } from "@/types/protection";
import { cn } from "@/lib/utils";

interface ProtectionCapabilityListProps {
  capabilities: ProtectionCapability[];
  onSelectCapability: (cap: ProtectionCapability) => void;
  onOpenLinkCheck: () => void;
}

export const ProtectionCapabilityList: React.FC<
  ProtectionCapabilityListProps
> = ({ capabilities, onSelectCapability, onOpenLinkCheck }) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {capabilities.map((cap) => {
        const isWebAvailable = cap.status === "AVAILABLE";
        const isLinkCheck = cap.id === "link-check";

        return (
          <article
            key={cap.id}
            aria-labelledby={`cap-title-${cap.id}`}
            className="rounded-2xl bg-surface border border-subtle hover:border-default p-5 flex flex-col justify-between transition-colors shadow-sm"
          >
            <div>
              {/* Header with Title and Platform Status */}
              <div className="flex items-start justify-between gap-3 mb-2">
                <div className="space-y-0.5">
                  <h3
                    id={`cap-title-${cap.id}`}
                    className="text-base font-semibold text-primary"
                  >
                    {cap.name}
                  </h3>
                  <div className="flex items-center gap-1.5 text-[11px] text-muted font-medium">
                    <span
                      className={cn(
                        "w-2 h-2 rounded-full",
                        cap.platform === "WEB_COMPANION" ? "bg-brand" : "bg-muted"
                      )}
                      aria-hidden="true"
                    />
                    <span>
                      {cap.platform === "WEB_COMPANION"
                        ? "Web Companion"
                        : "Android Device"}
                    </span>
                  </div>
                </div>

                <span
                  className={cn(
                    "text-[11px] px-2.5 py-1 rounded-full font-semibold border shrink-0",
                    isWebAvailable
                      ? "bg-risk-low-soft text-risk-low border-risk-low"
                      : "bg-surfaceElevated text-secondary border-default"
                  )}
                >
                  {cap.statusLabel}
                </span>
              </div>

              {/* Tagline & Description */}
              <p className="text-xs text-secondary leading-relaxed mb-4">
                {cap.tagline}
              </p>
            </div>

            {/* Bottom Actions */}
            <div className="pt-3 border-t border-subtle flex items-center justify-between text-xs">
              <button
                type="button"
                onClick={() => onSelectCapability(cap)}
                className="text-secondary hover:text-primary font-medium transition-colors focus-visible:ring-2 focus-visible:ring-brandLight rounded px-1 py-0.5"
              >
                Learn More
              </button>

              {isLinkCheck ? (
                <button
                  type="button"
                  onClick={onOpenLinkCheck}
                  className="px-3 py-1.5 rounded-lg bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-primary font-semibold text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
                >
                  Inspect Link
                </button>
              ) : cap.primaryActionHref ? (
                <Link
                  href={cap.primaryActionHref}
                  className="px-3 py-1.5 rounded-lg bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-brand hover:text-brandLight font-semibold text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
                >
                  {cap.primaryActionLabel}
                </Link>
              ) : (
                <span className="text-[11px] text-muted">
                  Use SuSagi mobile app
                </span>
              )}
            </div>
          </article>
        );
      })}
    </div>
  );
};
