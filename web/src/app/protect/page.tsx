import React from "react";
import { ProtectPageClient } from "@/features/protect/ProtectPageClient";
import { protectionService } from "@/services/protectionService";

interface ProtectPageProps {
  searchParams: Promise<{ fixture?: string }>;
}

export default async function ProtectPage({ searchParams }: ProtectPageProps) {
  const resolvedParams = await searchParams;
  const isDev = process.env.NODE_ENV === "development";
  const fixtureKey = resolvedParams.fixture;
  const isFixtureMode = isDev && Boolean(fixtureKey && fixtureKey !== "none");

  const capabilities = await protectionService.getCapabilities();

  return (
    <ProtectPageClient
      capabilities={capabilities}
      isFixtureMode={isFixtureMode}
    />
  );
}
