"use client";

import React, { useEffect, useRef } from "react";

interface ReadyStepProps {
  hasRealGuardian: boolean;
  guardianStepSkipped: boolean;
  isPhoneConnected: boolean;
  practiceCompleted: boolean;
  onFinish: () => void;
}

export const ReadyStep: React.FC<ReadyStepProps> = ({
  hasRealGuardian,
  guardianStepSkipped,
  isPhoneConnected,
  practiceCompleted,
  onFinish,
}) => {
  const h1Ref = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    h1Ref.current?.focus({ preventScroll: true });
  }, []);

  const guardianStatus = hasRealGuardian
    ? "Added"
    : guardianStepSkipped
    ? "Set up later"
    : "Not connected";

  const checklist = [
    {
      label: "Web companion",
      status: "Ready",
      isPositive: true,
    },
    {
      label: "Guardian Circle",
      status: guardianStatus,
      isPositive: hasRealGuardian,
    },
    {
      label: "Phone protection",
      status: isPhoneConnected ? "Connected" : "Not connected",
      isPositive: isPhoneConnected,
    },
    {
      label: "Safety walkthrough",
      status: practiceCompleted ? "Complete" : "Incomplete",
      isPositive: practiceCompleted,
    },
  ];

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Title */}
      <div className="space-y-2">
        <h1
          ref={h1Ref}
          tabIndex={-1}
          className="text-2xl sm:text-3xl font-bold tracking-tight text-primary outline-none"
        >
          You&apos;re ready to use SuSagi.
        </h1>
        <p className="text-base text-secondary leading-relaxed">
          You know what to look for and what to do when a request feels unusual.
        </p>
      </div>

      {/* Readiness Checklist */}
      <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-4 shadow-xs">
        <div className="text-xs font-semibold uppercase tracking-wider text-muted">
          Readiness overview
        </div>

        <div className="divide-y divide-subtle">
          {checklist.map((item) => (
            <div
              key={item.label}
              className="py-3 flex items-center justify-between gap-3 text-sm"
            >
              <span className="font-medium text-primary">{item.label}</span>
              <span
                className={`text-xs font-semibold px-2.5 py-1 rounded-md border ${
                  item.isPositive
                    ? "bg-risk-low-soft text-risk-low border-risk-low"
                    : "bg-surfaceElevated text-secondary border-subtle"
                }`}
              >
                {item.status}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Primary Action */}
      <div className="pt-2">
        <button
          type="button"
          onClick={onFinish}
          className="w-full sm:w-auto min-h-[48px] px-8 py-3 rounded-xl bg-brand hover:bg-brandLight active:bg-brandDark text-white font-semibold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer shadow-sm text-center inline-flex items-center justify-center"
        >
          Go to Home
        </button>
      </div>
    </div>
  );
};
