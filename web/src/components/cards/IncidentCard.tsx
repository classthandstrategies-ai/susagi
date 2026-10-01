import React from "react";
import Link from "next/link";
import { IncidentItem } from "@/types/activity";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { formatProtectiveAction, cn } from "@/lib/utils";

interface IncidentCardProps {
  incident: IncidentItem;
  className?: string;
}

export const IncidentCard: React.FC<IncidentCardProps> = ({
  incident,
  className,
}) => {
  return (
    <article
      className={cn(
        "rounded-xl bg-surface border border-subtle hover:border-default p-5 transition-colors",
        className
      )}
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
        <div className="flex items-center gap-2">
          <RiskBadge level={incident.riskLevel} size="sm" />
          <span className="text-xs text-muted font-mono">{incident.timestamp}</span>
        </div>
        <span className="text-xs text-secondary font-mono">
          {incident.source}
        </span>
      </div>

      <h3 className="text-base font-semibold text-primary mb-1.5">
        <Link
          href={`/activity/${incident.id}`}
          className="hover:text-brand transition-colors focus-visible:ring-2 focus-visible:ring-brandLight rounded"
        >
          {incident.title}
        </Link>
      </h3>

      <p className="text-sm text-secondary leading-relaxed mb-4 line-clamp-2">
        {incident.summary}
      </p>

      <div className="flex items-center justify-between pt-3 border-t border-subtle text-xs">
        <div className="text-muted">
          Action:{" "}
          <span className="text-primary font-medium">
            {formatProtectiveAction(incident.actionTaken)}
          </span>
        </div>

        <Link
          href={`/activity/${incident.id}`}
          className="inline-flex items-center gap-1 font-semibold text-brand hover:text-brandLight transition-colors"
        >
          <span>View Details</span>
          <svg
            className="w-3.5 h-3.5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
            aria-hidden="true"
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
          </svg>
        </Link>
      </div>
    </article>
  );
};
