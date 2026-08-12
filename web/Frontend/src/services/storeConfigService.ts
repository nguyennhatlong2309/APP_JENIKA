import api from './api';
import { StoreConfig } from '@/types';

export const storeConfigService = {
  async getConfig(): Promise<StoreConfig> {
    return api.get<StoreConfig>('/store-config');
  },

  async updateConfig(config: StoreConfig): Promise<StoreConfig> {
    return api.put<StoreConfig>('/store-config', config);
  },
};

export default storeConfigService;
