import { beforeEach, describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import { AuthProvider } from '../context/AuthContext';

function renderProtected(token: string) {
  if (token) {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('user_identity', '1');
    localStorage.setItem('user_id', '1');
  } else {
    localStorage.clear();
  }

  return render(
    <MemoryRouter initialEntries={['/private']}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<div>login-page</div>} />
          <Route
            path="/private"
            element={
              <ProtectedRoute>
                <div>private-page</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('renders children when authenticated', () => {
    renderProtected('1');
    expect(screen.getByText('private-page')).toBeInTheDocument();
  });

  it('redirects to login when unauthenticated', () => {
    renderProtected('');
    expect(screen.getByText('login-page')).toBeInTheDocument();
    expect(screen.queryByText('private-page')).not.toBeInTheDocument();
  });
});
