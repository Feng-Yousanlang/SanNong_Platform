import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { filterNavByIdentity } from '../constants/nav';

export default function Layout() {
  const { username, roleLabel, identity, logout } = useAuth();
  const navigate = useNavigate();
  const navItems = filterNavByIdentity(identity);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="app-shell">
      <div className="app-shell-bg" aria-hidden="true" />
      <header className="app-header">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true" />
          <span className="brand-text">三农服务平台</span>
        </div>
        <nav className="app-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) => (isActive ? 'active' : undefined)}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="header-user">
          <span className="user-meta">{username || '用户'}</span>
          <span className="role-tag">{roleLabel}</span>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>
            退出
          </button>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
      <footer className="app-footer">
        <div className="app-footer-inner">
          <span className="app-footer-line" aria-hidden="true" />
          <p>三农服务平台 · 智慧农业综合服务体系</p>
        </div>
      </footer>
    </div>
  );
}
