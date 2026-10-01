import React from "react";
import Link from "next/link";
import { ProtectionCapability } from "@/types/protection";
import { cn } from "@/lib/utils";

interface ProtectionOverviewProps {
  capabilities: ProtectionCapability[];
  onOpenLinkCheck: () => void;
}

export const ProtectionOverview: React.FC<ProtectionOverviewProps> = ({
  capabilities,
  onOpenLinkCheck,
}) => {
  return (
    <section aria-labelledby="protection-capabilities-title" className="space-y-3">
      <div className="flex items-center justify-between">
        <h2
          id="protection-capabilities-title"
          className="text-xs font-semibold uppercase tracking-wider text-muted"
        >
          Protection Capabilities
        </h2>
        <Link
          href="/protect"
          className="text-xs font-semibold text-brand hover:text-brandLight transition-colors"
        >
          Explore All Capabilities →
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {capabilities.map((cap) => {
          const isLinkCheck = cap.id === "link-check";
          const isWebAvailable = cap.status === "AVAILABLE";

          return (
            <div
              key={cap.id}
              className="rounded-xl bg-surface border border-subtle p-4 flex flex-col justify-between hover:border-default transition-colors"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-1.5">
                  <h3 className="text-sm font-semibold text-primary">{cap.name}</h3>
                  <span
                    className={cn(
                      "text-[11px] px-2 py-0.5 rounded font-medium border",
                      isWebAvailable
                        ? "bg-risk-low-soft text-risk-low border-risk-low"
                        : "bg-surfaceElevated text-muted border-subtle"
                    )}
                  >
                    {cap.statusLabel}
                  </span>
                </div>
                <p className="text-xs text-secondary leading-relaxed mb-3">
                  {cap.tagline}
                </p>
              </div>

              <div className="pt-2 border-t border-subtle flex items-center justify-between text-xs">
                <span className="text-muted text-[11px]">
                  {cap.whereItRuns}
                </span>

                {isLinkCheck ? (
                  <button
                    type="button"
                    onClick={onOpenLinkCheck}
                    className="font-semibold text-brand hover:text-brandLight transition-colors text-xs focus-visible:ring-2 focus-visible:ring-brandLight rounded px-1"
                  >
                    Inspect Link
                  </button>
                ) : cap.primaryActionHref ? (
                  <Link
                    href={cap.primaryActionHref}
                    className="font-semibold text-brand hover:text-brandLight transition-colors text-xs"
                  >
                    {cap.primaryActionLabel}
                  </Link>
                ) : (
                  <span className="text-muted text-[11px]">Mobile only</span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
};
