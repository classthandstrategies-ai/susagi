import React from "react";
import { ProtectionCapabilityCard } from "@/components/cards/ProtectionCapabilityCard";
import { ProtectionCapability } from "@/types/protection";
import { OfflineBanner } from "@/components/feedback/OfflineBanner";

export default function ProtectPage() {
  const capabilities: ProtectionCapability[] = [
    {
      id: "cap-call-screening",
      name: "Autonomous Call Screening",
      description:
        "Real-time speech-to-text with bilingual scam pattern detection for Hindi, Hinglish, and English.",
      status: "ACTIVE",
      iconName: "phone",
      supportedOnWeb: false,
    },
    {
      id: "cap-link-inspection",
      name: "Deep URL & Phishing Inspector",
      description:
        "Proactively inspects shortened and deceptive web links before sensitive banking credentials are entered.",
      status: "ACTIVE",
      iconName: "link",
      supportedOnWeb: true,
    },
    {
      id: "cap-qr-defense",
      name: "UPI Payment QR Validator",
      description:
        "Detects reverse-charge UPI QR codes and prevents deceptive money-request scams.",
      status: "ACTIVE",
      iconName: "qr",
      supportedOnWeb: true,
    },
    {
      id: "cap-guardian-network",
      name: "Guardian Verification Network",
      description:
        "Cryptographic and verbal identity challenges to neutralize AI voice cloning and impersonation attacks.",
      status: "CONFIGURED",
      iconName: "users",
      supportedOnWeb: true,
    },
  ];

  return (
    <div className="space-y-6">
      <OfflineBanner message="Phase W1 Product Shell — Production Protection Center experience arrives in W2." />

      <header className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-primary">
          Protection Shields
        </h1>
        <p className="text-sm text-secondary">
          Active multi-layered security shields protecting phone calls, messages, and transactions.
        </p>
      </header>

      <section
        aria-labelledby="capabilities-heading"
        className="grid grid-cols-1 md:grid-cols-2 gap-4"
      >
        <h2 id="capabilities-heading" className="sr-only">
          Security Capabilities
        </h2>
        {capabilities.map((cap) => (
          <ProtectionCapabilityCard key={cap.id} capability={cap} />
        ))}
      </section>
    </div>
  );
}
