"use client";

import React, { useState } from "react";
import Link from "next/link";
import { verificationService } from "@/services/verificationService";
import { VerificationStatus } from "@/types/verification";
import { StatusBanner } from "@/components/feedback/StatusBanner";
import { PrimarySafetyAction } from "@/components/actions/PrimarySafetyAction";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

export default function VerificationRequesterPage() {
  const [phone, setPhone] = useState("+91 98101 23456");
  const [claim, setClaim] = useState("Caller claims to be Bank Officer regarding KYC update");
  const [status, setStatus] = useState<VerificationStatus | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const result = await verificationService.requestVerification(phone, claim);
      setStatus(result.status);
      setStatusMessage(result.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <OfflineBanner message="Phase W1 Product Shell — Remote verification services honestly resolve to UNAVAILABLE." />

      <div className="flex items-center justify-between">
        <header className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight text-primary">
            Request Identity Verification
          </h1>
          <p className="text-sm text-secondary">
            Send an out-of-band cryptographic challenge to verify the caller&apos;s real identity.
          </p>
        </header>

        <Link
          href="/verification/respond"
          className="text-xs text-brand hover:text-brandLight font-medium"
        >
          Go to Responder →
        </Link>
      </div>

      {/* Result Status Banner */}
      {status && (
        <StatusBanner
          variant={status === "UNAVAILABLE" ? "warning" : "info"}
          title={`Verification Status: ${status}`}
          message={statusMessage || ""}
        />
      )}

      {/* Verification Requester Form */}
      <form
        onSubmit={handleSubmit}
        className="rounded-2xl bg-surface border border-subtle p-6 space-y-4"
      >
        <div className="space-y-1.5">
          <label htmlFor="phone-input" className="text-xs font-semibold uppercase tracking-wider text-muted">
            Recipient / Guardian Phone
          </label>
          <input
            id="phone-input"
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            className="w-full px-4 py-2.5 rounded-xl bg-surfaceElevated border border-default text-primary font-mono text-sm focus-visible:ring-2 focus-visible:ring-brandLight focus:outline-none"
            placeholder="+91 9XXXX XXXXX"
            required
          />
        </div>

        <div className="space-y-1.5">
          <label htmlFor="claim-input" className="text-xs font-semibold uppercase tracking-wider text-muted">
            Caller Claim or Verification Purpose
          </label>
          <textarea
            id="claim-input"
            value={claim}
            onChange={(e) => setClaim(e.target.value)}
            rows={3}
            className="w-full px-4 py-2.5 rounded-xl bg-surfaceElevated border border-default text-primary text-sm focus-visible:ring-2 focus-visible:ring-brandLight focus:outline-none resize-none"
            placeholder="e.g. Caller claims to be bank manager requiring urgent confirmation"
            required
          />
        </div>

        <div className="pt-2">
          <PrimarySafetyAction
            type="submit"
            label={loading ? "Sending Challenge..." : "Send Verification Challenge"}
            disabled={loading}
          />
        </div>

        <div className="pt-2 text-xs text-muted leading-relaxed">
          <strong>Notice:</strong> Remote verification requires paired Guardian cloud integration. In W1 shell, this request resolves honestly to <code className="text-risk-caution">UNAVAILABLE</code> without fabricating success.
        </div>
      </form>
    </div>
  );
}
