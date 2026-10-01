import { RiskAssessment } from "@/types/risk";
import { FIXTURE_NOTICE } from "./metadata";

export interface FixtureRiskAssessment extends RiskAssessment {
  _fixtureNotice: string;
}

export const lowRiskScenario: FixtureRiskAssessment = {
  id: "fixture-risk-low",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "LOW",
  headline: "Active Protection Active",
  explanation:
    "No suspicious indicators, coercive patterns, or financial pressure detected in recent communications.",
  recommendedAction: "CONTINUE_MONITORING",
  actionRationale:
    "System is standing by. Normal safe communication patterns detected.",
  signals: [],
  timestamp: "Just now",
  hindiHeadline: "सक्रिय सुरक्षा चालू है",
  hindiExplanation: "कोई संदिग्ध पैटर्न या वित्तीय दबाव नहीं मिला है।",
  hindiRecommendedAction: "निगरानी जारी रखें",
};

export const cautionScenario: FixtureRiskAssessment = {
  id: "fixture-risk-caution",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "CAUTION",
  headline: "Unverified Financial Inquiry",
  explanation:
    "Caller claims to represent a financial institution but caller identity cannot be verified through official banking records.",
  recommendedAction: "VERIFY_IDENTITY",
  actionRationale:
    "Do not provide account numbers or OTPs until identity is verified via an official incoming channel.",
  signals: [
    {
      id: "sig-c1",
      title: "Unverified Caller ID",
      description: "Incoming number does not match registered bank helpline numbers.",
      severity: "CAUTION",
      category: "CALLER_REPUTATION",
      timestamp: "1m ago",
    },
    {
      id: "sig-c2",
      title: "KYC Update Inquiry",
      description: "Caller requested clarification on banking KYC status.",
      severity: "CAUTION",
      category: "INFORMATION_PROBING",
      timestamp: "30s ago",
    },
  ],
  evidence: [
    {
      id: "evi-c1",
      title: "Call Audio Inspection",
      callerOrSource: "+91 98210 XXXXX",
      timestamp: "10:14 AM",
      transcriptSnippet: "We noticed your bank KYC is pending renewal...",
      severity: "CAUTION",
      tags: ["KYC", "Unknown Caller"],
    },
  ],
  timestamp: "1m ago",
  hindiHeadline: "अपुष्ट वित्तीय पूछताछ",
  hindiExplanation: "कॉलर बैंक प्रतिनिधि होने का दावा कर रहा है पर पहचान सत्यापित नहीं है।",
  hindiRecommendedAction: "पहचान सत्यापित करें",
};

export const highRiskScenario: FixtureRiskAssessment = {
  id: "fixture-risk-high",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "HIGH",
  headline: "Simulated Law Enforcement Claim",
  explanation:
    "Caller is attempting coercive authority by falsely alleging legal action and demanding non-disclosure.",
  recommendedAction: "DO_NOT_SHARE_CREDENTIALS",
  actionRationale:
    "Law enforcement agencies in India never conduct digital arrests or demand immediate money transfers over phone/video calls.",
  signals: [
    {
      id: "sig-h1",
      title: "Digital Arrest Assertion",
      description: "Claimed an arrest warrant has been issued by customs or police.",
      severity: "HIGH",
      category: "AUTHORITY_COERCION",
      timestamp: "2m ago",
      highlightedText: "Illegal parcel intercepted at customs",
    },
    {
      id: "sig-h2",
      title: "Secrecy Demand",
      description: "Instructed user not to disconnect or inform family members.",
      severity: "HIGH",
      category: "ISOLATION_TACTIC",
      timestamp: "1m ago",
      highlightedText: "Do not cut the call or tell anyone",
    },
  ],
  evidence: [
    {
      id: "evi-h1",
      title: "Audio Pattern Extract",
      callerOrSource: "+91 88001 XXXXX (Simulated)",
      timestamp: "10:30 AM",
      transcriptSnippet: "This is Crime Branch Delhi. A warrant has been issued...",
      severity: "HIGH",
      tags: ["Digital Arrest", "Crime Branch Impersonation"],
    },
  ],
  timestamp: "2m ago",
  hindiHeadline: "फर्जी पुलिस/डिजिटल अरेस्ट का दावा",
  hindiExplanation: "कॉलर कानूनी कार्रवाई का डर दिखाकर दबाव बना रहा है।",
  hindiRecommendedAction: "कोई जानकारी साझा न करें",
};

export const criticalRiskScenario: FixtureRiskAssessment = {
  id: "fixture-risk-critical",
  _fixtureNotice: FIXTURE_NOTICE,
  riskLevel: "CRITICAL",
  headline: "Immediate Scam Threat: Fund Transfer Demand",
  explanation:
    "Active extortion and urgency coercing immediate money transfer to a fake verification account.",
  recommendedAction: "END_CALL",
  actionRationale:
    "CRITICAL THREAT: Disconnect the call immediately. Legitimate authorities never ask you to transfer funds for verification.",
  signals: [
    {
      id: "sig-crit-1",
      title: "Direct Fund Transfer Coercion",
      description: "Aggressively demanding transfer to an 'RBI Safe Account'.",
      severity: "CRITICAL",
      category: "FINANCIAL_EXTORTION",
      timestamp: "45s ago",
      highlightedText: "Transfer Rs 50,000 immediately to verify your funds",
    },
    {
      id: "sig-crit-2",
      title: "Imminent Penalty Threat",
      description: "Threatening immediate arrest within 30 minutes if funds are not moved.",
      severity: "CRITICAL",
      category: "URGENCY_MANIPULATION",
      timestamp: "20s ago",
      highlightedText: "Police will reach your door in 30 minutes",
    },
  ],
  evidence: [
    {
      id: "evi-crit-1",
      title: "Live Call Analysis",
      callerOrSource: "+91 91100 XXXXX (Flagged)",
      timestamp: "10:45 AM",
      transcriptSnippet: "You must transfer the security deposit right now to this UPI ID...",
      severity: "CRITICAL",
      tags: ["Immediate Extortion", "UPI Demand", "Emergency"],
    },
  ],
  timestamp: "Just now",
  hindiHeadline: "गंभीर घोटाला: तत्काल पैसे भेजने की मांग",
  hindiExplanation: "तुरंत पैसे ट्रांसफर करने का गंभीर दबाव बनाया जा रहा है।",
  hindiRecommendedAction: "कॉल तुरंत काटें",
};

export const riskFixtures: Record<string, FixtureRiskAssessment> = {
  low: lowRiskScenario,
  caution: cautionScenario,
  high: highRiskScenario,
  critical: criticalRiskScenario,
};
