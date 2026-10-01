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
  additionalActions: ["DO_NOT_SHARE_CREDENTIALS", "USE_OFFICIAL_CHANNEL"],
  claimedIdentity: "HDFC Fraud Prevention",
  channel: "CALL",
  summary:
    "Caller attempted to harvest OTP under the guise of stopping an unauthorized international transaction on HDFC card.",
  outcome: "Call disconnected without sharing credentials or OTP. No financial loss incurred.",
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
      eventType: "CALL",
    },
    {
      id: "tl-2",
      timestamp: "09:42:35 AM",
      title: "Scam Pattern Detected",
      detail: "Caller manufactured urgency about an international debit.",
      riskLevel: "HIGH",
      eventType: "CALL",
    },
    {
      id: "tl-3",
      timestamp: "09:43:02 AM",
      title: "Critical Action Triggered",
      detail: "Caller demanded OTP. SuSagi alerted user to terminate call immediately.",
      riskLevel: "CRITICAL",
      eventType: "ACTION",
    },
    {
      id: "tl-4",
      timestamp: "09:43:15 AM",
      title: "Call Terminated Safely",
      detail: "User disconnected the call. No OTP or financial information shared.",
      riskLevel: "LOW",
      eventType: "ACTION",
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
  additionalActions: ["DO_NOT_SEND_MONEY", "USE_OFFICIAL_CHANNEL"],
  claimedIdentity: "Distressed Relative (Nephew)",
  channel: "CALL",
  summary:
    "Caller claimed to be a distressed relative in police custody, requesting urgent UPI transfer.",
  outcome: "Caller disconnected when asked for the family verification code.",
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
      eventType: "CALL",
    },
    {
      id: "tl-f2",
      timestamp: "04:16:12 PM",
      title: "Identity Challenge Prompt",
      detail: "SuSagi advised user to verify identity through trusted out-of-band channel.",
      riskLevel: "HIGH",
      eventType: "VERIFICATION",
    },
    {
      id: "tl-f3",
      timestamp: "04:16:45 PM",
      title: "Caller Hung Up",
      detail: "Caller disconnected when asked for the family verification code.",
      riskLevel: "LOW",
      eventType: "ACTION",
    },
  ],
};

export const courierLinkIncident: FixtureIncidentDetail = {
  id: "inc-courier-link-2026",
  _fixtureNotice: FIXTURE_NOTICE,
  title: "Unverified Postal Delivery Address Link",
  source: "SMS from VK-INDPST",
  timestamp: "2 days ago at 11:20 AM",
  riskLevel: "CAUTION",
  status: "REVIEWED",
  recommendedAction: "USE_OFFICIAL_CHANNEL",
  additionalActions: ["DO_NOT_INSTALL_REMOTE_ACCESS"],
  claimedIdentity: "India Post Delivery",
  channel: "LINK",
  summary:
    "SMS notification claiming an undelivered parcel due to an incomplete street address, directing to an unverified third-party tracking link.",
  outcome: "User verified tracking number on official portal; SMS link was not opened.",
  signals: [
    {
      id: "sig-c1",
      title: "Unverified Tracking URL",
      description: "Shortened domain link unrelated to official indiapost.gov.in domain.",
      severity: "CAUTION",
      category: "PHISHING_LINK",
      timestamp: "11:20 AM",
      highlightedText: "update-indpost-address.com/track",
    },
  ],
  evidence: [
    {
      id: "evi-c1",
      title: "SMS Message Content",
      callerOrSource: "VK-INDPST",
      timestamp: "11:20 AM",
      transcriptSnippet:
        "Your parcel delivery has been paused due to incorrect street address. Please confirm your details within 12h: http://update-indpost-address.com/track",
      severity: "CAUTION",
      tags: ["SMS Phishing", "Unverified Domain"],
    },
  ],
  timeline: [
    {
      id: "tl-c1",
      timestamp: "11:20:00 AM",
      title: "SMS Received",
      detail: "Unsolicited text received regarding parcel address issue.",
      riskLevel: "CAUTION",
      eventType: "MESSAGE",
    },
    {
      id: "tl-c2",
      timestamp: "11:21:15 AM",
      title: "Domain Verification Warning",
      detail: "Link domain was flagged as unrelated to registered postal services.",
      riskLevel: "CAUTION",
      eventType: "LINK",
    },
    {
      id: "tl-c3",
      timestamp: "11:22:00 AM",
      title: "Official Channel Advised",
      detail: "User advised to check tracking solely on official portal.",
      riskLevel: "LOW",
      eventType: "ACTION",
    },
  ],
};

export const utilityQueryIncident: FixtureIncidentDetail = {
  id: "inc-utility-query-2026",
  _fixtureNotice: FIXTURE_NOTICE,
  title: "Routine Maintenance Notification Inquiry",
  source: "+91 11 2345 6789 (Verified Provider)",
  timestamp: "3 days ago at 02:10 PM",
  riskLevel: "LOW",
  status: "RESOLVED",
  recommendedAction: "CONTINUE_MONITORING",
  claimedIdentity: "Delhi Vidyut Board",
  channel: "CALL",
  summary:
    "Informational call confirming scheduled grid maintenance in the residential sector. No payment or credentials requested.",
  outcome: "Call completed normally with zero suspicious prompts.",
  signals: [],
  evidence: [],
  timeline: [
    {
      id: "tl-u1",
      timestamp: "02:10:00 PM",
      title: "Call Connected",
      detail: "Automated notification of scheduled maintenance.",
      riskLevel: "LOW",
      eventType: "CALL",
    },
    {
      id: "tl-u2",
      timestamp: "02:11:30 PM",
      title: "Call Concluded",
      detail: "No credentials or payment requests observed.",
      riskLevel: "LOW",
      eventType: "SYSTEM",
    },
  ],
};

const toIncidentItem = (detail: FixtureIncidentDetail): IncidentItem => ({
  id: detail.id,
  title: detail.title,
  source: detail.source,
  timestamp: detail.timestamp,
  riskLevel: detail.riskLevel,
  actionTaken: detail.recommendedAction,
  summary: detail.summary,
  signalCount: detail.signals.length,
  claimedIdentity: detail.claimedIdentity,
  channel: detail.channel,
});

export const sampleIncidentsList: IncidentItem[] = [
  toIncidentItem(bankImpersonationIncident),
  toIncidentItem(familyImpersonationIncident),
  toIncidentItem(courierLinkIncident),
  toIncidentItem(utilityQueryIncident),
];

export const allFixtureIncidents: Record<string, FixtureIncidentDetail> = {
  [bankImpersonationIncident.id]: bankImpersonationIncident,
  [familyImpersonationIncident.id]: familyImpersonationIncident,
  [courierLinkIncident.id]: courierLinkIncident,
  [utilityQueryIncident.id]: utilityQueryIncident,
};
