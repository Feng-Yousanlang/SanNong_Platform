import { apiClient } from './client';
import { unwrapResponse } from './utils';

export async function fetchUserList(userId) {
  const { data } = await apiClient.get('/api/user/list', { params: { userId } });
  return unwrapResponse(data);
}

export async function updateUserPermission(body) {
  const { data } = await apiClient.post('/api/user/auth/update', body);
  return data;
}

export async function deleteUser(body) {
  const { data } = await apiClient.post('/api/user/auth/delete', body);
  return data;
}
