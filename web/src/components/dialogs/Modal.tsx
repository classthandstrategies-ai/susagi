"use client";

import React, { useEffect } from "react";
import { cn } from "@/lib/utils";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  className?: string;
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  className,
}) => {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      }
    };

    if (isOpen) {
      document.body.style.overflow = "hidden";
      window.addEventListener("keydown", handleKeyDown);
    }

    return () => {
      document.body.style.overflow = "unset";
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-void/80 backdrop-blur-sm animate-in fade-in duration-200"
    >
      <div
        className={cn(
          "w-full max-w-lg rounded-2xl bg-surfaceElevated border border-default shadow-2xl p-6 relative max-h-[90vh] overflow-y-auto",
          className
        )}
      >
        <div className="flex items-center justify-between gap-4 pb-4 border-b border-subtle mb-4">
          <h2
            id="modal-title"
            className="text-lg font-semibold text-primary truncate"
          >
            {title}
          </h2>

          <button
            type="button"
            onClick={onClose}
            aria-label="Close dialog"
            className="w-8 h-8 rounded-lg bg-surface border border-subtle hover:border-default flex items-center justify-center text-muted hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight"
          >
            <svg
              className="w-4 h-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
              aria-hidden="true"
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div>{children}</div>
      </div>
    </div>
  );
};
