import { RiskAssessment } from "@/types/risk";
import { FIXTURE_NOTICE } from "./metadata";

export interface FixtureRiskAssessment extends RiskAssessment {
  _fixtureNotice: string;
}

export const lowRiskScenario: FixtureRiskAssessment = {
  id: "fixture-risk-low",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "LOW",
  claimedIdentity: "Known Contact Number (Claimed)",
  headline: "Standard Communication Pattern",
  explanation:
    "No suspicious indicators, coercive patterns, or financial demands found in the current assessment.",
  recommendedAction: "CONTINUE_MONITORING",
  actionRationale:
    "System is standing by. Communication language follows expected everyday patterns.",
  signals: [],
  timestamp: "Active call (02:14)",
  hindiHeadline: "सामान्य संवाद पैटर्न",
  hindiExplanation: "कोई संदिग्ध पैटर्न या वित्तीय दबाव नहीं मिला है।",
  hindiRecommendedAction: "निगरानी जारी रखें",
};

export const cautionScenario: FixtureRiskAssessment = {
  id: "fixture-risk-caution",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "CAUTION",
  claimedIdentity: "State Bank Representative (Claimed)",
  headline: "Unverified Financial Inquiry",
  explanation:
    "Caller claims to represent a financial institution asking about account status without verified credentials.",
  recommendedAction: "VERIFY_IDENTITY",
  additionalActions: ["USE_OFFICIAL_CHANNEL"],
  actionRationale:
    "Do not provide account numbers or OTPs until identity is verified via an official incoming channel.",
  signals: [
    {
      id: "sig-c1",
      title: "Unverified Caller Claim",
      description: "Incoming number does not match registered bank helpline directory.",
      severity: "CAUTION",
      category: "CALLER_REPUTATION",
      timestamp: "1m ago",
    },
    {
      id: "sig-c2",
      title: "Unexpected Urgency Regarding KYC",
      description: "Caller insists that debit card access will expire today if details are not verified.",
      severity: "CAUTION",
      category: "INFORMATION_PROBING",
      timestamp: "30s ago",
      highlightedText: "Your card will be deactivated by this evening",
    },
    {
      id: "sig-c3",
      title: "Possible synthetic voice signal",
      description: "Voice characteristics appear unusual with slight robotic cadence inflection.",
      severity: "CAUTION",
      category: "VOICE_ACOUSTICS",
      timestamp: "15s ago",
    },
  ],
  evidence: [
    {
      id: "evi-c1",
      title: "Captured Call Context",
      callerOrSource: "+91 98210 XXXXX (Claimed: Bank Support)",
      timestamp: "10:14 AM",
      transcriptSnippet:
        "Sir, we noticed your banking KYC is pending renewal. Please confirm your registration details to avoid card block.",
      severity: "CAUTION",
      tags: ["KYC Query", "Unverified Caller", "Claimed Bank"],
    },
  ],
  timestamp: "Active call (01:45)",
  hindiHeadline: "अपुष्ट वित्तीय पूछताछ",
  hindiExplanation: "कॉलर बैंक प्रतिनिधि होने का दावा कर रहा है पर पहचान सत्यापित नहीं है।",
  hindiRecommendedAction: "पहचान सत्यापित करें",
};

