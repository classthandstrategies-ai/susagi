import type { Metadata, Viewport } from "next";
import "./globals.css";
import { AppShell } from "@/components/navigation/AppShell";

export const metadata: Metadata = {
  title: "SuSagi — Autonomous Scam Defense Companion",
  description:
    "Banking-grade protective companion for scam defense, live call screening, and trusted guardian circle.",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#111622",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="bg-base text-primary min-h-screen">
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}
