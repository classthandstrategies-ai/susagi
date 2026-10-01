import React from "react";
import Link from "next/link";

export const VerificationGuidance: React.FC = () => {
  return (
    <div className="space-y-4">
      <section
        aria-labelledby="guidance-heading"
        className="rounded-2xl bg-surface border border-subtle p-5 space-y-3"
      >
        <h3
          id="guidance-heading"
          className="text-xs font-semibold uppercase tracking-wider text-muted"
        >
          Protective Guidance
        </h3>

        <ul className="space-y-2.5 text-xs text-secondary leading-relaxed">
          <li className="flex items-start gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-brand mt-1.5 shrink-0" aria-hidden="true" />
            <span>Never share passwords, banking PINs, or one-time passwords (OTPs) over unverified phone calls.</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-brand mt-1.5 shrink-0" aria-hidden="true" />
            <span>If an unknown caller manufactures urgency around an alleged legal or hospital emergency, contact the family member directly through known numbers.</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-brand mt-1.5 shrink-0" aria-hidden="true" />
            <span>Organizations and banks do not demand immediate money transfers via personal UPI QR codes.</span>
          </li>
        </ul>
      </section>

      <div className="flex flex-col sm:flex-row gap-2.5">
        <Link
          href="/guardians"
          className="flex-1 min-h-[48px] px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center"
        >
          View Guardian Circle
        </Link>
        <Link
          href="/live"
          className="flex-1 min-h-[48px] px-4 py-2.5 rounded-xl bg-surfaceElevated hover:bg-surfaceHighlight border border-subtle text-xs font-semibold text-secondary hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight inline-flex items-center justify-center text-center"
        >
          Back to Live Defense
        </Link>
      </div>
    </div>
  );
};
