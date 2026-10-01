"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

export const TopBar: React.FC = () => {
  const pathname = usePathname();

  const getPageTitle = (path: string): string => {
    if (path === "/") return "Companion Home";
    if (path.startsWith("/protect")) return "Protection Shield";
    if (path.startsWith("/live")) return "Live Call Defense";
    if (path.startsWith("/activity")) return "Activity & Security Audit";
    if (path.startsWith("/guardians")) return "Guardian Circle";
    if (path.startsWith("/verification/respond")) return "Verification Responder";
    if (path.startsWith("/verification")) return "Identity Verification";
    if (path.startsWith("/settings")) return "Settings & Preferences";
    return "SuSagi";
  };

  return (
    <header className="h-16 px-4 sm:px-6 bg-surface/80 backdrop-blur-md border-b border-subtle sticky top-0 z-30 flex items-center justify-between gap-4">
      {/* Left: Mobile Brand / Page Title */}
      <div className="flex items-center gap-3">
        {/* Mobile brand logo */}
        <div className="md:hidden flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-brand flex items-center justify-center text-white shrink-0">
            <svg
              className="w-4 h-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2.5}
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.002A11.959 11.959 0 0112 2.714z"
              />
            </svg>
          </div>
          <span className="font-bold text-base text-primary tracking-tight">
            SuSagi
          </span>
        </div>

        {/* Desktop title / Mobile breadcrumb */}
        <div className="hidden md:block">
          <h1 className="text-base sm:text-lg font-semibold text-primary">
            {getPageTitle(pathname)}
          </h1>
        </div>
      </div>

      {/* Right: Protection Mode Badge & Mobile Settings Link */}
      <div className="flex items-center gap-3">
        <div
          role="status"
          aria-label="Companion status: Local Standby"
          className="flex items-center gap-2 px-2.5 py-1 rounded-full bg-surfaceElevated border border-subtle text-xs text-secondary font-medium"
        >
          <span
            className="w-2 h-2 rounded-full bg-risk-low animate-pulse"
            aria-hidden="true"
          />
          <span className="hidden sm:inline">Companion Standby</span>
          <span className="sm:hidden">Active</span>
        </div>

        {/* Mobile Settings Icon */}
        <Link
          href="/settings"
          aria-label="Settings"
          className={cn(
            "md:hidden w-9 h-9 rounded-lg bg-surfaceElevated border border-subtle flex items-center justify-center text-muted hover:text-primary transition-colors focus-visible:ring-2 focus-visible:ring-brandLight",
            pathname === "/settings" && "text-brand border-brand/50"
          )}
        >
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
              d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.325.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 011.37.49l1.296 2.247a1.125 1.125 0 01-.26 1.431l-1.003.827c-.293.241-.438.613-.43.992a7.723 7.723 0 010 .255c-.008.378.137.75.43.991l1.004.827c.424.35.534.955.26 1.43l-1.298 2.247a1.125 1.125 0 01-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.47 6.47 0 01-.22.128c-.331.183-.581.495-.644.869l-.213 1.281c-.09.543-.56.94-1.11.94h-2.594c-.55 0-1.019-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 01-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 01-1.369-.49l-1.297-2.247a1.125 1.125 0 01.26-1.431l1.004-.827c.292-.24.437-.613.43-.991a6.932 6.932 0 010-.255c.007-.38-.138-.751-.43-.992l-1.004-.827a1.125 1.125 0 01-.26-1.43l1.297-2.247a1.125 1.125 0 011.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.086.22-.128.332-.183.582-.495.644-.869l.214-1.28z"
            />
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            />
          </svg>
        </Link>
      </div>
    </header>
  );
};
