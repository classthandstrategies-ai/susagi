import { VerificationSession, VerificationResult } from "@/types/verification";

/**
 * Interface contract for remote identity verification operations.
 * Decouples React UI components from future backend/Firebase implementations.
 */
export interface IVerificationService {
  requestVerification(
    recipientPhone: string,
    claim: string
  ): Promise<VerificationResult>;

  respondToVerification(
    sessionId: string,
    accept: boolean
  ): Promise<VerificationResult>;

  getSession(sessionId: string): Promise<VerificationSession | null>;
}

/**
 * Production-ready runtime adapter.
 * Honestly reports UNAVAILABLE because no remote backend or Firebase is connected in W1.
 * Never presents fake network success.
 */
export class OfflineVerificationService implements IVerificationService {
  async requestVerification(
    _recipientPhone: string,
    _claim: string
  ): Promise<VerificationResult> {
    void _recipientPhone;
    void _claim;
    return {
      status: "UNAVAILABLE",
      message:
        "Identity verification network is not connected. The SuSagi web companion is operating in offline-first mode.",
    };
  }

  async respondToVerification(
    _sessionId: string,
    _accept: boolean
  ): Promise<VerificationResult> {
    void _sessionId;
    void _accept;
    return {
      status: "UNAVAILABLE",
      message:
        "Identity verification responder network is currently unavailable on this client.",
    };
  }

  async getSession(_sessionId: string): Promise<VerificationSession | null> {
    void _sessionId;
    return null;
  }
}

export const verificationService: IVerificationService =
  new OfflineVerificationService();
