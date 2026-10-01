import { IncidentDetail, IncidentItem } from "@/types/activity";
import { FIXTURE_NOTICE } from "./metadata";

export interface FixtureIncidentDetail extends IncidentDetail {
  _fixtureNotice: string;
}

export const bankImpersonationIncident: FixtureIncidentDetail = {
  id: "inc-bank-impersonation-2026",
  _fixtureNotice: FIXTURE_NOTICE,
  title: "Bank Credit Card Department Impersonation",
  source: "+91 98765 43210 (Spoofed Private Number)",
  timestamp: "Today at 09:42 AM",
  riskLevel: "CRITICAL",
  status: "BLOCKED",
  recommendedAction: "END_CALL",
  summary:
    "Caller attempted to harvest OTP under the guise of stopping an unauthorized international transaction on HDFC card.",
  signals: [
    {
      id: "sig-b1",
      title: "Urgent Financial Threat",
      description: "Claimed Rs. 48,000 debit was occurring right now in London.",
      severity: "HIGH",
      category: "URGENCY",
      timestamp: "09:42 AM",
      highlightedText: "Your card is being charged right now",
    },
    {
      id: "sig-b2",
      title: "OTP Harvesting Request",
      description: "Requested 6-digit cancellation code received via SMS.",
      severity: "CRITICAL",
      category: "CREDENTIAL_HARVESTING",
      timestamp: "09:43 AM",
      highlightedText: "Read out the 6-digit reversal code",
    },
  ],
  evidence: [
    {
      id: "evi-b1",
      title: "Incoming Call Audio Intercept",
      callerOrSource: "+91 98765 43210",
      timestamp: "09:42 AM",
      transcriptSnippet:
        "Sir this is Rajesh from HDFC Fraud Prevention. An unauthorized debit of 48000 rupees is initiated. To cancel, please tell me the cancellation code you received.",
      severity: "CRITICAL",
      tags: ["OTP Harvesting", "Bank Impersonation", "Voice Call"],
    },
  ],
  timeline: [
    {
      id: "tl-1",
      timestamp: "09:42:10 AM",
      title: "Call Initiated",
      detail: "Incoming call from unverified caller claiming to be HDFC Fraud Prevention.",
      riskLevel: "CAUTION",
    },
    {
      id: "tl-2",
      timestamp: "09:42:35 AM",
      title: "Scam Pattern Detected",
      detail: "Caller manufactured urgency about an international debit.",
      riskLevel: "HIGH",
    },
    {
      id: "tl-3",
      timestamp: "09:43:02 AM",
      title: "Critical Action Triggered",
      detail: "Caller demanded OTP. SuSagi alerted user to terminate call immediately.",
      riskLevel: "CRITICAL",
    },
    {
      id: "tl-4",
      timestamp: "09:43:15 AM",
      title: "Call Terminated Safely",
      detail: "User disconnected the call. No OTP or financial information shared.",
      riskLevel: "LOW",
    },
  ],
};

export const familyImpersonationIncident: FixtureIncidentDetail = {
  id: "inc-family-emergency-2026",
  _fixtureNotice: FIXTURE_NOTICE,
  title: "AI Voice / Family Emergency Impersonation",
  source: "+91 99112 00000 (Unknown Number)",
  timestamp: "Yesterday at 04:15 PM",
  riskLevel: "HIGH",
  status: "RESOLVED",
  recommendedAction: "VERIFY_IDENTITY",
  summary:
    "Caller claimed to be a distressed relative in police custody, requesting urgent UPI transfer.",
  signals: [
    {
      id: "sig-f1",
      title: "Distress Impersonation",
      description: "Caller claimed to be user's nephew involved in an accident.",
      severity: "HIGH",
      category: "FAMILY_EMERGENCY",
      timestamp: "04:15 PM",
    },
    {
      id: "sig-f2",
      title: "Third-party UPI Request",
      description: "Demanded money sent to a lawyer's personal UPI ID.",
      severity: "HIGH",
      category: "FINANCIAL_PRESSURE",
      timestamp: "04:16 PM",
    },
  ],
  evidence: [
    {
      id: "evi-f1",
      title: "Call Audio Transcript",
      callerOrSource: "+91 99112 00000",
      timestamp: "04:15 PM",
      transcriptSnippet:
        "Uncle please help me, I am at the police station with my friend, please send 25000 to this advocate QR code right now.",
      severity: "HIGH",
      tags: ["Emergency Impersonation", "UPI Demand"],
    },
  ],
  timeline: [
    {
      id: "tl-f1",
      timestamp: "04:15:00 PM",
      title: "Unknown Call Received",
      detail: "Caller spoke with emotional distress imitating relative.",
      riskLevel: "CAUTION",
    },
    {
      id: "tl-f2",
      timestamp: "04:16:12 PM",
      title: "Identity Challenge Prompt",
      detail: "SuSagi advised user to verify identity via family secret passphrase.",
      riskLevel: "HIGH",
    },
    {
      id: "tl-f3",
      timestamp: "04:16:45 PM",
      title: "Caller Hung Up",
      detail: "Caller disconnected when asked for the family verification code.",
      riskLevel: "LOW",
    },
  ],
};

export const sampleIncidentsList: IncidentItem[] = [
  {
    id: bankImpersonationIncident.id,
    title: bankImpersonationIncident.title,
    source: bankImpersonationIncident.source,
    timestamp: bankImpersonationIncident.timestamp,
    riskLevel: bankImpersonationIncident.riskLevel,
    actionTaken: bankImpersonationIncident.recommendedAction,
    summary: bankImpersonationIncident.summary,
    signalCount: bankImpersonationIncident.signals.length,
  },
  {
    id: familyImpersonationIncident.id,
    title: familyImpersonationIncident.title,
    source: familyImpersonationIncident.source,
    timestamp: familyImpersonationIncident.timestamp,
    riskLevel: familyImpersonationIncident.riskLevel,
    actionTaken: familyImpersonationIncident.recommendedAction,
    summary: familyImpersonationIncident.summary,
    signalCount: familyImpersonationIncident.signals.length,
  },
];
