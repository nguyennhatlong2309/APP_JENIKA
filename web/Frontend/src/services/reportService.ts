import api from './api';
import { DashboardData } from '@/types';

export const reportService = {
  async getDashboardData(): Promise<DashboardData> {
    return api.get<DashboardData>('/bao-cao/dashboard');
  },

  async getProductSales(params?: { startDate?: string; endDate?: string }): Promise<any[]> {
    const query = new URLSearchParams();
    if (params?.startDate) query.append('fromDate', params.startDate);
    if (params?.endDate) query.append('toDate', params.endDate);
    const queryString = query.toString();
    return api.get<any[]>(`/bao-cao/san-pham-ban${queryString ? `?${queryString}` : ''}`);
  },

  async getProductSalesStats(params?: { startDate?: string; endDate?: string }): Promise<any> {
    const query = new URLSearchParams();
    if (params?.startDate) query.append('fromDate', params.startDate);
    if (params?.endDate) query.append('toDate', params.endDate);
    const queryString = query.toString();
    return api.get<any>(`/bao-cao/san-pham-ban/stats${queryString ? `?${queryString}` : ''}`);
  },
};

export default reportService;
