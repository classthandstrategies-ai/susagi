"use client";

import React, { useMemo } from "react";
import { IncidentItem } from "@/types/activity";
import { IncidentCard } from "@/components/cards/IncidentCard";

interface ActivityListProps {
  incidents: IncidentItem[];
  fixtureKey?: string;
}

interface IncidentGroup {
  label: string;
  items: IncidentItem[];
}

export const ActivityList: React.FC<ActivityListProps> = ({
  incidents,
  fixtureKey,
}) => {
  // Chronological grouping into Today, Yesterday, Earlier
  const groups: IncidentGroup[] = useMemo(() => {
    const todayItems: IncidentItem[] = [];
    const yesterdayItems: IncidentItem[] = [];
    const earlierItems: IncidentItem[] = [];

    incidents.forEach((item) => {
      const lower = item.timestamp.toLowerCase();
      if (lower.startsWith("today")) {
        todayItems.push(item);
      } else if (lower.startsWith("yesterday")) {
        yesterdayItems.push(item);
      } else {
        earlierItems.push(item);
      }
    });

    const result: IncidentGroup[] = [];
    if (todayItems.length > 0) result.push({ label: "Today", items: todayItems });
    if (yesterdayItems.length > 0) result.push({ label: "Yesterday", items: yesterdayItems });
    if (earlierItems.length > 0) result.push({ label: "Earlier", items: earlierItems });

    // Fallback if none matched
    if (result.length === 0 && incidents.length > 0) {
      result.push({ label: "All activity", items: incidents });
    }

    return result;
  }, [incidents]);

  return (
    <div
      role="feed"
      aria-label="Security Activity Incidents"
      className="space-y-8"
    >
      {groups.map((group) => (
        <section key={group.label} aria-labelledby={`group-${group.label}`} className="space-y-3">
          <h2
            id={`group-${group.label}`}
            className="text-xs font-semibold text-secondary uppercase tracking-wider px-1"
          >
            {group.label}
          </h2>

          <div className="space-y-3">
            {group.items.map((incident) => {
              const detailHref = fixtureKey
                ? `/activity/${incident.id}?fixture=${fixtureKey}`
                : `/activity/${incident.id}`;

              return (
                <IncidentCard
                  key={incident.id}
                  incident={incident}
                  href={detailHref}
                />
              );
            })}
          </div>
        </section>
      ))}
    </div>
  );
};
