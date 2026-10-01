/**
 * SuSagi Verification Contract
 *
 * Used for Guardian identity challenge and verification sessions.
 */

export type VerificationStatus =
  | "READY"
  | "PENDING"
  | "VERIFIED"
  | "REJECTED"
  | "EXPIRED"
  | "UNAVAILABLE";

export interface VerificationSession {
  sessionId: string;
  status: VerificationStatus;
  requesterName: string;
  requesterPhone: string;
  recipientName: string;
  recipientPhone: string;
  claim: string; // e.g. "Identity verification request from Bank"
  createdAt: string;
  expiresAt: string;
}

export interface VerificationResult {
  status: VerificationStatus;
  message: string;
  session?: VerificationSession;
}
