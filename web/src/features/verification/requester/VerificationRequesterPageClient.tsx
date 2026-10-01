"use client";

import React, { useState } from "react";
import { VerificationRequesterViewState } from "./requesterTypes";
import { VerificationStatus } from "@/types/verification";
import { VerificationRequesterHeader } from "./VerificationRequesterHeader";
import { VerificationStateCard } from "./VerificationStateCard";
import { VerificationContextCard } from "./VerificationContextCard";
import { VerificationGuidance } from "./VerificationGuidance";

interface VerificationRequesterPageClientProps {
  initialState: VerificationRequesterViewState;
}

export const VerificationRequesterPageClient: React.FC<
  VerificationRequesterPageClientProps
> = ({ initialState }) => {
  const { session, outcomeNote, isFixtureMode } = initialState;

  // Local state to support interactive preview transition in fixture mode (Step 12)
  const [currentStatus, setCurrentStatus] = useState<VerificationStatus>(
    initialState.status
  );

  const handlePreviewRequest = () => {
    if (isFixtureMode && currentStatus === "READY") {
      setCurrentStatus("PENDING");
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <VerificationRequesterHeader
        status={currentStatus}
        isFixtureMode={isFixtureMode}
      />

      {/* Main Content Area */}
      {currentStatus === "UNAVAILABLE" ? (
        <VerificationStateCard
          status={currentStatus}
          session={session}
          outcomeNote={outcomeNote}
          isFixtureMode={isFixtureMode}
        />
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Left Column (7 cols): State Card & Context */}
          <div className="lg:col-span-7 space-y-6">
            <VerificationStateCard
              status={currentStatus}
              session={session}
              outcomeNote={outcomeNote}
              isFixtureMode={isFixtureMode}
              onPreviewRequest={handlePreviewRequest}
            />

            {session && <VerificationContextCard session={session} />}
          </div>

          {/* Right Column (5 cols): Guidance & Directives */}
          <div className="lg:col-span-5 space-y-6">
            <VerificationGuidance />
          </div>
        </div>
      )}
    </div>
  );
};
