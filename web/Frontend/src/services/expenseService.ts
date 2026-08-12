import api from './api';
import { ThuChiDbItem, ExpenseCategoryItem } from '@/types';
import { PageResponse } from './productService';

export const expenseService = {
  async getExpenses(): Promise<ThuChiDbItem[]> {
    return api.get<ThuChiDbItem[]>('/thu-chi');
  },

  async getExpensesPage(params: {
    page: number;
    size: number;
    search?: string;
    categoryId?: number;
    status?: string;
    startDate?: string;
    endDate?: string;
    transactionType?: string;
  }): Promise<PageResponse<ThuChiDbItem>> {
    const query = new URLSearchParams();
    query.append('page', params.page.toString());
    query.append('size', params.size.toString());
    if (params.search) query.append('search', params.search);
    if (params.categoryId !== undefined) query.append('categoryId', params.categoryId.toString());
    if (params.status && params.status !== 'All') query.append('status', params.status);
    if (params.startDate) query.append('fromDate', params.startDate);
    if (params.endDate) query.append('toDate', params.endDate);
    if (params.transactionType) query.append('transactionType', params.transactionType);
    return api.get<PageResponse<ThuChiDbItem>>(`/thu-chi/page?${query.toString()}`);
  },

  async getExpenseStats(params?: { startDate?: string; endDate?: string }): Promise<{
    totalIncome: number;
    totalExpenses: number;
    netProfit: number;
    transactionCount: number;
  }> {
    const query = new URLSearchParams();
    if (params?.startDate) query.append('fromDate', params.startDate);
    if (params?.endDate) query.append('toDate', params.endDate);
    const queryString = query.toString();
    return api.get<any>(`/thu-chi/stats${queryString ? `?${queryString}` : ''}`);
  },

  async createExpense(expense: any): Promise<ThuChiDbItem> {
    return api.post<ThuChiDbItem>('/thu-chi', expense);
  },

  async updateExpense(id: number, expense: any): Promise<ThuChiDbItem> {
    return api.put<ThuChiDbItem>(`/thu-chi/${id}`, expense);
  },

  async deleteExpense(id: number): Promise<void> {
    return api.delete<void>(`/thu-chi/${id}`);
  },

  async getExpenseCategories(): Promise<ExpenseCategoryItem[]> {
    return api.get<ExpenseCategoryItem[]>('/metadata/loai-thu-chi');
  },
};

export default expenseService;
