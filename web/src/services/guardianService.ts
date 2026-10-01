import { GuardianCircle } from "@/types/guardian";

export interface IGuardianService {
  getGuardianCircle(): Promise<GuardianCircle>;
}

export class OfflineGuardianService implements IGuardianService {
  async getGuardianCircle(): Promise<GuardianCircle> {
    return {
      guardians: [],
      securityPassphraseConfigured: false,
      activeAlertCount: 0,
    };
  }
}

export const guardianService: IGuardianService = new OfflineGuardianService();
