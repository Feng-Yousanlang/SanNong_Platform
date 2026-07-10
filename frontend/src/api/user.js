import { apiClient } from './client';
import { unwrapResponse } from './utils';

export async function fetchProfile(userId) {
  const { data } = await apiClient.get('/api/user/profile', { params: { userId } });
  return unwrapResponse(data);
}

export async function updateProfile(formData) {
  const { data } = await apiClient.put('/api/user/profile/update', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function uploadAvatar(formData) {
  const { data } = await apiClient.post('/api/user/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}
