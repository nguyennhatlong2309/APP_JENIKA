import api from './api';
import { ActivityLog } from '@/types';
import { PageResponse } from './productService';

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

  async getActivityLogsPage(params: {
    page: number;
    size: number;
    search?: string;
    thaoTac?: string;
    tab?: string;
  }): Promise<PageResponse<ActivityLog>> {
    const query = new URLSearchParams();
    query.append('page', params.page.toString());
    query.append('size', params.size.toString());
    if (params.search) query.append('search', params.search);
    if (params.thaoTac && params.thaoTac !== 'Tất cả') query.append('thaoTac', params.thaoTac);
    if (params.tab && params.tab !== 'Tất cả phân hệ') query.append('tab', params.tab);
    return api.get<PageResponse<ActivityLog>>(`/metadata/nhat-ky/page?${query.toString()}`);
  },
};

export default activityService;
