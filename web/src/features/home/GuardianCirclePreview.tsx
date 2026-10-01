import React from "react";
import Link from "next/link";
import { GuardianContact } from "@/types/guardian";

interface GuardianCirclePreviewProps {
  guardians: GuardianContact[];
  isFixtureMode?: boolean;
}

export const GuardianCirclePreview: React.FC<GuardianCirclePreviewProps> = ({
  guardians,
  isFixtureMode = false,
}) => {
  return (
    <section aria-labelledby="guardian-preview-title" className="space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h2
            id="guardian-preview-title"
            className="text-xs font-semibold uppercase tracking-wider text-muted"
          >
            Guardian Circle
          </h2>
          {isFixtureMode && (
            <span className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-brandSoft text-brand border border-brand/30 uppercase">
              FIXTURE
            </span>
          )}
        </div>
        <Link
          href="/guardians"
          className="text-xs font-semibold text-brand hover:text-brandLight transition-colors"
        >
          Manage Circle →
        </Link>
      </div>

      {guardians.length === 0 ? (
        <div className="rounded-xl bg-surface border border-subtle p-5 text-center space-y-2">
          <div className="w-9 h-9 rounded-full bg-surfaceElevated border border-subtle mx-auto flex items-center justify-center text-muted">
            <svg
              className="w-4 h-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={1.75}
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z"
              />
            </svg>
          </div>
          <h3 className="text-sm font-semibold text-primary">
            No Guardian data connected
          </h3>
          <p className="text-xs text-secondary leading-relaxed max-w-xs mx-auto">
            Trusted people will appear here when Guardian Circle is connected.
          </p>
          <div className="pt-1">
            <Link
              href="/guardians"
              className="inline-flex items-center text-xs font-semibold text-brand hover:text-brandLight"
            >
              View Guardians →
            </Link>
          </div>
        </div>
      ) : (
        <div className="space-y-2">
          {guardians.slice(0, 3).map((guardian) => (
            <div
              key={guardian.id}
              className="rounded-xl bg-surface border border-subtle p-3 flex items-center justify-between gap-3"
            >
              <div className="flex items-center gap-2.5 min-w-0">
                <div
                  className="w-8 h-8 rounded-full bg-brandSoft text-brand font-bold text-xs flex items-center justify-center shrink-0 border border-brand/20"
                  aria-hidden="true"
                >
                  {guardian.avatarInitials}
                </div>
                <div className="min-w-0">
                  <div className="text-xs font-semibold text-primary truncate">
                    {guardian.name}
                  </div>
                  <div className="text-[10px] text-muted truncate">
                    {guardian.relationship}
                  </div>
                </div>
              </div>

              <span className="text-[10px] font-mono text-muted shrink-0">
                {guardian.phone}
              </span>
            </div>
          ))}
        </div>
      )}
    </section>
  );
};
