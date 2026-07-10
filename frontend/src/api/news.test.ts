import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchNews } from './news';
import { apiClient } from './client';

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

describe('fetchNews', () => {
  beforeEach(() => {
    vi.mocked(apiClient.get).mockReset();
  });

  it('extracts newsList from backend payload', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { code: 200, data: { newsList: [{ title: '春耕' }] } },
    });

    const list = await fetchNews();
    expect(list).toEqual([{ title: '春耕' }]);
    expect(apiClient.get).toHaveBeenCalledWith('/api/news');
  });
});
