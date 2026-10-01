import React, { Suspense } from "react";
import { TopBar } from "./TopBar";
import { MobileNavigation } from "./MobileNavigation";
import { DevFixtureBar } from "../dev/DevFixtureBar";

interface AppShellProps {
  children: React.ReactNode;
}

export const AppShell: React.FC<AppShellProps> = ({ children }) => {
  return (
    <div className="min-h-screen bg-base text-primary flex flex-col antialiased">
      {/* Accessible skip link */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-brand focus:text-white focus:rounded-lg focus:shadow-xl focus:outline-none"
      >
        Skip to main content
      </a>

      {/* Desktop / Mobile Top Navigation */}
      <TopBar />

      {/* Main Content Column — Calm reading width */}
      <main
        id="main-content"
        tabIndex={-1}
        className="flex-1 w-full max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-10 pb-24 md:pb-12 focus:outline-none"
      >
        {children}
      </main>

      {/* Mobile Bottom Navigation */}
      <MobileNavigation />

      {/* Isolated Development-Only Fixture Switcher */}
      <Suspense fallback={null}>
        <DevFixtureBar />
      </Suspense>
    </div>
  );
};
