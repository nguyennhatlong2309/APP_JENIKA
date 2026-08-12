import api from './api';

export const uploadService = {
  async uploadImage(file: File): Promise<{ success: boolean; url: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return api.upload<{ success: boolean; url: string }>('/upload/image', formData);
  }
};

export default uploadService;
