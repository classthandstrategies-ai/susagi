"use client";

import React, { useEffect, useRef } from "react";
import { cn } from "@/lib/utils";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  className?: string;
}

const FOCUSABLE_SELECTOR =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  className,
}) => {
  const dialogRef = useRef<HTMLDivElement>(null);
  const previousActiveElementRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!isOpen) return;

    // 1. Remember previously focused element for restoration
    if (typeof document !== "undefined" && document.activeElement instanceof HTMLElement) {
      previousActiveElementRef.current = document.activeElement;
    }

    // 2. Lock body scroll
    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    // 3. Move initial focus into the dialog
    const focusTimeout = requestAnimationFrame(() => {
      if (!dialogRef.current) return;
      const focusables = dialogRef.current.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR);
      if (focusables.length > 0) {
        // Prefer first input or interactive element, or first focusable
        const firstInput = dialogRef.current.querySelector<HTMLElement>(
          'input:not([disabled]), textarea:not([disabled]), select:not([disabled])'
        );
        if (firstInput) {
          firstInput.focus();
        } else {
          focusables[0].focus();
        }
      } else {
        dialogRef.current.focus();
      }
    });

    // 4. Trap Tab / Shift+Tab and handle Escape
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        e.preventDefault();
        e.stopPropagation();
        onClose();
        return;
      }

      if (e.key === "Tab") {
        if (!dialogRef.current) return;

        const focusables = Array.from(
          dialogRef.current.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR)
        ).filter(
          (el) => el.offsetParent !== null || el.offsetWidth > 0 || el.offsetHeight > 0
        );

        if (focusables.length === 0) {
          e.preventDefault();
          return;
        }

        const firstElement = focusables[0];
        const lastElement = focusables[focusables.length - 1];

        if (e.shiftKey) {
          if (
            document.activeElement === firstElement ||
            !dialogRef.current.contains(document.activeElement)
          ) {
            e.preventDefault();
            lastElement.focus();
          }
        } else {
          if (
            document.activeElement === lastElement ||
            !dialogRef.current.contains(document.activeElement)
          ) {
            e.preventDefault();
            firstElement.focus();
          }
        }
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      cancelAnimationFrame(focusTimeout);
      document.body.style.overflow = originalOverflow;
      window.removeEventListener("keydown", handleKeyDown);

      // 5. Restore focus to opener
      if (
        previousActiveElementRef.current &&
        typeof previousActiveElementRef.current.focus === "function"
      ) {
        previousActiveElementRef.current.focus();
      }
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
      ref={dialogRef}
      tabIndex={-1}
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-void/80 backdrop-blur-sm animate-in fade-in duration-200 outline-none"
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
            className="w-8 h-8 rounded-lg bg-surface border border-subtle hover:border-default flex items-center justify-center text-muted hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer"
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
