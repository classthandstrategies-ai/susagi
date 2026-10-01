export interface GuardianContact {
  id: string;
  name: string;
  phone: string;
  relationship: string;
  isPrimary: boolean;
  canVerifyIdentity: boolean;
  status: "ACTIVE" | "PENDING_INVITE" | "UNAVAILABLE";
  avatarInitials: string;
  lastActive?: string;
}

export interface GuardianCircle {
  guardians: GuardianContact[];
  activeAlertCount: number;
}
