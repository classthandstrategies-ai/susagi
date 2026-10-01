"use client";

import React, { useState } from "react";
import { Modal } from "@/components/dialogs/Modal";
import { RiskBadge } from "@/components/risk/RiskBadge";
import { PrimarySafetyAction } from "@/components/actions/PrimarySafetyAction";
import { SecondarySafetyAction } from "@/components/actions/SecondarySafetyAction";
import { RiskLevel } from "@/types/risk";

interface LinkCheckDialogProps {
  isOpen: boolean;
  onClose: () => void;
  isFixtureMode?: boolean;
  initialUrl?: string;
}

interface ParsedUrlInfo {
  rawUrl: string;
  protocol: string;
  hostname: string;
  pathname: string;
}

export const LinkCheckDialog: React.FC<LinkCheckDialogProps> = ({
  isOpen,
  onClose,
  isFixtureMode = false,
  initialUrl = "",
}) => {
  const [inputUrl, setInputUrl] = useState(initialUrl);
  const [prevInitialUrl, setPrevInitialUrl] = useState(initialUrl);

  if (initialUrl !== prevInitialUrl) {
    setPrevInitialUrl(initialUrl);
    setInputUrl(initialUrl);
  }

  const [syntaxError, setSyntaxError] = useState<string | null>(null);
  const [analyzedUrl, setAnalyzedUrl] = useState<ParsedUrlInfo | null>(null);
  const [copied, setCopied] = useState(false);
  const [demoRiskLevel, setDemoRiskLevel] = useState<RiskLevel>("CAUTION");

  const handleValidate = (e: React.FormEvent) => {
    e.preventDefault();
    setSyntaxError(null);
    setCopied(false);

    const trimmed = inputUrl.trim();
    if (!trimmed) {
      setSyntaxError("Please enter or paste a web link to inspect.");
      return;
    }

    try {
      // Add protocol if user omitted it
      const urlToParse = trimmed.startsWith("http://") || trimmed.startsWith("https://")
        ? trimmed
        : `https://${trimmed}`;

      const parsed = new URL(urlToParse);
      setAnalyzedUrl({
        rawUrl: trimmed,
        protocol: parsed.protocol,
        hostname: parsed.hostname,
        pathname: parsed.pathname === "/" ? "" : parsed.pathname,
      });
    } catch {
      setSyntaxError(
        "Invalid web link format. Please verify the URL syntax (e.g., https://example.com/login)."
      );
      setAnalyzedUrl(null);
    }
  };

  const handleCopy = async () => {
    if (!analyzedUrl) return;
    try {
      await navigator.clipboard.writeText(analyzedUrl.rawUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Fallback
    }
  };

  const handleReset = () => {
    setInputUrl("");
    setSyntaxError(null);
    setAnalyzedUrl(null);
    setCopied(false);
  };

  const handleClose = () => {
    handleReset();
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Link inspector">
      <div className="space-y-4">
        <p className="text-sm text-secondary leading-relaxed">
          Inspect suspicious links before opening them or entering credentials.
        </p>

        {/* Input Form */}
        <form onSubmit={handleValidate} className="space-y-3">
          <div className="space-y-1.5">
            <label
              htmlFor="link-check-input"
              className="text-xs font-medium text-secondary"
            >
              Web link or URL
            </label>
            <div className="relative">
              <input
                id="link-check-input"
                type="text"
                value={inputUrl}
                onChange={(e) => {
                  setInputUrl(e.target.value);
                  if (syntaxError) setSyntaxError(null);
                }}
                placeholder="https://suspicious-bank-link.com/verify"
                className="w-full px-4 py-2.5 rounded-xl bg-surfaceElevated border border-default text-primary font-mono text-sm placeholder:text-muted/60 focus-visible:ring-2 focus-visible:ring-brandLight focus:outline-none"
              />
              {inputUrl && (
                <button
                  type="button"
                  onClick={() => setInputUrl("")}
                  className="absolute right-3 top-2.5 text-xs text-muted hover:text-primary min-h-[32px] px-2 flex items-center"
                  aria-label="Clear link input"
                >
                  Clear
                </button>
              )}
            </div>
            {syntaxError && (
              <p role="alert" className="text-xs text-risk-caution mt-1">
                {syntaxError}
              </p>
            )}
          </div>

          <div className="flex gap-2">
            <PrimarySafetyAction
              type="submit"
              label="Inspect link"
              className="flex-1"
            />
            {analyzedUrl && (
              <SecondarySafetyAction
                type="button"
                label="Reset"
                onClick={handleReset}
              />
            )}
          </div>
        </form>

        {/* Inspection Results Section */}
        {analyzedUrl && (
          <div className="rounded-xl bg-surface border border-subtle p-4 space-y-4 pt-4 mt-2">
            {/* Parsed Syntax Breakdown */}
            <div className="space-y-2">
              <div className="text-xs font-medium text-secondary">
                Link structure
              </div>
              <div className="bg-surfaceElevated rounded-xl p-3.5 space-y-2 font-mono text-xs border border-subtle">
                <div className="flex items-center justify-between text-secondary">
                  <span className="text-muted">Host / Domain:</span>
                  <span className="text-primary font-semibold">
                    {analyzedUrl.hostname}
                  </span>
                </div>
                <div className="flex items-center justify-between text-secondary">
                  <span className="text-muted">Protocol:</span>
                  <span className={analyzedUrl.protocol === "https:" ? "text-risk-low font-medium" : "text-risk-caution font-medium"}>
                    {analyzedUrl.protocol}
                  </span>
                </div>
                {analyzedUrl.pathname && (
                  <div className="flex items-center justify-between text-secondary truncate">
                    <span className="text-muted">Path:</span>
                    <span className="text-muted truncate max-w-[200px]">
                      {analyzedUrl.pathname}
                    </span>
                  </div>
                )}
              </div>
            </div>

            {/* Truthful Production Service Availability */}
            <div className="rounded-xl bg-surfaceElevated border border-subtle p-3.5 space-y-2">
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-brand" aria-hidden="true" />
                <span className="text-xs font-semibold text-primary">
                  Automated risk analysis is not connected yet
                </span>
              </div>
              <p className="text-xs text-secondary leading-relaxed">
                SuSagi does not produce simulated security scores on the web companion without an authoritative risk engine. Never enter sensitive passwords or OTPs on links sent via unsolicited SMS or messaging.
              </p>
            </div>

            {/* Development-Only Demo Risk State Preview */}
            {isFixtureMode && (
              <div className="rounded-xl border border-brand/20 bg-brandSoft/20 p-3 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-brand">
                    Development fixture preview
                  </span>
                  <RiskBadge level={demoRiskLevel} size="sm" />
                </div>
                <p className="text-xs text-muted">
                  Fixture preview of semantic visual states. (Select level for design verification):
                </p>
                <div className="flex gap-1.5 pt-1">
                  {(["LOW", "CAUTION", "HIGH", "CRITICAL"] as RiskLevel[]).map((level) => (
                    <button
                      key={level}
                      type="button"
                      onClick={() => setDemoRiskLevel(level)}
                      className={`text-[11px] px-2.5 py-1 rounded-lg font-medium transition-colors cursor-pointer ${
                        demoRiskLevel === level
                          ? "bg-brand text-white"
                          : "bg-surface text-secondary hover:text-primary border border-subtle"
                      }`}
                    >
                      {level}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Safe General User Actions */}
            <div className="space-y-2 pt-2 border-t border-subtle">
              <div className="text-xs font-medium text-secondary">
                Safe next steps
              </div>
              <ul className="text-xs text-secondary space-y-1.5 list-disc list-inside">
                <li>Search for the company or bank using your browser independently.</li>
                <li>Never share banking OTPs, debit card PINs, or UPI pins.</li>
                <li>Do not download apps or profile certificates prompted by unknown links.</li>
              </ul>
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-2 pt-2">
              <button
                type="button"
                onClick={handleCopy}
                className="flex-1 min-h-[44px] px-4 py-2 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
              >
                {copied ? "Link copied!" : "Copy link"}
              </button>
              <button
                type="button"
                onClick={handleClose}
                className="min-h-[44px] px-4 py-2 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle text-xs font-medium text-secondary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center"
              >
                Done
              </button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
};
