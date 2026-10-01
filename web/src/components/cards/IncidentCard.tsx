"use client";

import React from "react";
import Link from "next/link";
import { IncidentItem } from "@/types/activity";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { formatProtectiveAction, cn } from "@/lib/utils";

interface IncidentCardProps {
  incident: IncidentItem;
  className?: string;
  href?: string;
}

export const IncidentCard: React.FC<IncidentCardProps> = ({
  incident,
  className,
  href,
}) => {
  const targetHref = href || `/activity/${incident.id}`;

  return (
    <article
      className={cn(
        "rounded-2xl bg-surface border border-subtle hover:border-default p-5 transition-colors shadow-sm",
        className
      )}
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
        <div className="flex items-center gap-2">
          <RiskBadge level={incident.riskLevel} size="sm" />
          <span className="text-xs text-muted font-mono">{incident.timestamp}</span>
        </div>
        <div className="flex items-center gap-2 text-xs">
          {incident.claimedIdentity && (
            <span className="text-primary font-medium bg-surfaceElevated px-2 py-0.5 rounded-full border border-subtle">
              {incident.claimedIdentity}
            </span>
          )}
          <span className="text-secondary font-mono">{incident.source}</span>
        </div>
      </div>

      <h3 className="text-base font-semibold text-primary mb-1.5">
        <Link
          href={targetHref}
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
          Action taken:{" "}
          <span className="text-primary font-medium">
            {formatProtectiveAction(incident.actionTaken)}
          </span>
        </div>

        <Link
          href={targetHref}
          className="inline-flex items-center gap-1 font-medium text-brand hover:text-brandLight transition-colors"
        >
          <span>View incident</span>
          <span aria-hidden="true">›</span>
        </Link>
      </div>
    </article>
  );
};
