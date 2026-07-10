import { beforeEach, describe, expect, it } from 'vitest';
import type { InternalAxiosRequestConfig } from 'axios';
import { apiClient } from './client';

describe('apiClient', () => {
  let lastConfig: InternalAxiosRequestConfig | undefined;

  beforeEach(() => {
    localStorage.clear();
    lastConfig = undefined;
    apiClient.defaults.adapter = async (config) => {
      lastConfig = config;
      return {
        data: {},
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      };
    };
  });

  it('attaches bearer token from localStorage', async () => {
    localStorage.setItem('auth_token', '1');
    await apiClient.get('/api/news');
    expect(lastConfig?.headers?.Authorization).toBe('Bearer 1');
  });

  it('does not force json content-type for FormData uploads', async () => {
    const formData = new FormData();
    formData.append('file', new Blob(['x']), 'a.txt');
    await apiClient.post('/api/loan/apply', formData);
    const contentType = String(lastConfig?.headers?.['Content-Type'] ?? '');
    expect(contentType).not.toBe('application/json');
  });
});
