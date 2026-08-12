import api from './api';
import { SaleOrder } from '@/types';
import { PageResponse } from './productService';

export const saleService = {
  async getSaleOrders(): Promise<SaleOrder[]> {
    return api.get<SaleOrder[]>('/ban-hang');
  },

  async getSaleOrdersPage(params: {
    page: number;
    size: number;
    search?: string;
    status?: string;
    startDate?: string;
    endDate?: string;
  }): Promise<PageResponse<SaleOrder>> {
    const query = new URLSearchParams();
    query.append('page', params.page.toString());
    query.append('size', params.size.toString());
    if (params.search) query.append('search', params.search);
    if (params.status && params.status !== 'All') query.append('status', params.status);
    if (params.startDate) query.append('fromDate', params.startDate);
    if (params.endDate) query.append('toDate', params.endDate);
    return api.get<PageResponse<SaleOrder>>(`/ban-hang/page?${query.toString()}`);
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

  async exportSaleOrderExcel(id: number): Promise<Blob> {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
    const headers: Record<string, string> = {};
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }
    const response = await fetch(`${baseUrl}/ban-hang/${id}/export`, {
      method: 'GET',
      headers,
    });
    if (!response.ok) throw new Error('Không thể xuất file Excel hóa đơn');
    return response.blob();
  },
};

export default saleService;
