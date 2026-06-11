import api from './api';
import { ActivityLog } from '@/types';

const ENDPOINTS = {
  GET_ACTIVITY_LOGS: '/metadata/nhat-ky',
} as const;

export const activityService = {
  async getActivityLogs(): Promise<ActivityLog[]> {
    try {
      return await api.get<ActivityLog[]>(ENDPOINTS.GET_ACTIVITY_LOGS);
    } catch (error) {
      console.error('Failed to fetch activity logs:', error);
      throw error;
    }
  },
};

export default activityService;
