import React, { Suspense } from "react";
import { SideNavigation } from "./SideNavigation";
import { TopBar } from "./TopBar";
import { MobileNavigation } from "./MobileNavigation";
import { DevFixtureBar } from "../dev/DevFixtureBar";

interface AppShellProps {
  children: React.ReactNode;
}

export const AppShell: React.FC<AppShellProps> = ({ children }) => {
  return (
    <div className="min-h-screen bg-base text-primary flex flex-col md:flex-row antialiased">
      {/* Accessible skip link */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-brand focus:text-white focus:rounded-lg focus:shadow-xl focus:outline-none"
      >
        Skip to main content
      </a>

      {/* Desktop / Tablet Left Sidebar Navigation */}
      <SideNavigation />

      {/* Main Content Column */}
      <div className="flex-1 flex flex-col min-w-0 pb-20 md:pb-8">
        <TopBar />

        <main
          id="main-content"
          tabIndex={-1}
          className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8 focus:outline-none"
        >
          {children}
        </main>
      </div>

      {/* Mobile Bottom Navigation */}
      <MobileNavigation />

      {/* Isolated Development-Only Fixture Switcher */}
      <Suspense fallback={null}>
        <DevFixtureBar />
      </Suspense>
    </div>
  );
};
