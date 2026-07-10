import { beforeEach, describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import RoleGuard from './RoleGuard';
import { AuthProvider } from '../context/AuthContext';

function renderWithRole(identity: string, allowed: string[]) {
  localStorage.setItem('auth_token', '1');
  localStorage.setItem('user_identity', identity);
  localStorage.setItem('user_id', '1');

  return render(
    <MemoryRouter>
      <AuthProvider>
        <RoleGuard allowed={allowed as never}>
          <div>protected-content</div>
        </RoleGuard>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('RoleGuard', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('renders children when role is allowed', () => {
    renderWithRole('2', ['2']);
    expect(screen.getByText('protected-content')).toBeInTheDocument();
  });

  it('blocks farmer from buyer-only route', () => {
    renderWithRole('1', ['2']);
    expect(screen.queryByText('protected-content')).not.toBeInTheDocument();
  });

  it('allows admin on admin route', () => {
    renderWithRole('5', ['5']);
    expect(screen.getByText('protected-content')).toBeInTheDocument();
  });
});
