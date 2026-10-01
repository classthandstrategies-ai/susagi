import React from "react";

interface ServiceItem {
  id: string;
  name: string;
  status: "NOT_CONNECTED" | "STANDBY" | "UNAVAILABLE";
  statusLabel: string;
  description: string;
  badgeStyle: string;
}

const SERVICES: ServiceItem[] = [
  {
    id: "device-engine",
    name: "On-Device Defense Engine",
    status: "NOT_CONNECTED",
    statusLabel: "Not Connected",
    description:
      "Acoustic analysis and call protection operate directly on the host SuSagi Android device.",
    badgeStyle: "bg-surfaceElevated text-muted border-subtle",
  },
  {
    id: "call-screening",
    name: "Live Call Screening Relay",
    status: "NOT_CONNECTED",
    statusLabel: "Not Connected",
    description:
      "Web browsers cannot intercept cellular telephony streams. Telemetry syncs when paired with an Android host.",
    badgeStyle: "bg-surfaceElevated text-muted border-subtle",
  },
  {
    id: "activity-sync",
    name: "Activity Ledger Sync",
    status: "STANDBY",
    statusLabel: "Standby",
    description:
      "Historical incident telemetry syncs when connected to an authorized host device.",
    badgeStyle: "bg-surfaceElevated text-secondary border-subtle",
  },
  {
    id: "guardian-network",
    name: "Guardian Circle Network",
    status: "NOT_CONNECTED",
    statusLabel: "Not Connected",
    description:
      "Multi-party relay for emergency contact notifications requires a connected guardian service.",
    badgeStyle: "bg-surfaceElevated text-muted border-subtle",
  },
  {
    id: "verification-service",
    name: "Identity Verification Service",
    status: "UNAVAILABLE",
    statusLabel: "Unavailable",
    description:
      "Trusted-person identity verification service is currently unavailable in this companion runtime.",
    badgeStyle: "bg-surfaceElevated text-muted border-subtle",
  },
];

export const CompanionStatusSection: React.FC = () => {
  return (
    <section aria-labelledby="companion-status-heading" className="space-y-4">
      <div className="space-y-1">
        <h2
          id="companion-status-heading"
          className="text-base sm:text-lg font-bold text-primary flex items-center gap-2"
        >
          <span>Companion Service Status</span>
          <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-surfaceElevated text-muted border border-subtle">
            STANDBY RUNTIME
          </span>
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Operational connectivity state between this browser companion and the SuSagi defense ecosystem.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-3">
        {SERVICES.map((service) => (
          <div
            key={service.id}
            className="rounded-xl bg-surface border border-subtle p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
          >
            <div className="space-y-1">
              <div className="text-sm font-semibold text-primary">
                {service.name}
              </div>
              <p className="text-xs text-secondary leading-relaxed max-w-xl">
                {service.description}
              </p>
            </div>
            <div className="shrink-0 self-start sm:self-auto">
              <span
                className={`text-xs px-2.5 py-1 rounded-md font-semibold border ${service.badgeStyle}`}
              >
                {service.statusLabel}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Boundary Notice */}
      <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 space-y-1 text-xs text-secondary leading-relaxed">
        <div className="font-semibold text-primary">
          Companion Architecture Boundary
        </div>
        <p>
          The SuSagi Web Companion serves as an auxiliary monitoring and responder interface. Telephony screening, acoustic risk assessment, and protective call termination operate exclusively on the physical Android host.
        </p>
      </div>
    </section>
  );
};
