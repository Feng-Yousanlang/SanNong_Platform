import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { parseLoginResponse } from '../api/auth';
import { fetchProfile } from '../api/user';
import { pickValue } from '../api/utils';
import { getRoleLabel, isUserIdentity } from '../constants/roles';
import type { AuthContextValue, AuthState, UserIdentity } from '../types';

const AuthContext = createContext<AuthContextValue | null>(null);

const STORAGE_KEYS = {
  token: 'auth_token',
  identity: 'user_identity',
  userId: 'user_id',
  username: 'user_username',
} as const;

function readStoredAuth(): AuthState {
  try {
    const identity = localStorage.getItem(STORAGE_KEYS.identity) || '';
    return {
      token: localStorage.getItem(STORAGE_KEYS.token) || '',
      identity: isUserIdentity(identity) ? identity : '',
      userId: localStorage.getItem(STORAGE_KEYS.userId) || '',
      username: localStorage.getItem(STORAGE_KEYS.username) || '',
    };
  } catch {
    return { token: '', identity: '', userId: '', username: '' };
  }
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [auth, setAuth] = useState<AuthState>(readStoredAuth);

  useEffect(() => {
    const isAuthenticated = auth.token === '1' || auth.token === 'true';
    if (!isAuthenticated || auth.username || !auth.userId) return undefined;

    let cancelled = false;
    (async () => {
      try {
        const data = await fetchProfile(auth.userId);
        if (cancelled) return;
        const name = pickValue(data, ['username', 'user_name'], '');
        if (!name) return;
        localStorage.setItem(STORAGE_KEYS.username, name);
        setAuth((prev) => ({ ...prev, username: name }));
      } catch {
        // ignore profile fetch errors for display name backfill
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [auth.token, auth.username, auth.userId]);

  const value = useMemo<AuthContextValue>(() => {
    const isAuthenticated = auth.token === '1' || auth.token === 'true';

    return {
      ...auth,
      isAuthenticated,
      roleLabel: auth.identity ? getRoleLabel(auth.identity) : '未知角色',
      loginFromResponse(json, username = '') {
        const result = parseLoginResponse(json);
        if (!result.isSuccess) {
          return result;
        }

        const identityRaw = String(result.identity ?? '');
        const next: AuthState = {
          token: result.tokenValue,
          identity: isUserIdentity(identityRaw) ? (identityRaw as UserIdentity) : '',
          userId: String(result.userId ?? ''),
          username: username.trim(),
        };

        localStorage.setItem(STORAGE_KEYS.token, next.token);
        localStorage.setItem(STORAGE_KEYS.identity, next.identity);
        localStorage.setItem(STORAGE_KEYS.userId, next.userId);
        localStorage.setItem(STORAGE_KEYS.username, next.username);
        setAuth(next);
        return result;
      },
      logout() {
        localStorage.removeItem(STORAGE_KEYS.token);
        localStorage.removeItem(STORAGE_KEYS.identity);
        localStorage.removeItem(STORAGE_KEYS.userId);
        localStorage.removeItem(STORAGE_KEYS.username);
        setAuth({ token: '', identity: '', userId: '', username: '' });
      },
    };
  }, [auth]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
