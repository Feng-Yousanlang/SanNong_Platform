import { apiClient } from './client';
import { extractList } from './utils';

export async function fetchNews() {
  const { data } = await apiClient.get('/api/news');
  return extractList(data);
}
