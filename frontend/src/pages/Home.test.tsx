import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Home from './Home';
import { AuthProvider } from '../context/AuthContext';

vi.mock('../api/news', () => ({
  fetchNews: vi.fn(),
}));

import { fetchNews } from '../api/news';

function renderHome(identity = '1', username = 'farmer01') {
  localStorage.setItem('auth_token', '1');
  localStorage.setItem('user_identity', identity);
  localStorage.setItem('user_id', '1');
  localStorage.setItem('user_username', username);

  return render(
    <MemoryRouter>
      <AuthProvider>
        <Home />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('Home page', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.mocked(fetchNews).mockReset();
  });

  it('renders feature cards for farmer', async () => {
    vi.mocked(fetchNews).mockResolvedValue([{ title: '春耕', imgUrl: '/a.jpg', newsUrl: 'https://x.com' }]);
    renderHome('1');

    expect(screen.getByText('欢迎回来')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getAllByText('春耕').length).toBeGreaterThan(0);
    });
    expect(screen.getByText('功能入口')).toBeInTheDocument();
    expect(screen.getByText('账户概览')).toBeInTheDocument();
    expect(screen.queryByText('页面导航')).not.toBeInTheDocument();
  });

  it('shows empty news message when list is empty', async () => {
    vi.mocked(fetchNews).mockResolvedValue([]);
    renderHome('2');

    await waitFor(() => {
      expect(screen.getByText('暂无新闻')).toBeInTheDocument();
    });
  });

  it('hides news section for bank staff', async () => {
    vi.mocked(fetchNews).mockResolvedValue([]);
    renderHome('4');

    await waitFor(() => {
      expect(screen.queryByText('新闻轮播')).not.toBeInTheDocument();
    });
    expect(screen.getByText('功能入口')).toBeInTheDocument();
  });
});
