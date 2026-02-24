import api from './axios';

export interface User {
  username: string;
  realName: string;
  role: string;
}

export const getUsers = async (params: { page: number; size: number }) => {
  const response = await api.get('/users', { params });
  return response.data;
};

export const createUser = async (user: User & { password: string }) => {
  const response = await api.post('/users', user);
  return response.data;
};

export const disableUser = async (username: string) => {
  await api.delete(`/users/${username}`);
};
