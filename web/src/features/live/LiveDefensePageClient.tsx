"use client";

import React, { useState } from "react";
import { LiveDefenseViewState } from "./liveTypes";
import { LiveDefenseHeader } from "./LiveDefenseHeader";
import { LiveConnectionState } from "./LiveConnectionState";
import { LiveRiskHero } from "./LiveRiskHero";
import { LiveSignalsPanel } from "./LiveSignalsPanel";
import { LiveEvidencePanel } from "./LiveEvidencePanel";
import { ProtectiveActionsPanel } from "./ProtectiveActionsPanel";
import { VerificationUnavailableDialog } from "./VerificationUnavailableDialog";

interface LiveDefensePageClientProps {
  initialState: LiveDefenseViewState;
}

export const LiveDefensePageClient: React.FC<LiveDefensePageClientProps> = ({
  initialState,
}) => {
  const [isVerifyOpen, setIsVerifyOpen] = useState(false);
  const { connectionState, assessment, isFixtureMode } = initialState;

  return (
    <div className="space-y-8 max-w-3xl mx-auto">
      {/* Page Header */}
      <LiveDefenseHeader
        connectionState={connectionState}
        isFixtureMode={isFixtureMode}
      />

      {/* Normal Runtime Standby (No active call stream or assessment) */}
      {!assessment ? (
        <LiveConnectionState state={connectionState} />
      ) : (
        /* Sequential Calm Experience Following Exact Required Hierarchy:
           1. Risk State
           2. Human sentence
           3. Short explanation
           4. Immediate actions
           5. Evidence & Signals
           6. Safety guidance
           7. Secondary details
        */
        <div className="space-y-6">
          {/* 1, 2, 3: Risk State, Human Sentence, Short Explanation */}
          <LiveRiskHero assessment={assessment} />

          {/* 4 & 6: Immediate Actions and Safety Guidance */}
          <ProtectiveActionsPanel
            primaryAction={assessment.recommendedAction}
            additionalActions={assessment.additionalActions}
            onOpenVerifyDialog={() => setIsVerifyOpen(true)}
            isFixtureMode={isFixtureMode}
          />

          {/* 5: Evidence & Signals: What SuSagi noticed and What was said */}
          <div className="space-y-6">
            <LiveSignalsPanel signals={assessment.signals} />
            <LiveEvidencePanel evidence={assessment.evidence} />
          </div>

          {/* 7: Secondary Details */}
          <div className="rounded-2xl bg-surface border border-subtle p-4 text-xs text-muted flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 shadow-sm">
            <div>
              <span>Recorded: {assessment.timestamp}</span>
              {assessment.id && (
                <span className="ml-2 font-mono">· Ref {assessment.id.slice(0, 8)}</span>
              )}
            </div>
            <span>Protection runs locally on your Android device</span>
          </div>
        </div>
      )}

      {/* Identity Verification Dialog */}
      <VerificationUnavailableDialog
        isOpen={isVerifyOpen}
        onClose={() => setIsVerifyOpen(false)}
        isFixtureMode={isFixtureMode}
      />
    </div>
  );
};
