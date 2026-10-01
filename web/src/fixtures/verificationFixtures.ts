import { VerificationSession } from "@/types/verification";
import { FIXTURE_NOTICE } from "./metadata";

export interface FixtureVerificationSession extends VerificationSession {
  _fixtureNotice: string;
  outcomeNote?: string;
}

export const readyFixtureSession: FixtureVerificationSession = {
  sessionId: "sess-ready-demo",
  _fixtureNotice: FIXTURE_NOTICE,
  status: "READY",
  requesterName: "You (Companion User)",
  requesterPhone: "+91 98765 00001",
  recipientName: "Anita Sharma (Sister / Guardian)",
  recipientPhone: "+91 98101 23456",
  claim: "Unverified caller claiming to represent bank KYC department requesting account confirmation.",
  createdAt: "Just now",
  expiresAt: "5 minutes from start",
};

export const pendingFixtureSession: FixtureVerificationSession = {
  sessionId: "sess-pending-demo",
  _fixtureNotice: FIXTURE_NOTICE,
  status: "PENDING",
  requesterName: "You (Companion User)",
  requesterPhone: "+91 98765 00001",
  recipientName: "Anita Sharma",
  recipientPhone: "+91 98101 23456",
  claim: "Urgent medical expense assistance requested by unknown caller imitating family member.",
  createdAt: "1 minute ago",
  expiresAt: "in 4 minutes",
};

export const verifiedFixtureSession: FixtureVerificationSession = {
  sessionId: "sess-verified-demo",
  _fixtureNotice: FIXTURE_NOTICE,
  status: "VERIFIED",
  requesterName: "You (Companion User)",
  requesterPhone: "+91 98765 00001",
  recipientName: "Anita Sharma",
  recipientPhone: "+91 98101 23456",
  claim: "Emergency hostel fee transfer confirmation requested over voice call.",
  outcomeNote: "Anita confirmed that this request is from her.",
  createdAt: "4 minutes ago",
  expiresAt: "Concluded",
};

export const rejectedFixtureSession: FixtureVerificationSession = {
  sessionId: "sess-rejected-demo",
  _fixtureNotice: FIXTURE_NOTICE,
  status: "REJECTED",
  requesterName: "You (Companion User)",
  requesterPhone: "+91 98765 00001",
  recipientName: "Anita Sharma",
  recipientPhone: "+91 98101 23456",
  claim: "Caller requested cancellation OTP for an alleged debit card transaction.",
  outcomeNote: "Anita says this request is not from her.",
  createdAt: "6 minutes ago",
  expiresAt: "Concluded",
};

export const expiredFixtureSession: FixtureVerificationSession = {
  sessionId: "sess-expired-demo",
  _fixtureNotice: FIXTURE_NOTICE,
  status: "EXPIRED",
  requesterName: "You (Companion User)",
  requesterPhone: "+91 98765 00001",
  recipientName: "Aarav Sharma",
  recipientPhone: "+91 98202 34567",
  claim: "Caller requested verification of parcel tracking number.",
  outcomeNote: "No response was received before the verification window ended.",
  createdAt: "15 minutes ago",
  expiresAt: "Expired 5 minutes ago",
};

export const responderIncomingFixture: FixtureVerificationSession = {
  sessionId: "sess-incoming-req-2026",
  _fixtureNotice: FIXTURE_NOTICE,
  status: "PENDING",
  requesterName: "Papa (Ramesh)",
  requesterPhone: "+91 98101 11222",
  recipientName: "You (Guardian Responder)",
  recipientPhone: "+91 98765 00001",
  claim: "A caller claiming to be an advocate states you are detained and urgently need Rs. 25,000 sent via UPI.",
  createdAt: "2 minutes ago",
  expiresAt: "in 3 minutes",
};

export const allVerificationFixtures: Record<string, FixtureVerificationSession> = {
  ready: readyFixtureSession,
  pending: pendingFixtureSession,
  verified: verifiedFixtureSession,
  rejected: rejectedFixtureSession,
  expired: expiredFixtureSession,
  request: responderIncomingFixture,
};
