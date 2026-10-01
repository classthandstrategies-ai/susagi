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
    <div className="space-y-6 max-w-[1440px] mx-auto">
      {/* Page Header */}
      <LiveDefenseHeader
        connectionState={connectionState}
        isFixtureMode={isFixtureMode}
      />

      {/* Normal Runtime Standby (No active call stream or assessment) */}
      {!assessment ? (
        <LiveConnectionState state={connectionState} />
      ) : (
        /* Populated Live Defense Experience (Fixtures in Dev / Active Stream) */
        <div className="space-y-6">
          {/* Risk Hero: What is happening, why it matters, what to do next */}
          <LiveRiskHero assessment={assessment} />

          {/* Responsive Layout Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            {/* Left Column (7 cols): Signals and Captured Evidence */}
            <div className="lg:col-span-7 space-y-6">
              <LiveSignalsPanel signals={assessment.signals} />
              <LiveEvidencePanel evidence={assessment.evidence} />
            </div>

            {/* Right Column (5 cols): Protective Directives & Telephony Guidance */}
            <div className="lg:col-span-5 space-y-6">
              <ProtectiveActionsPanel
                primaryAction={assessment.recommendedAction}
                additionalActions={assessment.additionalActions}
                onOpenVerifyDialog={() => setIsVerifyOpen(true)}
                isFixtureMode={isFixtureMode}
              />

              {/* Live Session Telemetry Card */}
              <section
                aria-labelledby="telemetry-heading"
                className="rounded-2xl bg-surface border border-subtle p-5 space-y-3"
              >
                <div className="flex items-center justify-between">
                  <h3
                    id="telemetry-heading"
                    className="text-xs font-semibold uppercase tracking-wider text-muted"
                  >
                    Companion Stream Diagnostics
                  </h3>
                  <span className="text-[10px] font-mono text-muted">
                    {isFixtureMode ? "SIMULATED TELEMETRY" : "DEVICE LINK"}
                  </span>
                </div>

                <div className="space-y-2 font-mono text-xs">
                  <div className="flex items-center justify-between p-2.5 rounded-lg bg-surfaceElevated text-secondary">
                    <span className="text-muted">Acoustic Pipeline:</span>
                    <span className="text-primary font-semibold">
                      {isFixtureMode ? "Active Telemetry (Fixture)" : "Standby"}
                    </span>
                  </div>
                  <div className="flex items-center justify-between p-2.5 rounded-lg bg-surfaceElevated text-secondary">
                    <span className="text-muted">Linguistic Engine:</span>
                    <span className="text-primary font-semibold">
                      Bilingual (Hi / En)
                    </span>
                  </div>
                  <div className="flex items-center justify-between p-2.5 rounded-lg bg-surfaceElevated text-secondary">
                    <span className="text-muted">Device Control:</span>
                    <span className="text-risk-caution font-semibold">
                      Offline / Disconnected
                    </span>
                  </div>
                </div>

                <p className="text-[11px] text-muted leading-relaxed pt-1">
                  The web companion observes telemetry and displays protective guidance. Direct telephony hangup must be performed on the physical phone handset.
                </p>
              </section>
            </div>
          </div>
        </div>
      )}

      {/* Accessible Identity Verification Preview Modal */}
      <VerificationUnavailableDialog
        isOpen={isVerifyOpen}
        onClose={() => setIsVerifyOpen(false)}
        isFixtureMode={isFixtureMode}
      />
    </div>
  );
};
