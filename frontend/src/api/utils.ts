import type { ApiResult } from '../types';
import { API_BASE } from './client';

export function unwrapResponse<T>(json: ApiResult<T>): T {
  if (json?.code === 200) {
    return (json.data !== undefined ? json.data : json) as T;
  }
  throw new Error(json?.message || '请求失败');
}

export function extractList(payload: unknown): unknown[] {
  if (!payload || typeof payload !== 'object') return [];
  const obj = payload as Record<string, unknown>;

  if (Array.isArray(payload)) return payload;
  if (Array.isArray(obj.data)) return obj.data;
  if (Array.isArray(obj.newsList)) return obj.newsList;

  const data = obj.data;
  if (data && typeof data === 'object') {
    const nested = data as Record<string, unknown>;
    if (Array.isArray(nested.newsList)) return nested.newsList;
    if (Array.isArray(nested.records)) return nested.records;
    if (Array.isArray(nested.list)) return nested.list;
    if (Array.isArray(nested.items)) return nested.items;
    if (Array.isArray(nested.products)) return nested.products;
    if (Array.isArray(nested.experts)) return nested.experts;
    if (!Array.isArray(data)) return [data];
  }

  if (Array.isArray(obj.products)) return obj.products;
  if (Array.isArray(obj.experts)) return obj.experts;
  return [];
}

export const DEFAULT_IMG =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="300" height="200"><rect fill="#e8f5e9" width="100%" height="100%"/><text x="50%" y="50%" fill="#2e7d32" font-size="16" text-anchor="middle" dy=".3em">暂无图片</text></svg>',
  );

export function resolveImage(url: unknown): string {
  if (!url) return DEFAULT_IMG;
  const trimmed = String(url).trim();
  if (!trimmed) return DEFAULT_IMG;

  const sameOriginPath = (() => {
    if (trimmed.startsWith('/products/') || trimmed.startsWith('/news/')) return trimmed;
    const m = trimmed.match(/^https?:\/\/[^/]+(\/(?:products|news)\/.+)$/);
    return m ? m[1] : null;
  })();
  if (sameOriginPath) return sameOriginPath;

  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
    return trimmed;
  }
  const path = trimmed.startsWith('/') ? trimmed : `/${trimmed}`;
  return `${API_BASE}${path}`;
}

export function formatDate(value: unknown): string {
  if (!value) return '—';
  const date = new Date(String(value));
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString('zh-CN');
}

export function pickValue(
  obj: Record<string, unknown> | null | undefined,
  keys: string[],
  fallback = '—',
): unknown {
  if (!obj) return fallback;
  for (const key of keys) {
    const value = obj[key];
    if (value !== undefined && value !== null && value !== '') {
      return value;
    }
  }
  return fallback;
}
