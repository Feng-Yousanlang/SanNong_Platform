import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchBuyRequestList, publishBuyRequest } from './buyRequest';
import { apiClient } from './client';

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('buyRequest api', () => {
  beforeEach(() => {
    vi.mocked(apiClient.get).mockReset();
    vi.mocked(apiClient.post).mockReset();
  });

  it('fetchBuyRequestList unwraps list payload', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { code: 200, data: [{ title: '求购大米' }] },
    });

    const list = await fetchBuyRequestList();
    expect(list).toEqual([{ title: '求购大米' }]);
  });

  it('publishBuyRequest posts body to backend', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({
      data: { code: 200, message: '发布成功' },
    });

    const body = { title: '求购', content: '100斤', contact: '13800000000' };
    const result = await publishBuyRequest(body);
    expect(result.code).toBe(200);
    expect(apiClient.post).toHaveBeenCalledWith('/api/buyRequest/publish', body);
  });
});
