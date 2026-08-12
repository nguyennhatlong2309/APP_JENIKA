import api from './api';
import { PurchaseOrder } from '@/types';
import { PageResponse } from './productService';

export const purchaseService = {
  async getPurchaseOrders(): Promise<PurchaseOrder[]> {
    return api.get<PurchaseOrder[]>('/nhap-hang');
  },

  async getPurchaseOrdersPage(params: {
    page: number;
    size: number;
    search?: string;
    status?: string;
    startDate?: string;
    endDate?: string;
  }): Promise<PageResponse<PurchaseOrder>> {
    const query = new URLSearchParams();
    query.append('page', params.page.toString());
    query.append('size', params.size.toString());
    if (params.search) query.append('search', params.search);
    if (params.status && params.status !== 'All') query.append('status', params.status);
    if (params.startDate) query.append('fromDate', params.startDate);
    if (params.endDate) query.append('toDate', params.endDate);
    return api.get<PageResponse<PurchaseOrder>>(`/nhap-hang/page?${query.toString()}`);
  },

  async getPurchaseOrderStats(params?: { startDate?: string; endDate?: string }): Promise<{
    totalProcurement: number;
    totalDebt: number;
    totalOrders: number;
    completionRate: number;
  }> {
    const query = new URLSearchParams();
    if (params?.startDate) query.append('fromDate', params.startDate);
    if (params?.endDate) query.append('toDate', params.endDate);
    const queryString = query.toString();
    return api.get<any>(`/nhap-hang/stats${queryString ? `?${queryString}` : ''}`);
  },

  async getPurchaseOrderById(id: number): Promise<PurchaseOrder> {
    return api.get<PurchaseOrder>(`/nhap-hang/${id}`);
  },

  async createPurchaseOrder(order: Partial<PurchaseOrder>): Promise<PurchaseOrder> {
    return api.post<PurchaseOrder>('/nhap-hang', order);
  },

  async updatePurchaseOrder(id: number, order: Partial<PurchaseOrder>): Promise<PurchaseOrder> {
    return api.put<PurchaseOrder>(`/nhap-hang/${id}`, order);
  },

  async updateStatus(id: number, status: string): Promise<void> {
    return api.put<void>(`/nhap-hang/${id}/status?status=${encodeURIComponent(status)}`);
  },

  async exportPurchaseOrderExcel(id: number): Promise<Blob> {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
    const headers: Record<string, string> = {};
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }
    const response = await fetch(`${baseUrl}/nhap-hang/${id}/export`, {
      method: 'GET',
      headers,
    });
    if (!response.ok) throw new Error('Không thể xuất file Excel đơn nhập hàng');
    return response.blob();
  },
};

export default purchaseService;
