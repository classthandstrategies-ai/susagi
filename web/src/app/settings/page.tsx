import React from "react";
import { PermissionCard } from "@/components/cards/PermissionCard";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

export default function SettingsPage() {
  const permissions = [
    {
      title: "Microphone & Call Audio Buffer",
      description:
        "Required on Android device for on-device real-time scam acoustic analysis.",
      status: "GRANTED" as const,
    },
    {
      title: "Notification Scanner Service",
      description:
        "Detects suspicious OTP codes and urgent money transfer alerts across messaging apps.",
      status: "GRANTED" as const,
    },
    {
      title: "Call Screening & Phone State",
      description:
        "Allows autonomous screening of unknown and spoofed numbers before call pickup.",
      status: "GRANTED" as const,
    },
    {
      title: "Guardian Network Cloud Relay",
      description:
        "Optional end-to-end encrypted relay for multi-party identity challenges.",
      status: "NOT_CONFIGURED" as const,
    },
  ];

  return (
    <div className="space-y-6 max-w-3xl">
      <OfflineBanner message="Phase W1 Product Shell — System settings & companion preferences." />

      <header className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-primary">
          Companion Settings & Permissions
        </h1>
        <p className="text-sm text-secondary">
          Configure protection thresholds, permission access, and device pairing.
        </p>
      </header>

      {/* Permissions Section */}
      <section aria-labelledby="permissions-heading" className="space-y-3">
        <h2 id="permissions-heading" className="text-sm font-semibold uppercase tracking-wider text-muted">
          Device Permissions (Android Host)
        </h2>

        <div className="space-y-3">
          {permissions.map((perm) => (
            <PermissionCard
              key={perm.title}
              title={perm.title}
              description={perm.description}
              status={perm.status}
            />
          ))}
        </div>
      </section>

      {/* Pairing Section */}
      <section aria-labelledby="pairing-heading" className="space-y-3 pt-4 border-t border-subtle">
        <h2 id="pairing-heading" className="text-sm font-semibold uppercase tracking-wider text-muted">
          Companion Device Link
        </h2>

        <div className="rounded-xl bg-surface border border-subtle p-5 space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-semibold text-primary">
                Local Companion Pairing
              </h3>
              <p className="text-xs text-secondary mt-0.5">
                Status: Local Standby (Web Interface)
              </p>
            </div>
            <span className="text-xs px-2.5 py-1 rounded bg-brandSoft text-brand border border-brand/30 font-medium">
              READY
            </span>
          </div>

          <p className="text-xs text-muted leading-relaxed">
            The web companion operates without requiring proprietary cloud secrets or API keys. Live call screening telemetry will sync automatically when paired with the local SuSagi Android engine.
          </p>
        </div>
      </section>
    </div>
  );
}
