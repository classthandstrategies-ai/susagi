"use client";

import React, { useState } from "react";
import { CompanionStatusSection } from "./CompanionStatusSection";
import { AccessibilitySection } from "./AccessibilitySection";
import { PrivacyDataSection } from "./PrivacyDataSection";
import { AboutSection } from "./AboutSection";

type SettingsTab = "ALL" | "ACCESSIBILITY" | "PRIVACY" | "STATUS" | "ABOUT";

export const SettingsPageClient: React.FC = () => {
  const [activeTab, setActiveTab] = useState<SettingsTab>("ALL");

  const tabs: { id: SettingsTab; label: string }[] = [
    { id: "ALL", label: "All settings" },
    { id: "ACCESSIBILITY", label: "Accessibility" },
    { id: "PRIVACY", label: "Privacy" },
    { id: "STATUS", label: "Connection" },
    { id: "ABOUT", label: "About" },
  ];

  return (
    <div className="space-y-8 max-w-3xl mx-auto pb-12">
      {/* Page Header */}
      <header className="space-y-2">
        <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-primary">
          Settings
        </h1>
        <p className="text-sm text-secondary leading-relaxed">
          Manage your accessibility preferences, review privacy guarantees, and inspect connection status.
        </p>
      </header>

      {/* Tab Filter Bar */}
      <nav
        aria-label="Settings section filter"
        className="flex flex-wrap items-center gap-1.5 border-b border-subtle pb-3"
      >
        {tabs.map((tab) => {
          const isSelected = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              role="tab"
              aria-selected={isSelected}
              onClick={() => setActiveTab(tab.id)}
              className={`min-h-[40px] px-3.5 py-1.5 rounded-full text-xs font-medium transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center border ${
                isSelected
                  ? "bg-brand text-white border-brand shadow-sm"
                  : "bg-surface hover:bg-surfaceElevated text-secondary hover:text-primary border-subtle"
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </nav>

      {/* Settings Sections */}
      <div className="space-y-8">
        {(activeTab === "ALL" || activeTab === "ACCESSIBILITY") && (
          <div id="accessibility">
            <AccessibilitySection />
          </div>
        )}

        {(activeTab === "ALL" || activeTab === "PRIVACY") && (
          <div id="privacy" className={activeTab === "ALL" ? "pt-6 border-t border-subtle" : ""}>
            <PrivacyDataSection />
          </div>
        )}

        {(activeTab === "ALL" || activeTab === "STATUS") && (
          <div id="service-status" className={activeTab === "ALL" ? "pt-6 border-t border-subtle" : ""}>
            <CompanionStatusSection />
          </div>
        )}

        {(activeTab === "ALL" || activeTab === "ABOUT") && (
          <div id="about" className={activeTab === "ALL" ? "pt-6 border-t border-subtle" : ""}>
            <AboutSection />
          </div>
        )}
      </div>
    </div>
  );
};
