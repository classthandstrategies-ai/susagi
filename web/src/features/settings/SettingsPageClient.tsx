"use client";

import React, { useState } from "react";
import { CompanionStatusSection } from "./CompanionStatusSection";
import { AccessibilitySection } from "./AccessibilitySection";
import { PrivacyDataSection } from "./PrivacyDataSection";
import { AboutSection } from "./AboutSection";

type SettingsTab = "ALL" | "STATUS" | "ACCESSIBILITY" | "PRIVACY" | "ABOUT";

export const SettingsPageClient: React.FC = () => {
  const [activeTab, setActiveTab] = useState<SettingsTab>("ALL");

  const tabs: { id: SettingsTab; label: string }[] = [
    { id: "ALL", label: "All Settings" },
    { id: "STATUS", label: "Service Status" },
    { id: "ACCESSIBILITY", label: "Accessibility" },
    { id: "PRIVACY", label: "Privacy & Data" },
    { id: "ABOUT", label: "About SuSagi" },
  ];

  return (
    <div className="space-y-8 max-w-4xl mx-auto pb-12">
      {/* Page Header */}
      <header className="space-y-2">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-muted">
            Preferences & Architecture
          </span>
          <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-surfaceElevated text-secondary border border-subtle">
            WEB COMPANION
          </span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-primary">
          Settings & Companion Preferences
        </h1>
        <p className="text-sm text-secondary max-w-2xl leading-relaxed">
          Operational connectivity status, browser accessibility options, and privacy disclosures for the SuSagi companion.
        </p>
      </header>

      {/* Tab Filter Bar (Accessible, 48px minimum target) */}
      <nav
        aria-label="Settings section filter"
        className="flex flex-wrap items-center gap-2 border-b border-subtle pb-3"
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
              className={`min-h-[48px] px-4 py-2.5 rounded-xl text-xs font-semibold transition-colors focus-visible:ring-2 focus-visible:ring-brandLight cursor-pointer inline-flex items-center justify-center ${
                isSelected
                  ? "bg-brand text-white shadow-sm"
                  : "bg-surface hover:bg-surfaceElevated text-secondary hover:text-primary border border-subtle"
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </nav>

      {/* Settings Sections */}
      <div className="space-y-10">
        {(activeTab === "ALL" || activeTab === "STATUS") && (
          <div id="service-status">
            <CompanionStatusSection />
          </div>
        )}

        {(activeTab === "ALL" || activeTab === "ACCESSIBILITY") && (
          <div id="accessibility" className={activeTab === "ALL" ? "pt-6 border-t border-subtle" : ""}>
            <AccessibilitySection />
          </div>
        )}

        {(activeTab === "ALL" || activeTab === "PRIVACY") && (
          <div id="privacy" className={activeTab === "ALL" ? "pt-6 border-t border-subtle" : ""}>
            <PrivacyDataSection />
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
