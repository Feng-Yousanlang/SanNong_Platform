import { apiClient } from './client';
import { unwrapResponse } from './utils';

export async function fetchKnowledgeList(page = 1, pageSize = 5, q = '') {
  const { data } = await apiClient.get('/api/knowledge/list', {
    params: { page, page_size: pageSize, q },
  });
  return data;
}

export async function addKnowledge(body) {
  const { data } = await apiClient.post('/api/knowledge/add', body);
  return data;
}

export async function updateKnowledge(body) {
  const { data } = await apiClient.post('/api/knowledge/update', body);
  return data;
}

export async function deleteKnowledge(body) {
  const { data } = await apiClient.post('/api/knowledge/delete', body);
  return data;
}

export async function fetchKnowledgeDetail(id) {
  const { data } = await apiClient.get(`/api/knowledge/${id}`);
  return unwrapResponse(data);
}
