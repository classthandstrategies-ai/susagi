import {
  ProtectionCapability,
  WebProtectionOverview,
} from "@/types/protection";

export interface IProtectionService {
  getOverview(): Promise<WebProtectionOverview>;
  getCapabilities(): Promise<ProtectionCapability[]>;
  getCapabilityById(id: string): Promise<ProtectionCapability | null>;
}

export const DEFAULT_CAPABILITIES: ProtectionCapability[] = [
  {
    id: "call-protection",
    name: "Call Protection",
    tagline: "Helps identify suspicious behavior during supported calls.",
    description:
      "Analyzes live conversation patterns for extortion, digital arrest claims, and authority coercion.",
    platform: "ANDROID_DEVICE",
    status: "NOT_CONNECTED",
    statusLabel: "Device service not connected",
    whereItRuns: "SuSagi Android Protection App",
    whatItDoes:
      "Performs on-device speech-to-text with bilingual scam pattern detection during active phone calls.",
    whatYouCanDo:
      "Connect a supported Android phone running SuSagi to receive live telemetry, or view simulated scenarios in Live Defense.",
    primaryActionLabel: "Open Live Defense",
    primaryActionHref: "/live",
    isInteractiveOnWeb: false,
  },
  {
    id: "message-protection",
    name: "Message Protection",
    tagline:
      "Checks supported messages and alerts for suspicious requests when connected to a SuSagi protection service.",
    description:
      "Inspects SMS and notification alerts for OTP harvesting, impersonation, and fraudulent payment links.",
    platform: "ANDROID_DEVICE",
    status: "NOT_CONNECTED",
    statusLabel: "Not connected",
    whereItRuns: "SuSagi Android Notification Service",
    whatItDoes:
      "Scans incoming messages for coercive demands, fake parcel delivery alerts, and unverified bank warnings.",
    whatYouCanDo:
      "Install SuSagi on your Android device to automatically screen incoming SMS and alerts.",
    primaryActionLabel: "Review Activity",
    primaryActionHref: "/activity",
    isInteractiveOnWeb: false,
  },
  {
    id: "link-check",
    name: "Link Check",
    tagline: "Check suspicious links before opening them.",
    description:
      "Inspect suspicious URLs and domain syntax before entering passwords or banking credentials.",
    platform: "WEB_COMPANION",
    status: "AVAILABLE",
    statusLabel: "Available in companion UI",
    whereItRuns: "SuSagi Web Companion",
    whatItDoes:
      "Validates URL format, identifies misleading top-level domains, and gives safety guidance without contacting untrusted hosts.",
    whatYouCanDo:
      "Paste any link into the companion inspector to review protocol safety, host identity, and safe navigation steps.",
    primaryActionLabel: "Check a Link",
    isInteractiveOnWeb: true,
  },
  {
    id: "qr-shield",
    name: "QR Shield",
    tagline: "Scan QR codes with a supported SuSagi device client.",
    description:
      "Protects against reverse-charge UPI payment scams and malicious redirect codes.",
    platform: "ANDROID_DEVICE",
    status: "DEVICE_ONLY",
    statusLabel: "Available on mobile device",
    whereItRuns: "SuSagi Android Camera Scanner",
    whatItDoes:
      "Decodes UPI payment parameters and warns if a QR code is designed to debit your account instead of crediting it.",
    whatYouCanDo:
      "Use the SuSagi mobile app to scan unknown QR codes in stores, bills, or unsolicited messages.",
    isInteractiveOnWeb: false,
  },
];

export class ProductionProtectionService implements IProtectionService {
  async getOverview(): Promise<WebProtectionOverview> {
    return {
      state: "READY",
      headline: "Web Companion Ready",
      statusDescription:
        "This companion is ready. Device protection status will appear here when connected to SuSagi services.",
      deviceSyncStatus: "Device service not connected",
      isDeviceConnected: false,
      activeShieldsCount: 0,
      totalShieldsCount: 4,
    };
  }

  async getCapabilities(): Promise<ProtectionCapability[]> {
    return DEFAULT_CAPABILITIES;
  }

  async getCapabilityById(id: string): Promise<ProtectionCapability | null> {
    return DEFAULT_CAPABILITIES.find((c) => c.id === id) || null;
  }
}

export const protectionService: IProtectionService =
  new ProductionProtectionService();
