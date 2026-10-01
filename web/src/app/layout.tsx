import type { Metadata, Viewport } from "next";
import "./globals.css";
import { AppShell } from "@/components/navigation/AppShell";

export const metadata: Metadata = {
  title: "SuSagi — Safety Companion",
  description:
    "SuSagi is a safety companion for reviewing suspicious activity, coordinating identity checks, and inspecting links before you open them.",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#FAF9F6",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="bg-base text-primary min-h-screen">
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}
