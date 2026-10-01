import React from "react";
import { IncidentTimelineItem, TimelineEventType } from "@/types/activity";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { cn } from "@/lib/utils";

interface IncidentTimelineProps {
  timeline: IncidentTimelineItem[];
  isFixtureMode?: boolean;
}

const getEventIcon = (type?: TimelineEventType) => {
  switch (type) {
    case "CALL":
      return (
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />
        </svg>
      );
    case "MESSAGE":
      return (
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a.75.75 0 01-1.074-.85 9.97 9.97 0 001.378-3.08A8.064 8.064 0 013 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
        </svg>
      );
    case "LINK":
      return (
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
        </svg>
      );
    case "VERIFICATION":
      return (
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
        </svg>
      );
    case "ACTION":
      return (
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
        </svg>
      );
    default:
      return (
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      );
  }
};

export const IncidentTimeline: React.FC<IncidentTimelineProps> = ({
  timeline,
  isFixtureMode,
}) => {
  return (
    <section
      aria-labelledby="timeline-heading"
      className="rounded-2xl bg-surface border border-subtle p-6 space-y-4"
    >
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 id="timeline-heading" className="text-base font-semibold text-primary">
          Incident Progression Timeline
        </h2>
        <span className="text-[11px] font-mono text-muted">
          Chronological Event Sequence
        </span>
      </div>

      <div className="pt-2">
        {timeline.map((item, index) => {
          const isLast = index === timeline.length - 1;

          return (
            <div key={item.id} className="flex gap-4 relative">
              {/* Connecting Line */}
              {!isLast && (
                <div
                  className="absolute left-[15px] top-8 bottom-0 w-[2px] bg-subtle"
                  aria-hidden="true"
                />
              )}

              {/* Event Type & Risk Indicator Node */}
              <div
                className={cn(
                  "relative z-10 w-8 h-8 rounded-full bg-surfaceElevated border border-subtle flex items-center justify-center shrink-0 mt-0.5 text-muted",
                  item.riskLevel === "CRITICAL" && "text-risk-critical border-risk-critical/40",
                  item.riskLevel === "HIGH" && "text-risk-high border-risk-high/40",
                  item.riskLevel === "CAUTION" && "text-risk-caution border-risk-caution/40",
                  item.riskLevel === "LOW" && "text-risk-low border-risk-low/40"
                )}
              >
                {getEventIcon(item.eventType)}
              </div>

              {/* Item Content */}
              <div className="flex-1 pb-6">
                <div className="flex flex-wrap items-center justify-between gap-2 mb-1">
                  <div className="flex items-center gap-2">
                    <h3 className="text-sm font-semibold text-primary">
                      {item.title}
                    </h3>
                    {item.eventType && (
                      <span className="text-[10px] font-mono font-semibold px-1.5 py-0.2 rounded bg-surfaceHighlight text-muted uppercase">
                        {item.eventType}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-2">
                    <RiskBadge level={item.riskLevel} size="sm" />
                    <span className="text-xs text-muted font-mono">
                      {item.timestamp}
                    </span>
                  </div>
                </div>
                <p className="text-sm text-secondary leading-relaxed">
                  {item.detail}
                </p>
              </div>
            </div>
          );
        })}
      </div>

      {isFixtureMode && (
        <p className="text-[11px] text-muted leading-relaxed pt-2 border-t border-subtle">
          * Note: Multi-channel sequence (SMS, Voice, Link, Verification) is simulated fixture demonstration data. Normal production companion describes activity records without implying cross-channel correlation.
        </p>
      )}
    </section>
  );
};
