"use client";

import React from "react";
import packageJson from "../../../package.json";

export const AboutSection: React.FC = () => {
  return (
    <section aria-labelledby="about-heading" className="space-y-4">
      <div className="space-y-1">
        <h2
          id="about-heading"
          className="text-lg font-semibold text-primary"
        >
          About SuSagi
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Product release information, architecture specifications, and safety notices.
        </p>
      </div>

      <div className="rounded-2xl bg-surface border border-subtle p-6 space-y-5 shadow-sm">
        {/* System Meta Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pb-4 border-b border-subtle">
          <div>
            <div className="text-xs font-medium text-muted mb-0.5">
              Product version
            </div>
            <div className="text-sm font-semibold text-primary">
              SuSagi V1 (Web Companion v{packageJson.version})
            </div>
          </div>

          <div>
            <div className="text-xs font-medium text-muted mb-0.5">
              Android baseline
            </div>
            <div className="text-xs font-mono text-primary font-semibold truncate">
              95f09cc9c512ac4d35f17c6f8b94d7528ded5053
            </div>
          </div>

          <div>
            <div className="text-xs font-medium text-muted mb-0.5">
              Companion environment
            </div>
            <div className="text-sm font-semibold text-primary">
              Next.js · React · TypeScript
            </div>
          </div>

          <div>
            <div className="text-xs font-medium text-muted mb-0.5">
              Design foundation
            </div>
            <div className="text-sm font-semibold text-primary">
              Light consumer safety palette
            </div>
          </div>
        </div>

        {/* Mission Statement */}
        <div className="space-y-1.5 text-xs text-secondary leading-relaxed">
          <div className="font-semibold text-primary text-sm">
            Mission
          </div>
          <p>
            SuSagi is built to safeguard individuals and families against coercive fraud, voice cloning, urgency extortion, and unauthorized transfers. The Android host application handles native telephony screening and acoustic scoring, while this Web Companion provides accessible oversight, incident history review, and trusted-person identity verification.
          </p>
        </div>

        {/* Emergency Disclaimer Banner */}
        <div className="rounded-xl bg-surfaceElevated border border-subtle p-4 space-y-1 text-xs text-secondary leading-relaxed">
          <span className="font-semibold text-primary block">
            Emergency & safety notice:
          </span>
          <p>
            SuSagi Web Companion is an auxiliary defensive utility. SuSagi does not replace local emergency services, your bank&apos;s official fraud support, or law enforcement.
          </p>
        </div>
      </div>
    </section>
  );
};
