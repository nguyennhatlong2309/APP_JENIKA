import api from './api';
import { SaleOrder } from '@/types';

export const saleService = {
  async getSaleOrders(): Promise<SaleOrder[]> {
    return api.get<SaleOrder[]>('/ban-hang');
  },

  async getSaleOrderById(id: number): Promise<SaleOrder> {
    return api.get<SaleOrder>(`/ban-hang/${id}`);
  },

  async getSaleStats(params?: { startDate?: string; endDate?: string }): Promise<any> {
    const query = new URLSearchParams();
    if (params?.startDate) query.append('startDate', params.startDate);
    if (params?.endDate) query.append('endDate', params.endDate);
    const queryString = query.toString();
    return api.get<any>(`/ban-hang/stats${queryString ? `?${queryString}` : ''}`);
  },

  async createSaleOrder(order: Partial<SaleOrder>): Promise<SaleOrder> {
    return api.post<SaleOrder>('/ban-hang', order);
  },

  async updateSaleOrder(id: number, order: Partial<SaleOrder>): Promise<SaleOrder> {
    return api.put<SaleOrder>(`/ban-hang/${id}`, order);
  },

  async updateStatus(id: number, status: string): Promise<void> {
    return api.put<void>(`/ban-hang/${id}/status?status=${encodeURIComponent(status)}`);
  },
};

export default saleService;
