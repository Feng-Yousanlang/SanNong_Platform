import type { ApiResult, LoginParseResult, LoginResponseData, RegisterPayload } from '../types';
import { apiClient } from './client';

export async function loginWithPassword(username: string, password: string) {
  const { data } = await apiClient.post<ApiResult<LoginResponseData>>('/api/auth/login/pwd', {
    username,
    password,
  });
  return data;
}

export async function registerUser(payload: RegisterPayload) {
  const { data } = await apiClient.post<ApiResult<unknown>>('/api/auth/register', payload);
  return data;
}

function readNestedField(source: Record<string, unknown>, key: string): unknown {
  const nested = source.data;
  if (nested && typeof nested === 'object') {
    return (nested as Record<string, unknown>)[key];
  }
  return undefined;
}

export function parseLoginResponse(json: unknown): LoginParseResult {
  const root = (json && typeof json === 'object' ? json : {}) as Record<string, unknown>;
  const token = root.token ?? readNestedField(root, 'token');
  const identity = root.identity ?? readNestedField(root, 'identity');
  const userId = root.id ?? readNestedField(root, 'id');
  const tokenValue = String(token ?? '').trim();
  const code = root.code;
  const isSuccess =
    (code === 200 || code === '200') && (tokenValue === '1' || tokenValue === 'true');

  return {
    isSuccess,
    tokenValue,
    identity: identity != null ? String(identity) : undefined,
    userId: userId != null ? String(userId) : undefined,
    message: typeof root.message === 'string' ? root.message : undefined,
  };
}
