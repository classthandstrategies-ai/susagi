"use client";

import React, { useEffect, useRef, useState } from "react";
import { PRACTICE_SCENARIO } from "./onboardingPracticeScenario";
import { Modal } from "@/components/dialogs/Modal";

interface PracticeScenarioStepProps {
  onComplete: () => void;
}

export const PracticeScenarioStep: React.FC<PracticeScenarioStepProps> = ({
  onComplete,
}) => {
  const h1Ref = useRef<HTMLHeadingElement>(null);
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);

  useEffect(() => {
    h1Ref.current?.focus({ preventScroll: true });
  }, []);

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Top Practice Badges */}
      <div className="space-y-1.5">
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-[11px] font-bold px-2.5 py-0.5 rounded bg-brandSoft text-brand border border-brand/20 tracking-wider uppercase">
            {PRACTICE_SCENARIO.title}
          </span>
          <span className="text-xs font-medium text-muted">
            {PRACTICE_SCENARIO.notice}
          </span>
        </div>
        <p className="text-sm text-secondary">
          Experience how SuSagi flags an urgent request before you respond.
        </p>
      </div>

      {/* Scenario Prompt */}
      <div className="p-4 rounded-xl bg-surfaceElevated border border-subtle">
        <div className="text-xs font-semibold text-muted uppercase tracking-wider mb-1">
          Scenario
        </div>
        <p className="text-sm font-medium text-primary leading-relaxed">
          {PRACTICE_SCENARIO.scenarioText}
        </p>
      </div>

      {/* Risk Assessment Box */}
      <div className="rounded-2xl bg-surface border border-subtle p-5 sm:p-6 space-y-5 shadow-xs">
        {/* Risk Level Header */}
        <div className="flex items-center justify-between gap-3 border-b border-subtle pb-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span
                className="w-2.5 h-2.5 rounded-full bg-risk-high"
                aria-hidden="true"
              />
              <span className="text-xs font-bold text-risk-high tracking-wider uppercase">
                {PRACTICE_SCENARIO.riskLevel} RISK
              </span>
            </div>
            <h1
              ref={h1Ref}
              tabIndex={-1}
              className="text-lg sm:text-xl font-bold tracking-tight text-primary outline-none"
            >
              {PRACTICE_SCENARIO.headline}
            </h1>
          </div>
        </div>

        {/* Explanation */}
        <p className="text-sm text-secondary leading-relaxed">
          {PRACTICE_SCENARIO.explanation}
        </p>

        {/* Why SuSagi is concerned */}
        <div className="space-y-2">
          <h2 className="text-xs font-semibold text-primary uppercase tracking-wider">
            Why SuSagi is concerned:
          </h2>
          <ul className="space-y-1.5 text-sm text-secondary">
            {PRACTICE_SCENARIO.concerns.map((concern, idx) => (
              <li key={idx} className="flex items-center gap-2">
                <span className="text-risk-high text-base leading-none">•</span>
                <span>{concern}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* What to do */}
        <div className="p-4 rounded-xl bg-risk-high-soft border border-risk-high-border space-y-1">
          <div className="text-xs font-bold text-risk-high uppercase tracking-wider">
            What to do:
          </div>
          <p className="text-sm font-medium text-primary">
            {PRACTICE_SCENARIO.safetyGuidance}
          </p>
        </div>

        {/* Identity CTA */}
        <div className="pt-2">
          <button
            type="button"
            onClick={() => setIsPreviewModalOpen(true)}
            className="w-full sm:w-auto min-h-[48px] px-5 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-default text-xs font-semibold text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer inline-flex items-center justify-center gap-2"
          >
            <svg
              className="w-4 h-4 text-secondary"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z"
              />
            </svg>
            <span>Preview identity check</span>
          </button>
        </div>
      </div>

      {/* Primary Final Action */}
      <div className="pt-2">
        <button
          type="button"
          onClick={onComplete}
          className="w-full sm:w-auto min-h-[48px] px-8 py-3 rounded-xl bg-brand hover:bg-brandLight active:bg-brandDark text-white font-semibold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer shadow-sm text-center inline-flex items-center justify-center"
        >
          I understand
        </button>
      </div>

      {/* Practice Identity Check Modal Preview */}
      {isPreviewModalOpen && (
        <Modal
          isOpen={true}
          title="Identity Check Preview"
          onClose={() => setIsPreviewModalOpen(false)}
        >
          <div className="space-y-4">
            <div className="p-3.5 rounded-xl bg-surfaceElevated border border-subtle text-xs font-medium text-secondary">
              Practice preview — no verification request was sent.
            </div>

            <p className="text-sm text-secondary leading-relaxed">
              In a live situation when connected to SuSagi verification services,
              you could send an out-of-band identity check to someone in your
              Guardian Circle to independently confirm the caller&apos;s claim.
            </p>

            <div className="pt-2 flex justify-end">
              <button
                type="button"
                onClick={() => setIsPreviewModalOpen(false)}
                className="min-h-[48px] px-6 py-2.5 rounded-xl bg-brand hover:bg-brandLight text-white font-medium text-xs transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer inline-flex items-center justify-center"
              >
                Close preview
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
