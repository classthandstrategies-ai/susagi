import React from "react";
import Link from "next/link";

interface QuickActionsProps {
  onOpenLinkCheck: () => void;
}

export const QuickActions: React.FC<QuickActionsProps> = ({
  onOpenLinkCheck,
}) => {
  return (
    <section aria-labelledby="quick-actions-title" className="space-y-3">
      <h2
        id="quick-actions-title"
        className="text-xs font-semibold uppercase tracking-wider text-muted"
      >
        Quick Actions
      </h2>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
        <Link
          href="/live"
          className="rounded-xl bg-surface border border-subtle hover:border-default p-3 flex flex-col items-center justify-center text-center group transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
        >
          <div className="w-8 h-8 rounded-lg bg-risk-critical-soft text-risk-critical flex items-center justify-center mb-1.5 group-hover:scale-105 transition-transform">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />
            </svg>
          </div>
          <span className="text-xs font-semibold text-primary group-hover:text-brand transition-colors">
            Live Defense
          </span>
          <span className="text-[10px] text-muted mt-0.5">Call Monitor</span>
        </Link>

        <button
          type="button"
          onClick={onOpenLinkCheck}
          className="rounded-xl bg-surface border border-subtle hover:border-default p-3 flex flex-col items-center justify-center text-center group transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
        >
          <div className="w-8 h-8 rounded-lg bg-brandSoft text-brand flex items-center justify-center mb-1.5 group-hover:scale-105 transition-transform">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
            </svg>
          </div>
          <span className="text-xs font-semibold text-primary group-hover:text-brand transition-colors">
            Check Link
          </span>
          <span className="text-[10px] text-muted mt-0.5">URL Inspector</span>
        </button>

        <Link
          href="/activity"
          className="rounded-xl bg-surface border border-subtle hover:border-default p-3 flex flex-col items-center justify-center text-center group transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
        >
          <div className="w-8 h-8 rounded-lg bg-surfaceHighlight text-secondary flex items-center justify-center mb-1.5 group-hover:scale-105 transition-transform">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5M9 11.25v1.5M12 9v3.75m3-6v6" />
            </svg>
          </div>
          <span className="text-xs font-semibold text-primary group-hover:text-brand transition-colors">
            Activity Log
          </span>
          <span className="text-[10px] text-muted mt-0.5">Audit History</span>
        </Link>

        <Link
          href="/guardians"
          className="rounded-xl bg-surface border border-subtle hover:border-default p-3 flex flex-col items-center justify-center text-center group transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
        >
          <div className="w-8 h-8 rounded-lg bg-brandSoft text-brand flex items-center justify-center mb-1.5 group-hover:scale-105 transition-transform">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
            </svg>
          </div>
          <span className="text-xs font-semibold text-primary group-hover:text-brand transition-colors">
            Guardians
          </span>
          <span className="text-[10px] text-muted mt-0.5">Family Network</span>
        </Link>
      </div>
    </section>
  );
};
