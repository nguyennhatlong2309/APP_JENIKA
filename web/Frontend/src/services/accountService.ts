import api from './api';

export interface AccountItem {
  id: number;
  username: string;
  trangThai: string;
  ngayTao: string;
  nhanVienId: number | null;
  tenNhanVien: string;
  roles: string[];
}

export interface UnlinkedEmployee {
  id: number;
  tenNhanVien: string;
  sdt: string;
  email: string;
  vaiTro: string;
}

export interface PermissionItem {
  id: number;
  tenQuyen: string;
  moTa: string;
}

export interface RoleDetailItem {
  id: number;
  tenVaiTro: string;
  moTa: string;
  quyens: string[];
}

export const accountService = {
  // Accounts Management
  async getAccounts(): Promise<AccountItem[]> {
    return api.get<AccountItem[]>('/auth/accounts');
  },

  async getRoles(): Promise<string[]> {
    return api.get<string[]>('/auth/roles');
  },

  async getUnlinkedEmployees(): Promise<UnlinkedEmployee[]> {
    return api.get<UnlinkedEmployee[]>('/auth/unlinked-employees');
  },

  async createAccount(payload: { username: string; password?: string; nhanVienId: number; roles: string[] }): Promise<void> {
    return api.post<void>('/auth/register', payload);
  },

  async updateAccount(id: number, payload: { trangThai?: string; roles?: string[]; password?: string }): Promise<void> {
    return api.put<void>(`/auth/accounts/${id}`, payload);
  },

  async deleteAccount(id: number): Promise<void> {
    return api.delete<void>(`/auth/accounts/${id}`);
  },

  // Roles & Permissions Management
  async getPermissions(): Promise<PermissionItem[]> {
    return api.get<PermissionItem[]>('/auth/permissions');
  },

  async getRolesDetails(): Promise<RoleDetailItem[]> {
    return api.get<RoleDetailItem[]>('/auth/roles/details');
  },

  async createRole(payload: { tenVaiTro: string; moTa?: string; quyens: string[] }): Promise<void> {
    return api.post<void>('/auth/roles', payload);
  },

  async updateRole(id: number, payload: { tenVaiTro?: string; moTa?: string; quyens: string[] }): Promise<void> {
    return api.put<void>(`/auth/roles/${id}`, payload);
  },

  async deleteRole(id: number): Promise<void> {
    return api.delete<void>(`/auth/roles/${id}`);
  }
};

export default accountService;
