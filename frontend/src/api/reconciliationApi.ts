import api from './axios';

export const autoReconcile = async (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await api.post('/reconciliations/auto', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};