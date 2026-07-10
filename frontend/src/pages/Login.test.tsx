import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Login from './Login';
import { AuthProvider } from '../context/AuthContext';

vi.mock('../api/auth', () => ({
  loginWithPassword: vi.fn(),
  registerUser: vi.fn(),
}));

function renderLogin() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <Login />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('Login page', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('shows validation message when login fields are empty', async () => {
    renderLogin();
    fireEvent.click(screen.getByRole('button', { name: '登录' }));
    expect(await screen.findByText('请填写账号和密码')).toBeInTheDocument();
  });

  it('switches to register mode and validates required fields', async () => {
    renderLogin();
    fireEvent.click(screen.getByRole('button', { name: '注册账号' }));
    fireEvent.click(screen.getByRole('button', { name: '注册' }));
    expect(await screen.findByText('请填写全部信息')).toBeInTheDocument();
  });

  it('rejects mismatched passwords on register', async () => {
    renderLogin();
    fireEvent.click(screen.getByRole('button', { name: '注册账号' }));

    fireEvent.change(screen.getByLabelText('用户名'), { target: { value: 'buyer01' } });
    fireEvent.change(screen.getByLabelText('昵称'), { target: { value: '买家' } });
    fireEvent.change(screen.getByLabelText('密码'), { target: { value: '123456' } });
    fireEvent.change(screen.getByLabelText('确认密码'), { target: { value: '654321' } });
    fireEvent.change(screen.getByLabelText('身份类型'), { target: { value: '2' } });

    fireEvent.click(screen.getByRole('button', { name: '注册' }));
    expect(await screen.findByText('两次输入的密码不一致')).toBeInTheDocument();
  });
});
