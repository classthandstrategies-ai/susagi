"use client";

import React, { useState } from "react";
import Link from "next/link";
import { verificationService } from "@/services/verificationService";
import { VerificationStatus } from "@/types/verification";
import { StatusBanner } from "@/components/feedback/StatusBanner";
import { PrimarySafetyAction } from "@/components/actions/PrimarySafetyAction";
import { SecondarySafetyAction } from "@/components/actions/SecondarySafetyAction";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

export default function VerificationResponderPage() {
  const [sessionId, setSessionId] = useState("sess-challenge-4821");
  const [status, setStatus] = useState<VerificationStatus | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleRespond = async (accept: boolean) => {
    setLoading(true);
    try {
      const result = await verificationService.respondToVerification(
        sessionId,
        accept
      );
      setStatus(result.status);
      setStatusMessage(result.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <OfflineBanner message="Phase W1 Product Shell — Responder network operates in offline-first mode." />

      <div className="flex items-center justify-between">
        <header className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight text-primary">
            Verification Responder
          </h1>
          <p className="text-sm text-secondary">
            Respond to incoming identity challenges from your family members or circle.
          </p>
        </header>

        <Link
          href="/verification"
          className="text-xs text-brand hover:text-brandLight font-medium"
        >
          ← Back to Requester
        </Link>
      </div>

      {status && (
        <StatusBanner
          variant={status === "UNAVAILABLE" ? "warning" : "info"}
          title={`Responder Status: ${status}`}
          message={statusMessage || ""}
        />
      )}

      <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-4">
        <div className="space-y-1.5">
          <label
            htmlFor="session-id-input"
            className="text-xs font-semibold uppercase tracking-wider text-muted"
          >
            Challenge Session ID
          </label>
          <input
            id="session-id-input"
            type="text"
            value={sessionId}
            onChange={(e) => setSessionId(e.target.value)}
            className="w-full px-4 py-2.5 rounded-xl bg-surfaceElevated border border-default text-primary font-mono text-sm focus-visible:ring-2 focus-visible:ring-brandLight focus:outline-none"
            placeholder="sess-..."
          />
        </div>

        <div className="p-4 rounded-xl bg-surfaceElevated border border-subtle space-y-2">
          <div className="text-xs uppercase font-semibold text-muted">
            Incoming Challenge Simulation
          </div>
          <p className="text-sm text-primary font-medium">
            &ldquo;Are you currently on call with the Bank asking for our debit card OTP?&rdquo;
          </p>
          <p className="text-xs text-secondary">
            Requester: Papa (+91 98101 XXXXX) • 2 minutes ago
          </p>
        </div>

        <div className="flex flex-col sm:flex-row gap-3 pt-2">
          <PrimarySafetyAction
            label={loading ? "Responding..." : "Yes, This Is Legitimate"}
            onClick={() => handleRespond(true)}
            disabled={loading}
          />
          <SecondarySafetyAction
            label="No! This Is A Scam"
            onClick={() => handleRespond(false)}
            disabled={loading}
          />
        </div>

        <div className="pt-2 text-xs text-muted leading-relaxed">
          <strong>Backend Integrity:</strong> No fake verification tokens are minted. Backend-dependent runtime actions resolve honestly to <code className="text-risk-caution">UNAVAILABLE</code>.
        </div>
      </div>
    </div>
  );
}
