import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchLoanProducts } from './loans';
import { apiClient } from './client';

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

describe('loans api', () => {
  beforeEach(() => {
    vi.mocked(apiClient.get).mockReset();
  });

  it('fetchLoanProducts unwraps success payload', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { code: 200, data: [{ fpName: '惠农贷' }] },
    });

    const products = await fetchLoanProducts();
    expect(products).toEqual([{ fpName: '惠农贷' }]);
    expect(apiClient.get).toHaveBeenCalledWith('/api/loan/products');
  });
});
