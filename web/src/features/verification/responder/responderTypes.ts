import { VerificationSession } from "@/types/verification";

export type ResponderLocalResult = "NONE" | "VERIFIED_PREVIEW" | "REJECTED_PREVIEW";

export interface VerificationResponderViewState {
  hasRequest: boolean;
  session: VerificationSession | null;
  isFixtureMode: boolean;
  fixtureKey?: string;
}
