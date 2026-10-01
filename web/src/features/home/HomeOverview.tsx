import React from "react";
import { WebProtectionOverview } from "@/types/protection";
import { ProtectionStatusCard } from "@/components/cards/ProtectionStatusCard";

interface HomeOverviewProps {
  overview: WebProtectionOverview;
  isFixtureMode?: boolean;
}

export const HomeOverview: React.FC<HomeOverviewProps> = ({
  overview,
  isFixtureMode = false,
}) => {
  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-xs font-semibold uppercase tracking-wider text-muted">
          Protection Overview
        </h2>
        {isFixtureMode && (
          <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-brandSoft text-brand border border-brand/30 uppercase">
            DEVELOPMENT FIXTURE
          </span>
        )}
      </div>

      <ProtectionStatusCard overview={overview} isFixture={isFixtureMode} />
    </div>
  );
};
