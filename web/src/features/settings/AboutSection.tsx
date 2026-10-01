import React from "react";
import packageJson from "../../../package.json";

export const AboutSection: React.FC = () => {
  return (
    <section aria-labelledby="about-heading" className="space-y-4">
      <div className="space-y-1">
        <h2
          id="about-heading"
          className="text-base sm:text-lg font-bold text-primary"
        >
          About SuSagi
        </h2>
        <p className="text-xs sm:text-sm text-secondary leading-relaxed">
          Product release metadata, architecture specifications, and safety notices.
        </p>
      </div>

      <div className="rounded-xl bg-surface border border-subtle p-5 space-y-4">
        {/* System Meta Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pb-4 border-b border-subtle">
          <div>
            <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-0.5">
              Product Version
            </div>
            <div className="text-sm font-semibold text-primary">
              Version {packageJson.version}
            </div>
            <div className="text-xs text-secondary mt-0.5">
              SuSagi Web Companion
            </div>
          </div>

          <div>
            <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-0.5">
              Android Product Baseline
            </div>
            <div className="text-xs font-mono text-primary font-semibold truncate">
              95f09cc9c512ac4d35f17c6f8b94d7528ded5053
            </div>
            <div className="text-xs text-secondary mt-0.5">
              Host Core Verification Checkpoint
            </div>
          </div>

          <div>
            <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-0.5">
              Companion Environment
            </div>
            <div className="text-sm font-semibold text-primary">
              Next.js &middot; React &middot; TypeScript
            </div>
            <div className="text-xs text-secondary mt-0.5">
              Isolated workspace (/web)
            </div>
          </div>

          <div>
            <div className="text-[10px] font-semibold uppercase tracking-wider text-muted mb-0.5">
              Design Token System
            </div>
            <div className="text-sm font-semibold text-primary">
              High-Contrast Defense Standard
            </div>
            <div className="text-xs text-secondary mt-0.5">
              Designed for strong text contrast
            </div>
          </div>
        </div>

        {/* Mission Statement */}
        <div className="space-y-1.5 text-xs text-secondary leading-relaxed">
          <div className="font-semibold text-primary text-sm">
            Mission & Architecture
          </div>
          <p>
            SuSagi is built to safeguard vulnerable individuals and families against coercive fraud, voice cloning, urgency extortion, and unauthorized transfers. The Android host application handles native telephony interception and acoustic scoring, while this Web Companion provides accessible oversight, incident history review, and trusted-person identity verification.
          </p>
        </div>

        {/* Emergency Disclaimer Banner */}
        <div className="rounded-lg bg-surfaceElevated border border-subtle p-3.5 space-y-1 text-xs text-secondary leading-relaxed">
          <span className="font-semibold text-primary block">
            Emergency & Safety Notice:
          </span>
          <p>
            SuSagi Web Companion is an auxiliary defensive utility. SuSagi does not replace local emergency services, your bank&apos;s official fraud support, or other appropriate authorities.
          </p>
        </div>
      </div>
    </section>
  );
};
