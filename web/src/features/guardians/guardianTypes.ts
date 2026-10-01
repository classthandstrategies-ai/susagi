import { GuardianCircle } from "@/types/guardian";
import { GuardianServiceStatus } from "@/services/guardianService";

export interface GuardianCircleViewState {
  status: GuardianServiceStatus;
  circle: GuardianCircle;
  errorMessage?: string;
  isFixtureMode: boolean;
  fixtureKey?: string;
}

export type GuardianDialogMode =
  | "NONE"
  | "ADD"
  | "EDIT"
  | "REMOVE"
  | "EDUCATION";

export interface GuardianFormData {
  id?: string;
  name: string;
  relationship: string;
  phone: string;
  isPrimary?: boolean;
}
