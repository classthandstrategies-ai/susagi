import { VerificationSession, VerificationStatus } from "@/types/verification";

export interface VerificationRequesterViewState {
  status: VerificationStatus;
  session: VerificationSession | null;
  outcomeNote?: string;
  isFixtureMode: boolean;
  fixtureKey?: string;
}
