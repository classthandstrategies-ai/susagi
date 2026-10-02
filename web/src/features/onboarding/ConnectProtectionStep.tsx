"use client";

import React, { useEffect, useRef } from "react";

interface ConnectProtectionStepProps {
  isDeviceConnected?: boolean;
  onContinue: () => void;
}

export const ConnectProtectionStep: React.FC<ConnectProtectionStepProps> = ({
  isDeviceConnected = false,
  onContinue,
}) => {
  const h1Ref = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    h1Ref.current?.focus({ preventScroll: true });
  }, []);

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Title */}
      <div className="space-y-2">
        <h1
          ref={h1Ref}
          tabIndex={-1}
          className="text-2xl sm:text-3xl font-bold tracking-tight text-primary outline-none"
        >
          Connect your phone for live protection.
        </h1>
        <p className="text-sm text-secondary leading-relaxed">
          Supported live protection and call-screening capabilities require a connected SuSagi phone.
        </p>
      </div>

      {/* Truthful Connection Status Card */}
      <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-4 shadow-xs">
        <div className="flex items-start gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-surfaceElevated border border-subtle flex items-center justify-center text-muted shrink-0">
            <svg
              className="w-5 h-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={1.75}
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M10.5 1.5H8.25A2.25 2.25 0 006 3.75v16.5a2.25 2.25 0 002.25 2.25h7.5A2.25 2.25 0 0018 20.25V3.75a2.25 2.25 0 00-2.25-2.25H13.5m-3 0V3h3V1.5m-3 0h3m-3 18.75h3"
              />
            </svg>
          </div>

          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span
                className={`w-2 h-2 rounded-full ${
                  isDeviceConnected ? "bg-risk-low" : "bg-risk-caution"
                }`}
                aria-hidden="true"
              />
              <span className="text-sm font-semibold text-primary">
                {isDeviceConnected ? "Phone connected" : "Phone not connected"}
              </span>
            </div>

            <p className="text-xs text-secondary leading-relaxed">
              {isDeviceConnected
                ? "Your phone is connected and actively sharing defense telemetry with this companion."
                : "Phone connection isn't available in this preview yet."}
            </p>
          </div>
        </div>

        <div className="pt-2 border-t border-subtle text-xs text-muted leading-relaxed">
          This web companion lets you review security activity and coordinate identity verification. Live call interception operates on your Android device.
        </div>
      </div>

      {/* Primary Action */}
      <div className="pt-2">
        <button
          type="button"
          onClick={onContinue}
          className="w-full sm:w-auto min-h-[48px] px-8 py-3 rounded-xl bg-brand hover:bg-brandLight active:bg-brandDark text-white font-semibold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brand cursor-pointer shadow-sm text-center inline-flex items-center justify-center"
        >
          Continue setup
        </button>
      </div>
    </div>
  );
};
