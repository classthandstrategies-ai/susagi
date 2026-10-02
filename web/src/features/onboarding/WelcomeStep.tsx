"use client";

import React, { useEffect, useRef } from "react";

interface WelcomeStepProps {
  onSetup: () => void;
  onExploreFirst: () => void;
}

export const WelcomeStep: React.FC<WelcomeStepProps> = ({
  onSetup,
  onExploreFirst,
}) => {
  const h1Ref = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    h1Ref.current?.focus({ preventScroll: true });
  }, []);

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Icon / Calm Visual Anchor */}
      <div className="w-12 h-12 rounded-2xl bg-surfaceElevated border border-subtle flex items-center justify-center text-primary shadow-xs">
        <svg
          className="w-6 h-6"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={1.75}
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.002A11.959 11.959 0 0112 2.714z"
          />
        </svg>
      </div>

      {/* Copy */}
      <div className="space-y-3">
        <h1
          ref={h1Ref}
          tabIndex={-1}
          className="text-2xl sm:text-3xl font-bold tracking-tight text-primary outline-none"
        >
          Stay one step ahead of suspicious requests.
        </h1>
        <p className="text-base text-secondary leading-relaxed max-w-lg">
          SuSagi helps you review suspicious activity, verify who you&apos;re
          speaking with, and inspect unfamiliar links.
        </p>
      </div>

      {/* Actions */}
      <div className="pt-2 flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
        <button
          type="button"
          onClick={onSetup}
          className="min-h-[48px] px-6 py-3 rounded-xl bg-brand hover:bg-brandLight active:bg-brandDark text-white font-semibold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer shadow-sm text-center inline-flex items-center justify-center"
        >
          Set up SuSagi
        </button>

        <button
          type="button"
          onClick={onExploreFirst}
          className="min-h-[48px] px-5 py-3 rounded-xl bg-surface hover:bg-surfaceElevated border border-subtle hover:border-default text-secondary hover:text-primary font-medium text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer text-center inline-flex items-center justify-center"
        >
          Explore first
        </button>
      </div>
    </div>
  );
};
