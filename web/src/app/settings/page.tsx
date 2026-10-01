import React from "react";
import type { Metadata } from "next";
import { SettingsPageClient } from "@/features/settings";

export const metadata: Metadata = {
  title: "Settings & Preferences — SuSagi Companion",
  description:
    "Review SuSagi companion operational status, accessibility preferences, and privacy disclosures.",
};

export default function SettingsPage() {
  return <SettingsPageClient />;
}
