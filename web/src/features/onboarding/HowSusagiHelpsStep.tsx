"use client";

import React, { useEffect, useRef } from "react";

interface HowSusagiHelpsStepProps {
  onContinue: () => void;
}

export const HowSusagiHelpsStep: React.FC<HowSusagiHelpsStepProps> = ({
  onContinue,
}) => {
  const h1Ref = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    h1Ref.current?.focus({ preventScroll: true });
  }, []);

  const situations = [
    {
      id: "otp",
      context: "Someone asks for your OTP.",
      response:
        "SuSagi can surface the suspicious request and explain why it matters.",
    },
    {
      id: "family",
      context: "Someone claims to be family and asks for money.",
      response:
        "You can ask someone in your Guardian Circle to confirm whether the request is actually from them.",
    },
    {
      id: "link",
      context: "You receive an unfamiliar link.",
      response:
        "Inspect the link's address and structure before you open it.",
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
          How SuSagi helps
        </h1>
        <p className="text-sm text-secondary">
          Common moments where taking a pause protects you.
        </p>
      </div>

      {/* 3 Situations Stacked Vertically */}
      <div className="space-y-4">
        {situations.map((item) => (
          <div
            key={item.id}
            className="rounded-2xl bg-surface border border-subtle p-5 space-y-1.5 shadow-xs"
          >
            <h2 className="text-sm font-semibold text-primary">
              {item.context}
            </h2>
            <p className="text-sm text-secondary leading-relaxed">
              {item.response}
            </p>
          </div>
        ))}
      </div>

      {/* Closing Line */}
      <div className="p-4 rounded-xl bg-surfaceElevated border border-subtle">
        <p className="text-sm font-medium text-primary leading-relaxed">
          SuSagi helps you slow the moment down before you act.
        </p>
      </div>

      {/* Primary Action */}
      <div className="pt-2">
        <button
          type="button"
          onClick={onContinue}
          className="w-full sm:w-auto min-h-[48px] px-8 py-3 rounded-xl bg-brand hover:bg-brandLight active:bg-brandDark text-white font-semibold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer shadow-sm text-center inline-flex items-center justify-center"
        >
          Continue
        </button>
      </div>
    </div>
  );
};
