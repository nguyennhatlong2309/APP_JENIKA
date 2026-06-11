import api from './api';
import { PurchaseOrder } from '@/types';

export const purchaseService = {
  async getPurchaseOrders(): Promise<PurchaseOrder[]> {
    return api.get<PurchaseOrder[]>('/nhap-hang');
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
};

export default purchaseService;
