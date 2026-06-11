import api from './api';
import { ThuChiDbItem, ExpenseCategoryItem } from '@/types';

export const expenseService = {
  async getExpenses(): Promise<ThuChiDbItem[]> {
    return api.get<ThuChiDbItem[]>('/thu-chi');
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
