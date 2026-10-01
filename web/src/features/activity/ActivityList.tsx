import React from "react";
import { IncidentItem } from "@/types/activity";
import { IncidentCard } from "@/components/cards/IncidentCard";

interface ActivityListProps {
  incidents: IncidentItem[];
  fixtureKey?: string;
}

export const ActivityList: React.FC<ActivityListProps> = ({
  incidents,
  fixtureKey,
}) => {
  return (
    <div
      role="feed"
      aria-label="Security Activity Incidents"
      className="space-y-3"
    >
      {incidents.map((incident) => {
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
  );
};
