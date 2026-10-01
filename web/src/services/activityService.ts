import { IncidentItem, IncidentDetail } from "@/types/activity";

export interface IActivityService {
  getIncidents(): Promise<IncidentItem[]>;
  getIncidentById(id: string): Promise<IncidentDetail | null>;
}

export class OfflineActivityService implements IActivityService {
  async getIncidents(): Promise<IncidentItem[]> {
    // In production without paired Android device or cloud sync, returns empty state
    return [];
  }

  async getIncidentById(_id: string): Promise<IncidentDetail | null> {
    void _id;
    return null;
  }
}

export const activityService: IActivityService = new OfflineActivityService();
