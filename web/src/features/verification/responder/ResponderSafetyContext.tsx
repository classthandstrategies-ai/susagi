import React from "react";

export const ResponderSafetyContext: React.FC = () => {
  return (
    <div className="rounded-xl bg-surfaceElevated p-4 border border-subtle space-y-2 text-xs text-secondary leading-relaxed">
      <div className="flex items-center gap-2 text-primary font-semibold">
        <svg className="w-4 h-4 text-brand shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
        </svg>
        <span>Why you are receiving this challenge</span>
      </div>
      <p>
        A contact in your circle was approached by someone claiming to represent an institution or family member. They are asking you to confirm whether this request genuinely came from you.
      </p>
      <p className="text-muted text-[11px]">
        If you did not initiate or authorize this request, select &ldquo;No, Not Me&rdquo; immediately.
      </p>
    </div>
  );
};
