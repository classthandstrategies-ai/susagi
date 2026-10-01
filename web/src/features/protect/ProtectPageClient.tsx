"use client";

import React, { useState } from "react";
import { ProtectionCapability } from "@/types/protection";
import { ProtectionCapabilityList } from "./ProtectionCapabilityList";
import { ProtectionCapabilityDetail } from "./ProtectionCapabilityDetail";
import { LinkCheckDialog } from "./LinkCheckDialog";

interface ProtectPageClientProps {
  capabilities: ProtectionCapability[];
  isFixtureMode?: boolean;
}

export const ProtectPageClient: React.FC<ProtectPageClientProps> = ({
  capabilities,
  isFixtureMode = false,
}) => {
  const [selectedCapability, setSelectedCapability] =
    useState<ProtectionCapability | null>(null);
  const [isLinkCheckOpen, setIsLinkCheckOpen] = useState(false);

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <header className="space-y-1">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            Protection Shields
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/30 uppercase">
              FIXTURE MODE
            </span>
          )}
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
          Defense Capabilities
        </h1>
        <p className="text-sm text-secondary max-w-2xl leading-relaxed">
          Comprehensive multi-layered protection architecture. Device-level telephony shields run on Android, while link and verification checks run natively in the companion.
        </p>
      </header>

      {/* Architecture Context Banner */}
      <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
        <div className="space-y-0.5">
          <div className="font-semibold text-primary">
            Companion & Device Architecture
          </div>
          <p className="text-secondary">
            Web companion provides URL inspection, identity verification, and telemetry audit. Telephony interception requires an Android phone running SuSagi.
          </p>
        </div>
        <button
          type="button"
          onClick={() => setIsLinkCheckOpen(true)}
          className="px-3.5 py-1.5 rounded-lg bg-brand hover:bg-blue-600 text-white font-semibold shrink-0 transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
        >
          Inspect a Link
        </button>
      </div>

      {/* Capabilities List */}
      <ProtectionCapabilityList
        capabilities={capabilities}
        onSelectCapability={(cap) => setSelectedCapability(cap)}
        onOpenLinkCheck={() => setIsLinkCheckOpen(true)}
      />

      {/* Capability Detail Modal */}
      <ProtectionCapabilityDetail
        capability={selectedCapability}
        isOpen={Boolean(selectedCapability)}
        onClose={() => setSelectedCapability(null)}
        onOpenLinkCheck={() => setIsLinkCheckOpen(true)}
      />

      {/* Interactive Link Check Dialog */}
      <LinkCheckDialog
        isOpen={isLinkCheckOpen}
        onClose={() => setIsLinkCheckOpen(false)}
        isFixtureMode={isFixtureMode}
      />
    </div>
  );
};
