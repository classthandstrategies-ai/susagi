import React from "react";
import { ScamEvidence } from "@/types/risk";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { cn } from "@/lib/utils";

interface EvidenceCardProps {
  evidence: ScamEvidence;
  className?: string;
}

export const EvidenceCard: React.FC<EvidenceCardProps> = ({
  evidence,
  className,
}) => {
  return (
    <div
      className={cn(
        "rounded-xl bg-surface border border-subtle p-4 space-y-3",
        className
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <h4 className="text-sm font-semibold text-primary">{evidence.title}</h4>
          <span className="text-xs text-muted font-mono">
            {evidence.callerOrSource}
          </span>
        </div>
        <div className="flex items-center gap-2">
          <RiskBadge level={evidence.severity} size="sm" />
          <span className="text-xs text-muted font-mono">{evidence.timestamp}</span>
        </div>
      </div>

      {evidence.transcriptSnippet && (
        <div className="rounded-lg bg-surfaceElevated p-3 border border-subtle text-xs sm:text-sm text-secondary italic leading-relaxed">
          &ldquo;{evidence.transcriptSnippet}&rdquo;
        </div>
      )}

      {evidence.tags && evidence.tags.length > 0 && (
        <div className="flex flex-wrap gap-1.5 pt-1">
          {evidence.tags.map((tag) => (
            <span
              key={tag}
              className="px-2 py-0.5 rounded text-[11px] font-medium bg-surfaceHighlight text-secondary"
            >
              {tag}
            </span>
          ))}
        </div>
      )}
    </div>
  );
};
