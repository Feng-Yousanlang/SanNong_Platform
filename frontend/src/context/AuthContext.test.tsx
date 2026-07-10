import { beforeEach, describe, expect, it } from 'vitest';
import { act, render, screen, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';

function Probe() {
  const { isAuthenticated, roleLabel, loginFromResponse, logout } = useAuth();
  return (
    <div>
      <span data-testid="auth">{isAuthenticated ? 'yes' : 'no'}</span>
      <span data-testid="role">{roleLabel}</span>
      <button
        type="button"
        onClick={() =>
          loginFromResponse({ code: 200, data: { token: '1', identity: '2', id: '8' } })
        }
      >
        login
      </button>
      <button type="button" onClick={logout}>
        logout
      </button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('loginFromResponse should persist auth state', async () => {
    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>,
    );

    await act(async () => {
      screen.getByRole('button', { name: 'login' }).click();
    });

    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('yes');
    });
    expect(screen.getByTestId('role').textContent).toBe('买家');
    expect(localStorage.getItem('user_identity')).toBe('2');
  });

  it('logout should clear auth state', async () => {
    localStorage.setItem('auth_token', '1');
    localStorage.setItem('user_identity', '1');
    localStorage.setItem('user_id', '1');

    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>,
    );

    await act(async () => {
      screen.getByRole('button', { name: 'logout' }).click();
    });

    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('no');
    });
    expect(localStorage.getItem('auth_token')).toBeNull();
  });
});
