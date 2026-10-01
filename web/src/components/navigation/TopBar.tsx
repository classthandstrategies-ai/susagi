"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

interface NavLink {
  name: string;
  href: string;
}

const PRIMARY_NAV: NavLink[] = [
  { name: "Home", href: "/" },
  { name: "Protect", href: "/protect" },
  { name: "Activity", href: "/activity" },
  { name: "Guardians", href: "/guardians" },
];

export const TopBar: React.FC = () => {
  const pathname = usePathname();

  return (
    <header className="h-16 px-4 sm:px-6 lg:px-8 bg-surface/90 backdrop-blur-md border-b border-subtle sticky top-0 z-30 flex items-center justify-between">
      {/* Brand Identity & Desktop Primary Navigation */}
      <div className="flex items-center gap-8">
        <Link
          href="/"
          className="flex items-center gap-2.5 text-primary font-bold text-lg tracking-tight select-none focus-visible:ring-2 focus-visible:ring-brandLight rounded-lg py-1 px-1.5 -ml-1.5"
          aria-label="SuSagi Home"
        >
          <div className="w-7 h-7 rounded-lg bg-brand flex items-center justify-center text-white shrink-0 shadow-sm">
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
          <span>SuSagi</span>
        </Link>

        {/* Desktop Restrained Navigation */}
        <nav
          aria-label="Primary Navigation"
          className="hidden md:flex items-center gap-1"
        >
          {PRIMARY_NAV.map((item) => {
            const isActive =
              item.href === "/"
                ? pathname === "/"
                : pathname.startsWith(item.href);

            return (
              <Link
                key={item.href}
                href={item.href}
                aria-current={isActive ? "page" : undefined}
                className={cn(
                  "px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors select-none focus-visible:ring-2 focus-visible:ring-brandLight",
                  isActive
                    ? "text-primary font-semibold bg-surfaceElevated"
                    : "text-secondary hover:text-primary hover:bg-surfaceElevated/60"
                )}
              >
                {item.name}
              </Link>
            );
          })}
        </nav>
      </div>

      {/* Right Controls: Settings (visibly secondary) */}
      <div className="flex items-center gap-2">
        <Link
          href="/settings"
          aria-current={pathname === "/settings" ? "page" : undefined}
          aria-label="Settings"
          className={cn(
            "min-h-[44px] min-w-[44px] sm:min-w-0 sm:px-3 sm:py-1.5 rounded-lg text-sm font-medium text-secondary hover:text-primary hover:bg-surfaceElevated transition-colors flex items-center justify-center gap-2 focus-visible:ring-2 focus-visible:ring-brandLight",
            pathname === "/settings" && "text-primary font-semibold bg-surfaceElevated"
          )}
        >
          <svg
            className="w-4 h-4 text-muted shrink-0"
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
          <span className="hidden sm:inline">Settings</span>
        </Link>
      </div>
    </header>
  );
};
