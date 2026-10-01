import React from "react";
import { ScamSignal } from "@/types/risk";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { cn } from "@/lib/utils";

interface SignalCardProps {
  signal: ScamSignal;
  className?: string;
}

export const SignalCard: React.FC<SignalCardProps> = ({ signal, className }) => {
  return (
    <article
      aria-labelledby={`signal-title-${signal.id}`}
      className={cn(
        "rounded-xl bg-surface border border-subtle hover:border-default p-4 transition-colors",
        className
      )}
    >
      <div className="flex items-start justify-between gap-3 mb-2">
        <h3
          id={`signal-title-${signal.id}`}
          className="text-base font-semibold text-primary"
        >
          {signal.title}
        </h3>
        <RiskBadge level={signal.severity} size="sm" />
      </div>

      <p className="text-sm text-secondary leading-relaxed mb-3">
        {signal.description}
      </p>

      {signal.highlightedText && (
        <div className="rounded-lg bg-surfaceElevated px-3 py-2 border-l-2 border-brand text-xs text-secondary font-mono mb-2">
          &ldquo;{signal.highlightedText}&rdquo;
        </div>
      )}

      <div className="flex items-center justify-between text-xs text-muted pt-2 border-t border-subtle">
        {signal.category && (
          <span className="uppercase tracking-wider font-semibold">
            {signal.category.replace(/_/g, " ")}
          </span>
        )}
        {signal.timestamp && <span>{signal.timestamp}</span>}
      </div>
    </article>
  );
};
