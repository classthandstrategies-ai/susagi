import React from "react";

interface ResponderActionsProps {
  onConfirmLegitimate: () => void;
  onRejectScam: () => void;
  disabled?: boolean;
}

export const ResponderActions: React.FC<ResponderActionsProps> = ({
  onConfirmLegitimate,
  onRejectScam,
  disabled = false,
}) => {
  return (
    <div className="flex flex-col sm:flex-row gap-3 pt-2">
      {/* Yes, It's Me Action */}
      <button
        type="button"
        onClick={onConfirmLegitimate}
        disabled={disabled}
        className="flex-1 min-h-[52px] px-6 py-3.5 rounded-xl bg-risk-low text-void hover:opacity-90 font-bold text-sm transition-opacity focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
      >
        <svg className="w-5 h-5 text-void shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
        </svg>
        <span>Yes, It&apos;s Me (Authorized)</span>
      </button>

      {/* No, Not Me Action */}
      <button
        type="button"
        onClick={onRejectScam}
        disabled={disabled}
        className="flex-1 min-h-[52px] px-6 py-3.5 rounded-xl bg-risk-critical text-white hover:bg-red-700 font-bold text-sm transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
      >
        <svg className="w-5 h-5 text-white shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
        <span>No, Not Me (Decline)</span>
      </button>
    </div>
  );
};
