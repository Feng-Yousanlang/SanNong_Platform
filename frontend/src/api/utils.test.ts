import { describe, expect, it } from 'vitest';
import {
  DEFAULT_IMG,
  extractList,
  formatDate,
  pickValue,
  resolveImage,
  unwrapResponse,
} from './utils';

describe('api utils', () => {
  it('unwrapResponse should return data on success', () => {
    expect(unwrapResponse({ code: 200, data: { ok: true } })).toEqual({ ok: true });
  });

  it('unwrapResponse should throw on failure', () => {
    expect(() => unwrapResponse({ code: 500, message: '失败' })).toThrow('失败');
  });

  it('extractList should handle multiple backend shapes', () => {
    expect(extractList([1, 2])).toEqual([1, 2]);
    expect(extractList({ data: [3] })).toEqual([3]);
    expect(extractList({ newsList: [4] })).toEqual([4]);
    expect(extractList({ data: { products: [5] } })).toEqual([5]);
    expect(extractList({ data: { title: 'single' } })).toEqual([{ title: 'single' }]);
    expect(extractList({ data: { records: [6] } })).toEqual([6]);
    expect(extractList({ data: { list: [7] } })).toEqual([7]);
    expect(extractList({ products: [8] })).toEqual([8]);
    expect(extractList({ data: { experts: [{ name: '王专家' }] } })).toEqual([{ name: '王专家' }]);
    expect(extractList(null)).toEqual([]);
  });

  it('resolveImage should normalize relative and absolute urls', () => {
    expect(resolveImage('')).toBe(DEFAULT_IMG);
    expect(resolveImage('https://cdn.example.com/a.jpg')).toBe('https://cdn.example.com/a.jpg');
    expect(resolveImage('uploads/a.jpg')).toContain('/uploads/a.jpg');
    expect(resolveImage('/uploads/a.jpg')).toContain('/uploads/a.jpg');
    expect(resolveImage('http://localhost:8080/products/apple.svg')).toBe('/products/apple.svg');
    expect(resolveImage('/products/apple.svg')).toBe('/products/apple.svg');
  });

  it('formatDate should format valid dates and fallback safely', () => {
    expect(formatDate(null)).toBe('—');
    expect(formatDate('not-a-date')).toBe('not-a-date');
    expect(formatDate('2026-01-01T00:00:00Z')).toContain('2026');
  });

  it('pickValue should read first non-empty field', () => {
    expect(pickValue({ a: '', b: 'value' }, ['a', 'b'])).toBe('value');
    expect(pickValue(null, ['a'], 'fallback')).toBe('fallback');
  });
});
