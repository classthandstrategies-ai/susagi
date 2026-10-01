"use client";

import React from "react";

interface ServiceItem {
  id: string;
  name: string;
  status: "CONNECTED" | "STANDBY" | "PHONE_ONLY";
  statusLabel: string;
  description: string;
}

const SERVICES: ServiceItem[] = [
  {
    id: "phone-connection",
    name: "Android phone connection",
    status: "STANDBY",
    statusLabel: "Standby",
    description: "Pairs with your phone over a secure local channel to receive call alerts.",
  },
  {
    id: "call-screening",
    name: "Live call screening",
    status: "PHONE_ONLY",
    statusLabel: "Runs on phone",
    description: "Speech analysis and call protection operate directly on your Android phone.",
  },
  {
    id: "activity-sync",
    name: "Activity history sync",
    status: "STANDBY",
    statusLabel: "Local companion",
    description: "Safety audit records and incident summaries saved in companion storage.",
  },
  {
    id: "guardian-network",
    name: "Guardian Circle",
    status: "STANDBY",
    statusLabel: "Ready",
    description: "Family identity challenge service ready for outgoing verification requests.",
  },
];

export const CompanionStatusSection: React.FC = () => {
  return (
    <section aria-labelledby="companion-status-heading" className="space-y-4">
      <div className="space-y-1">
        <h2
          id="companion-status-heading"
          className="text-lg font-semibold text-primary"
        >
          Connection status
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Current connection state between this web companion and your Android device.
        </p>
      </div>

      <div className="rounded-2xl bg-surface border border-subtle divide-y divide-subtle shadow-sm overflow-hidden">
        {SERVICES.map((service) => (
          <div
            key={service.id}
            className="p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
          >
            <div className="space-y-0.5">
              <div className="text-sm font-semibold text-primary">
                {service.name}
              </div>
              <p className="text-xs text-secondary leading-relaxed">
                {service.description}
              </p>
            </div>
            <div className="shrink-0 self-start sm:self-auto">
              <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-surfaceElevated border border-subtle text-secondary">
                {service.statusLabel}
              </span>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};