export const highRiskScenario: FixtureRiskAssessment = {
  id: "fixture-risk-high",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "HIGH",
  claimedIdentity: "Customs / Crime Branch (Claimed)",
  headline: "Authority Coercion & Digital Arrest Threat",
  explanation:
    "Caller is attempting coercive authority by falsely alleging an illegal parcel interception and demanding non-disclosure.",
  recommendedAction: "DO_NOT_SHARE_CREDENTIALS",
  additionalActions: [
    "DO_NOT_SEND_MONEY",
    "USE_OFFICIAL_CHANNEL",
    "VERIFY_IDENTITY",
  ],
  actionRationale:
    "Law enforcement agencies in India never conduct digital arrests or demand immediate money transfers over phone/video calls.",
  signals: [
    {
      id: "sig-h1",
      title: "Digital Arrest Assertion",
      description: "Claimed a formal arrest warrant has been issued by customs or police.",
      severity: "HIGH",
      category: "AUTHORITY_COERCION",
      timestamp: "2m ago",
      highlightedText: "Illegal parcel intercepted at Mumbai customs with drugs",
    },
    {
      id: "sig-h2",
      title: "Secrecy & Non-Disclosure Demand",
      description: "Instructed user under threat not to disconnect or inform family members.",
      severity: "HIGH",
      category: "ISOLATION_TACTIC",
      timestamp: "1m ago",
      highlightedText: "Do not cut the call or tell anyone in your house",
    },
    {
      id: "sig-h3",
      title: "Voice characteristics appear unusual",
      description: "Acoustic parameters indicate unnatural pacing and background synthesized noise floor.",
      severity: "HIGH",
      category: "VOICE_ACOUSTICS",
      timestamp: "45s ago",
    },
  ],
  evidence: [
    {
      id: "evi-h1",
      title: "Captured Call Context",
      callerOrSource: "+91 88001 XXXXX (Claimed: Crime Branch)",
      timestamp: "10:30 AM",
      transcriptSnippet:
        "This is Inspector Verma from Crime Branch Delhi. A warrant has been issued against your Aadhaar card. Do not cut this video call or police will come.",
      severity: "HIGH",
      tags: ["Digital Arrest Claim", "Authority Impersonation", "Secrecy Demand"],
    },
  ],
  timestamp: "Active call (03:12)",
  hindiHeadline: "फर्जी पुलिस / डिजिटल अरेस्ट का दावा",
  hindiExplanation: "कॉलर कानूनी कार्रवाई का डर दिखाकर दबाव बना रहा है।",
  hindiRecommendedAction: "कोई जानकारी साझा न करें",
};

export const criticalRiskScenario: FixtureRiskAssessment = {
  id: "fixture-risk-critical",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "CRITICAL",
  claimedIdentity: "RBI Security Department (Claimed)",
  headline: "Immediate Scam Threat: Urgent Fund Transfer Coercion",
  explanation:
    "Active extortion and extreme urgency coercing immediate money transfer or remote access app installation.",
  recommendedAction: "END_CALL",
  additionalActions: [
    "DO_NOT_SEND_MONEY",
    "DO_NOT_INSTALL_REMOTE_ACCESS",
    "DO_NOT_SHARE_CREDENTIALS",
  ],
  actionRationale:
    "CRITICAL SAFETY ACTION: End the call on your phone immediately. Legitimate authorities never ask you to transfer funds or share screen access.",
  signals: [
    {
      id: "sig-crit-1",
      title: "Direct Fund Transfer Coercion",
      description: "Aggressively demanding transfer of funds to an unverified 'RBI Safe Account'.",
      severity: "CRITICAL",
      category: "FINANCIAL_EXTORTION",
      timestamp: "45s ago",
      highlightedText: "Transfer Rs 50,000 immediately to verify your clean funds",
    },
    {
      id: "sig-crit-2",
      title: "Remote Access App Installation Request",
      description: "Instructing user to download AnyDesk/TeamViewer to 'secure device'.",
      severity: "CRITICAL",
      category: "DEVICE_TAKEOVER",
      timestamp: "30s ago",
      highlightedText: "Install QuickSupport app so we can verify your device",
    },
    {
      id: "sig-crit-3",
      title: "Imminent Penalty Threat",
      description: "Threatening immediate bank freeze and police dispatch within 15 minutes.",
      severity: "CRITICAL",
      category: "URGENCY_MANIPULATION",
      timestamp: "10s ago",
      highlightedText: "Your account will be frozen in 15 minutes if you delay",
    },
  ],
  evidence: [
    {
      id: "evi-crit-1",
      title: "Captured Call Context",
      callerOrSource: "+91 91100 XXXXX (Flagged Number)",
      timestamp: "10:45 AM",
      transcriptSnippet:
        "You must transfer the security deposit right now to this UPI ID or install the verification tool so we can clear your name.",
      severity: "CRITICAL",
      tags: ["Direct Extortion", "UPI Demand", "Remote Access Prompt"],
    },
  ],
  timestamp: "Active call (04:05)",
  hindiHeadline: "गंभीर घोटाला: तत्काल पैसे भेजने की मांग",
  hindiExplanation: "तुरंत पैसे ट्रांसफर करने और ऐप डाउनलोड करने का दबाव बनाया जा रहा है।",
  hindiRecommendedAction: "कॉल तुरंत फोन पर काटें",
};

export const riskFixtures: Record<string, FixtureRiskAssessment> = {
  low: lowRiskScenario,
  caution: cautionScenario,
  high: highRiskScenario,
  critical: criticalRiskScenario,
};
