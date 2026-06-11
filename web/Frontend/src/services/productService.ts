import api from './api';
import { ProductItem, CategoryItem, GroupItem, UnitItem } from '@/types';

export const productService = {
  // Products
  async getActiveProducts(): Promise<ProductItem[]> {
    return api.get<ProductItem[]>('/san-pham');
  },

  async getAllProducts(): Promise<ProductItem[]> {
    return api.get<ProductItem[]>('/san-pham/all');
  },

  async getLowStockProducts(): Promise<ProductItem[]> {
    return api.get<ProductItem[]>('/san-pham/low-stock');
  },

  async createProduct(product: any): Promise<ProductItem> {
    return api.post<ProductItem>('/san-pham', product);
  },

  async updateProduct(id: number, product: any): Promise<ProductItem> {
    return api.put<ProductItem>(`/san-pham/${id}`, product);
  },

  async deleteProduct(id: number): Promise<void> {
    return api.delete<void>(`/san-pham/${id}`);
  },

  async restoreProduct(id: number): Promise<ProductItem> {
    return api.put<ProductItem>(`/san-pham/${id}/restore`);
  },

  // Categories
  async getCategories(): Promise<CategoryItem[]> {
    return api.get<CategoryItem[]>('/metadata/danh-muc');
  },

  async createCategory(category: { tenDanhMuc: string; moTa?: string }): Promise<CategoryItem> {
    return api.post<CategoryItem>('/metadata/danh-muc', category);
  },

  async updateCategory(id: number, category: { tenDanhMuc: string }): Promise<CategoryItem> {
    return api.put<CategoryItem>(`/metadata/danh-muc/${id}`, category);
  },

  // Groups
  async getGroups(): Promise<GroupItem[]> {
    return api.get<GroupItem[]>('/metadata/nhom-san-pham');
  },

  async createGroup(group: { tenNhom: string }): Promise<GroupItem> {
    return api.post<GroupItem>('/metadata/nhom-san-pham', group);
  },

  async updateGroup(id: number, group: { tenNhom: string }): Promise<GroupItem> {
    return api.put<GroupItem>(`/metadata/nhom-san-pham/${id}`, group);
  },

  // Units
  async getUnits(): Promise<UnitItem[]> {
    return api.get<UnitItem[]>('/metadata/don-vi');
  },
};

export default productService;
