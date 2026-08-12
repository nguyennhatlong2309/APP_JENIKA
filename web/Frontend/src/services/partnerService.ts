import api from './api';
import { PartnerItem, EmployeeItem } from '@/types';
import { PageResponse } from './productService';

export const partnerService = {
  // Business Partners
  async getPartners(): Promise<PartnerItem[]> {
    return api.get<PartnerItem[]>('/metadata/doi-tac');
  },

  async getPartnersPage(params: {
    page: number;
    size: number;
    search?: string;
  }): Promise<PageResponse<PartnerItem>> {
    const query = new URLSearchParams();
    query.append('page', params.page.toString());
    query.append('size', params.size.toString());
    if (params.search) query.append('search', params.search);
    return api.get<PageResponse<PartnerItem>>(`/metadata/doi-tac/page?${query.toString()}`);
  },

  async createPartner(partner: { ten: string; sdt?: string; diaChi?: string; email?: string }): Promise<PartnerItem> {
    return api.post<PartnerItem>('/metadata/doi-tac', partner);
  },

  async updatePartner(id: number, partner: { ten: string; sdt?: string; diaChi?: string; email?: string }): Promise<PartnerItem> {
    return api.put<PartnerItem>(`/metadata/doi-tac/${id}`, partner);
  },

  async deletePartner(id: number): Promise<void> {
    return api.delete<void>(`/metadata/doi-tac/${id}`);
  },

  // Employees
  async getEmployees(): Promise<EmployeeItem[]> {
    return api.get<EmployeeItem[]>('/metadata/nhan-vien');
  },

  async getEmployeesPage(params: {
    page: number;
    size: number;
    search?: string;
  }): Promise<PageResponse<EmployeeItem>> {
    const query = new URLSearchParams();
    query.append('page', params.page.toString());
    query.append('size', params.size.toString());
    if (params.search) query.append('search', params.search);
    return api.get<PageResponse<EmployeeItem>>(`/metadata/nhan-vien/page?${query.toString()}`);
  },

  async createEmployee(employee: { tenNhanVien: string; sdt?: string; email?: string; vaiTro?: string }): Promise<EmployeeItem> {
    return api.post<EmployeeItem>('/metadata/nhan-vien', employee);
  },

  async updateEmployee(id: number, employee: { tenNhanVien: string; sdt?: string; email?: string; vaiTro?: string }): Promise<EmployeeItem> {
    return api.put<EmployeeItem>(`/metadata/nhan-vien/${id}`, employee);
  },

  async deleteEmployee(id: number): Promise<void> {
    return api.delete<void>(`/metadata/nhan-vien/${id}`);
  },
};

export default partnerService;
