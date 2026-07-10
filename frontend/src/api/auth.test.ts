import { beforeEach, describe, expect, it } from 'vitest';
import { parseLoginResponse } from './auth';

describe('parseLoginResponse', () => {
  it('accepts nested success payload from backend', () => {
    const result = parseLoginResponse({
      code: 200,
      data: { token: '1', identity: '2', id: '42' },
    });

    expect(result.isSuccess).toBe(true);
    expect(result.identity).toBe('2');
    expect(result.userId).toBe('42');
  });

  it('accepts string code 200', () => {
    const result = parseLoginResponse({
      code: '200',
      data: { token: 'true', identity: '1', id: '7' },
    });

    expect(result.isSuccess).toBe(true);
    expect(result.tokenValue).toBe('true');
  });

  it('rejects invalid token even when code is 200', () => {
    const result = parseLoginResponse({
      code: 200,
      data: { token: 'invalid', identity: '1', id: '1' },
    });

    expect(result.isSuccess).toBe(false);
  });

  it('reads flat API response shape', () => {
    const result = parseLoginResponse({
      code: 200,
      token: '1',
      identity: '1',
      id: '9',
    });

    expect(result.isSuccess).toBe(true);
    expect(result.userId).toBe('9');
  });

  it('returns message from backend error payload', () => {
    const result = parseLoginResponse({ code: 401, message: '用户名或密码错误' });
    expect(result.isSuccess).toBe(false);
    expect(result.message).toBe('用户名或密码错误');
  });
});
