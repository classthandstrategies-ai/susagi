import { RiskLevel, ProtectiveAction } from "@/types/risk";

export function cn(...classes: (string | boolean | undefined | null)[]): string {
  return classes.filter(Boolean).join(" ");
}

export function getRiskLabel(risk: RiskLevel): string {
  switch (risk) {
    case "LOW":
      return "Protected";
    case "CAUTION":
      return "Caution";
    case "HIGH":
      return "High Risk";
    case "CRITICAL":
      return "Critical Scam Alert";
  }
}

export function getRiskHindiLabel(risk: RiskLevel): string {
  switch (risk) {
    case "LOW":
      return "सुरक्षित";
    case "CAUTION":
      return "सावधान";
    case "HIGH":
      return "उच्च जोखिम";
    case "CRITICAL":
      return "गंभीर घोटाला चेतावनी";
  }
}

export function getRiskColorClass(risk: RiskLevel): {
  text: string;
  bg: string;
  bgSoft: string;
  border: string;
} {
  switch (risk) {
    case "LOW":
      return {
        text: "text-risk-low",
        bg: "bg-risk-low",
        bgSoft: "bg-risk-low-soft",
        border: "border-risk-low",
      };
    case "CAUTION":
      return {
        text: "text-risk-caution",
        bg: "bg-risk-caution",
        bgSoft: "bg-risk-caution-soft",
        border: "border-risk-caution",
      };
    case "HIGH":
      return {
        text: "text-risk-high",
        bg: "bg-risk-high",
        bgSoft: "bg-risk-high-soft",
        border: "border-risk-high",
      };
    case "CRITICAL":
      return {
        text: "text-risk-critical",
        bg: "bg-risk-critical",
        bgSoft: "bg-risk-critical-soft",
        border: "border-risk-critical",
      };
  }
}

export function formatProtectiveAction(action: ProtectiveAction): string {
  switch (action) {
    case "CONTINUE_MONITORING":
      return "Continue Monitoring";
    case "VERIFY_IDENTITY":
      return "Verify Identity";
    case "END_CALL":
      return "End Call Immediately";
    case "DO_NOT_SHARE_CREDENTIALS":
      return "Do Not Share Passwords / OTPs";
    case "DO_NOT_SEND_MONEY":
      return "Do Not Transfer Money";
    case "USE_OFFICIAL_CHANNEL":
      return "Call Official Bank Helpline";
    case "DO_NOT_INSTALL_REMOTE_ACCESS":
      return "Do Not Install Remote Access Apps";
  }
}
