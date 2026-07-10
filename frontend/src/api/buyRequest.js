import { apiClient } from './client';
import { unwrapResponse } from './utils';

export async function fetchBuyRequestList() {
  const { data } = await apiClient.get('/api/buyRequest/list');
  return unwrapResponse(data);
}

export async function publishBuyRequest(body) {
  const { data } = await apiClient.post('/api/buyRequest/publish', body);
  return data;
}

export async function deleteBuyRequest(buyRequestId, userId) {
  const { data } = await apiClient.delete('/api/buyRequest/delete', {
    params: { buyRequestId, userId },
  });
  return data;
}

export async function searchBuyRequest(body) {
  const { data } = await apiClient.post('/api/buyRequest/search', body);
  return unwrapResponse(data);
}
