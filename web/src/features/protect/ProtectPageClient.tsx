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
  const [quickUrl, setQuickUrl] = useState("");

  const handleQuickInspect = (e: React.FormEvent) => {
    e.preventDefault();
    if (quickUrl.trim()) {
      setIsLinkCheckOpen(true);
    }
  };

  return (
    <div className="space-y-8 max-w-3xl mx-auto">
      {/* Page Header */}
      <header className="space-y-2">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-secondary">
            Shields & Tools
          </span>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded-full bg-surfaceElevated text-muted border border-subtle">
              Fixture mode
            </span>
          )}
        </div>
        <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-primary">
          Protection
        </h1>
        <p className="text-sm text-secondary leading-relaxed">
          SuSagi protects you across calls, messages, and links. Telephony shields run on your Android phone, while link and verification checks run natively here in your web companion.
        </p>
      </header>

      {/* Quick Link Check Utility Card */}
      <div className="rounded-2xl bg-surface border border-subtle p-5 shadow-sm space-y-3">
        <div className="flex items-center justify-between gap-3">
          <div className="space-y-0.5">
            <h2 className="text-sm font-semibold text-primary">
              Check a suspicious link
            </h2>
            <p className="text-xs text-secondary">
              Inspect an unfamiliar URL safely before opening it or entering passwords.
            </p>
          </div>
          <button
            type="button"
            onClick={() => setIsLinkCheckOpen(true)}
            className="min-h-[44px] px-3.5 py-2 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors cursor-pointer shrink-0"
          >
            Open inspector
          </button>
        </div>

        <form onSubmit={handleQuickInspect} className="flex gap-2 pt-1">
          <input
            type="text"
            value={quickUrl}
            onChange={(e) => setQuickUrl(e.target.value)}
            placeholder="Paste any link to inspect..."
            className="flex-1 px-3.5 py-2.5 rounded-xl bg-surfaceElevated border border-subtle text-primary font-mono text-xs placeholder:text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brandLight"
          />
          <button
            type="submit"
            className="min-h-[44px] px-4 py-2 rounded-xl bg-brand hover:bg-brandLight text-white font-medium text-xs transition-colors cursor-pointer shrink-0 inline-flex items-center justify-center focus-visible:ring-2 focus-visible:ring-brandLight"
          >
            Check
          </button>
        </form>
      </div>

      {/* Capabilities List */}
      <div className="space-y-3">
        <h2 className="text-xs font-medium text-secondary">
          Active shields & companions
        </h2>
        <ProtectionCapabilityList
          capabilities={capabilities}
          onSelectCapability={(cap) => setSelectedCapability(cap)}
          onOpenLinkCheck={() => setIsLinkCheckOpen(true)}
        />
      </div>

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
        onClose={() => {
          setIsLinkCheckOpen(false);
          setQuickUrl("");
        }}
        isFixtureMode={isFixtureMode}
      />
    </div>
  );
};
