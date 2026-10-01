import React from "react";
import Link from "next/link";
import { IncidentItem } from "@/types/activity";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { formatProtectiveAction } from "@/lib/utils";

interface RecentActivityPreviewProps {
  incidents: IncidentItem[];
  isFixtureMode?: boolean;
}

export const RecentActivityPreview: React.FC<RecentActivityPreviewProps> = ({
  incidents,
  isFixtureMode = false,
}) => {
  return (
    <section aria-labelledby="recent-activity-title" className="space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h2
            id="recent-activity-title"
            className="text-xs font-semibold uppercase tracking-wider text-muted"
          >
            Recent Security Activity
          </h2>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-brandSoft text-brand border border-brand/30 uppercase">
              FIXTURE
            </span>
          )}
        </div>
        <Link
          href="/activity"
          className="text-xs font-semibold text-brand hover:text-brandLight transition-colors"
        >
          View Full History →
        </Link>
      </div>

      {incidents.length === 0 ? (
        <div className="rounded-xl bg-surface border border-subtle p-6 text-center space-y-2">
          <div className="w-10 h-10 rounded-full bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
            <svg
              className="w-5 h-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={1.5}
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.002A11.959 11.959 0 0112 2.714z"
              />
            </svg>
          </div>
          <h3 className="text-sm font-semibold text-primary">
            No activity connected yet
          </h3>
          <p className="text-xs text-secondary max-w-sm mx-auto leading-relaxed">
            When SuSagi services are connected, important safety events will appear here.
          </p>
        </div>
      ) : (
        <div className="space-y-2.5">
          {incidents.map((inc) => (
            <Link
              key={inc.id}
              href={`/activity/${inc.id}`}
              className="block rounded-xl bg-surface border border-subtle hover:border-default p-4 transition-colors group focus-visible:ring-2 focus-visible:ring-brandLight"
            >
              <div className="flex items-center justify-between gap-2 mb-1.5">
                <div className="flex items-center gap-2">
                  <RiskBadge level={inc.riskLevel} size="sm" />
                  <span className="text-[11px] text-muted font-mono">{inc.timestamp}</span>
                </div>
                {isFixtureMode && (
                  <span className="text-[9px] font-mono uppercase text-muted bg-surfaceElevated px-1.5 py-0.5 rounded">
                    FIXTURE DATA
                  </span>
                )}
              </div>

              <h4 className="text-sm font-semibold text-primary group-hover:text-brand transition-colors truncate">
                {inc.title}
              </h4>
              <p className="text-xs text-secondary mt-1 line-clamp-1">
                {inc.summary}
              </p>

              <div className="flex items-center justify-between pt-2 mt-2 border-t border-subtle text-[11px] text-muted">
                <span>Action: {formatProtectiveAction(inc.actionTaken)}</span>
                <span className="font-semibold text-brand group-hover:underline">
                  Details →
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </section>
  );
};
