import api from './api';
import { ProductItem, CategoryItem, GroupItem, UnitItem } from '@/types';

let categoriesCache: Promise<CategoryItem[]> | null = null;
let unitsCache: Promise<UnitItem[]> | null = null;
let groupsCache: Promise<GroupItem[]> | null = null;

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const productService = {
  // Products
  async getActiveProducts(): Promise<ProductItem[]> {
    return api.get<ProductItem[]>('/san-pham');
  },

  async getAllProducts(): Promise<ProductItem[]> {
    return api.get<ProductItem[]>('/san-pham/all');
  },

  async getProductsPage(params: {
    page: number;
    size: number;
    biXoa?: boolean;
    search?: string;
    categoryIds?: number[];
    groupIds?: number[];
    statuses?: string[];
  }): Promise<PageResponse<ProductItem>> {
    const queryParams = new URLSearchParams();
    queryParams.append('page', params.page.toString());
    queryParams.append('size', params.size.toString());
    if (params.biXoa !== undefined) queryParams.append('biXoa', params.biXoa.toString());
    if (params.search) queryParams.append('search', params.search);
    if (params.categoryIds && params.categoryIds.length > 0) {
      queryParams.append('categoryIds', params.categoryIds.join(','));
    }
    if (params.groupIds && params.groupIds.length > 0) {
      queryParams.append('groupIds', params.groupIds.join(','));
    }
    if (params.statuses && params.statuses.length > 0) {
      queryParams.append('statuses', params.statuses.join(','));
    }
    return api.get<PageResponse<ProductItem>>(`/san-pham/page?${queryParams.toString()}`);
  },

  async getProductStats(): Promise<{
    totalItems: number;
    totalValue: number;
    lowStockCount: number;
    activeCount: number;
    deletedCount: number;
  }> {
    return api.get('/san-pham/stats');
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
    if (!categoriesCache) {
      categoriesCache = api.get<CategoryItem[]>('/metadata/danh-muc').catch(err => {
        categoriesCache = null;
        throw err;
      });
    }
    return categoriesCache;
  },

  async createCategory(category: { tenDanhMuc: string; moTa?: string }): Promise<CategoryItem> {
    categoriesCache = null;
    return api.post<CategoryItem>('/metadata/danh-muc', category);
  },

  async updateCategory(id: number, category: { tenDanhMuc: string }): Promise<CategoryItem> {
    categoriesCache = null;
    return api.put<CategoryItem>(`/metadata/danh-muc/${id}`, category);
  },

  // Groups
  async getGroups(): Promise<GroupItem[]> {
    if (!groupsCache) {
      groupsCache = api.get<GroupItem[]>('/metadata/nhom-san-pham').catch(err => {
        groupsCache = null;
        throw err;
      });
    }
    return groupsCache;
  },

  async createGroup(group: { tenNhom: string }): Promise<GroupItem> {
    groupsCache = null;
    return api.post<GroupItem>('/metadata/nhom-san-pham', group);
  },

  async updateGroup(id: number, group: { tenNhom: string }): Promise<GroupItem> {
    groupsCache = null;
    return api.put<GroupItem>(`/metadata/nhom-san-pham/${id}`, group);
  },

  // Units
  async getUnits(): Promise<UnitItem[]> {
    if (!unitsCache) {
      unitsCache = api.get<UnitItem[]>('/metadata/don-vi').catch(err => {
        unitsCache = null;
        throw err;
      });
    }
    return unitsCache;
  },
};

export default productService;
