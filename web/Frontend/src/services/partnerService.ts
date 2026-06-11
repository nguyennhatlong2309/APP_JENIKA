import api from './api';
import { PartnerItem, EmployeeItem } from '@/types';

export const partnerService = {
  // Business Partners
  async getPartners(): Promise<PartnerItem[]> {
    return api.get<PartnerItem[]>('/metadata/doi-tac');
  },

  async createPartner(partner: { ten: string; sdt?: string; diaChi?: string; email?: string }): Promise<PartnerItem> {
    return api.post<PartnerItem>('/metadata/doi-tac', partner);
  },

  // Employees
  async getEmployees(): Promise<EmployeeItem[]> {
    return api.get<EmployeeItem[]>('/metadata/nhan-vien');
  },

  async createEmployee(employee: { tenNhanVien: string; sdt?: string; email?: string; vaiTro?: string }): Promise<EmployeeItem> {
    return api.post<EmployeeItem>('/metadata/nhan-vien', employee);
  },

  async deleteEmployee(id: number): Promise<void> {
    return api.delete<void>(`/metadata/nhan-vien/${id}`);
  },
};

export default partnerService;
