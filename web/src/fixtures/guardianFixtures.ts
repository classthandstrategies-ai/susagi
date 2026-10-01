import { GuardianCircle } from "@/types/guardian";
import { FIXTURE_NOTICE } from "./metadata";

export interface FixtureGuardianCircle extends GuardianCircle {
  _fixtureNotice: string;
}

export const guardianCircleFixture: FixtureGuardianCircle = {
  _fixtureNotice: FIXTURE_NOTICE,
  securityPassphraseConfigured: true,
  activeAlertCount: 0,
  guardians: [
    {
      id: "g-1",
      name: "Aarav Sharma",
      phone: "+91 98101 23456",
      relationship: "Son / Primary Guardian",
      isPrimary: true,
      canVerifyIdentity: true,
      status: "ACTIVE",
      avatarInitials: "AS",
      lastActive: "Active today",
    },
    {
      id: "g-2",
      name: "Pooja Verma",
      phone: "+91 98202 34567",
      relationship: "Daughter",
      isPrimary: false,
      canVerifyIdentity: true,
      status: "ACTIVE",
      avatarInitials: "PV",
      lastActive: "Yesterday",
    },
    {
      id: "g-3",
      name: "Rohan Gupta",
      phone: "+91 98303 45678",
      relationship: "Trusted Neighbor",
      isPrimary: false,
      canVerifyIdentity: false,
      status: "PENDING_INVITE",
      avatarInitials: "RG",
    },
  ],
};
