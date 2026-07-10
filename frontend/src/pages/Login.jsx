import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { loginWithPassword, registerUser } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import { REGISTER_ROLE_OPTIONS } from '../constants/roles';
import './Login.css';

export default function Login() {
  const navigate = useNavigate();
  const { isAuthenticated, loginFromResponse } = useAuth();
  const [mode, setMode] = useState('login');

  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [registerForm, setRegisterForm] = useState({
    username: '',
    name: '',
    password: '',
    passwordConfirm: '',
    identity: '',
  });
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleLoginSubmit(event) {
    event.preventDefault();
    setMessage('');

    if (!loginForm.username || !loginForm.password) {
      setMessage('请填写账号和密码');
      return;
    }

    setLoading(true);
    try {
      const json = await loginWithPassword(loginForm.username, loginForm.password);
      const result = loginFromResponse(json, loginForm.username);
      if (result.isSuccess) {
        navigate('/', { replace: true });
      } else {
        setMessage(json.message || '登录失败，请检查账号密码');
      }
    } catch (error) {
      setMessage(error.response?.data?.message || '无法连接后端，请确认 localhost:8080 已启动');
    } finally {
      setLoading(false);
    }
  }

  async function handleRegisterSubmit(event) {
    event.preventDefault();
    setMessage('');

    const { username, name, password, passwordConfirm, identity } = registerForm;
    if (!username || !name || !password || !passwordConfirm || !identity) {
      setMessage('请填写全部信息');
      return;
    }
    if (password !== passwordConfirm) {
      setMessage('两次输入的密码不一致');
      return;
    }

    setLoading(true);
    try {
      const json = await registerUser({
        username,
        name,
        password,
        passwordConfirm,
        identity,
      });
      if (json.code === 200) {
        setMessage('注册成功，请使用账号登录');
        setMode('login');
        setLoginForm({ username, password: '' });
      } else {
        setMessage(json.message || '注册失败');
      }
    } catch (error) {
      setMessage(error.response?.data?.message || '注册请求失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <main className="login-main">
        <section className="login-showcase" aria-label="平台介绍">
          <div className="login-showcase-pattern" aria-hidden="true" />
          <div className="login-showcase-inner">
            <div className="brand">
              <span className="brand-mark" aria-hidden="true" />
              <span className="brand-text">三农服务平台</span>
            </div>
            <h1 className="login-showcase-title">连接农业全产业链</h1>
            <p className="login-showcase-desc">
              为农户、买家、专家、银行与管理员提供统一的数字化服务入口
            </p>
            <div className="login-showcase-grid">
              <div className="showcase-panel">
                <span className="showcase-panel-label">融资信贷</span>
                <strong>贷款申请与审批</strong>
              </div>
              <div className="showcase-panel">
                <span className="showcase-panel-label">产销对接</span>
                <strong>商城与求购平台</strong>
              </div>
              <div className="showcase-panel">
                <span className="showcase-panel-label">专家知识</span>
                <strong>预约咨询与知识库</strong>
              </div>
            </div>
          </div>
        </section>

        <section className="login-form-panel">
          <div className="login-card">
          <div className="login-tabs">
            <button
              type="button"
              className={mode === 'login' ? 'active' : ''}
              onClick={() => {
                setMode('login');
                setMessage('');
              }}
            >
              账号登录
            </button>
            <button
              type="button"
              className={mode === 'register' ? 'active' : ''}
              onClick={() => {
                setMode('register');
                setMessage('');
              }}
            >
              注册账号
            </button>
          </div>

          {mode === 'login' ? (
            <form onSubmit={handleLoginSubmit} className="login-form">
              <label>
                账号
                <input
                  value={loginForm.username}
                  onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })}
                  placeholder="请输入账号"
                  autoComplete="username"
                />
              </label>
              <label>
                密码
                <input
                  type="password"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                  placeholder="请输入密码"
                  autoComplete="current-password"
                />
              </label>
              <button type="submit" className="primary-btn" disabled={loading}>
                {loading ? '登录中...' : '登录'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegisterSubmit} className="login-form">
              <label>
                用户名
                <input
                  value={registerForm.username}
                  onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })}
                  placeholder="设置用户名"
                />
              </label>
              <label>
                昵称
                <input
                  value={registerForm.name}
                  onChange={(e) => setRegisterForm({ ...registerForm, name: e.target.value })}
                  placeholder="设置昵称"
                />
              </label>
              <label>
                密码
                <input
                  type="password"
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                  placeholder="设置密码"
                />
              </label>
              <label>
                确认密码
                <input
                  type="password"
                  value={registerForm.passwordConfirm}
                  onChange={(e) =>
                    setRegisterForm({ ...registerForm, passwordConfirm: e.target.value })
                  }
                  placeholder="再次输入密码"
                />
              </label>
              <label>
                身份类型
                <select
                  value={registerForm.identity}
                  onChange={(e) => setRegisterForm({ ...registerForm, identity: e.target.value })}
                >
                  <option value="">请选择</option>
                  {REGISTER_ROLE_OPTIONS.map((role) => (
                    <option key={role.value} value={role.value}>
                      {role.label}
                    </option>
                  ))}
                </select>
              </label>
              <p className="register-hint">
                仅支持注册农户、买家。专家 / 银行 / 管理员账号由平台管理员分发，请联系管理员获取。
              </p>
              <button type="submit" className="primary-btn" disabled={loading}>
                {loading ? '注册中...' : '注册'}
              </button>
            </form>
          )}

          {message ? <p className="form-message">{message}</p> : null}
          </div>
        </section>
      </main>
    </div>
  );
}
