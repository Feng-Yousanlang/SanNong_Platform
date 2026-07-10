import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Layout from './Layout';
import { AuthProvider } from '../context/AuthContext';

function renderLayout(identity = '1', username = 'farmer01') {
  localStorage.setItem('auth_token', '1');
  localStorage.setItem('user_identity', identity);
  localStorage.setItem('user_id', '42');
  localStorage.setItem('user_username', username);

  return render(
    <MemoryRouter initialEntries={['/']}>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<div>child-page</div>} />
          </Route>
          <Route path="/login" element={<div>login-page</div>} />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('Layout', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('shows farmer navigation and user meta', () => {
    renderLayout('1');
    expect(screen.getByText('三农服务平台')).toBeInTheDocument();
    expect(screen.getByText('农户')).toBeInTheDocument();
    expect(screen.getByText('farmer01')).toBeInTheDocument();
    expect(screen.getByText('农产品管理')).toBeInTheDocument();
    expect(screen.getByText('child-page')).toBeInTheDocument();
  });

  it('logout navigates to login page', () => {
    renderLayout('2');
    fireEvent.click(screen.getByRole('button', { name: '退出' }));
    expect(screen.getByText('login-page')).toBeInTheDocument();
  });
});
