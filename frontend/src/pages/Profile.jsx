import { useCallback, useEffect, useMemo, useState } from 'react';
import { fetchProfile, updateProfile, uploadAvatar } from '../api/user';
import { formatDate, pickValue, resolveImage } from '../api/utils';
import { ROLE_LABELS } from '../constants/roles';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';

function profileCompleteness(form) {
  const fields = [form.realName, form.phone, form.email];
  const filled = fields.filter((v) => String(v || '').trim()).length;
  return Math.round((filled / fields.length) * 100);
}

export default function Profile() {
  const { userId, roleLabel } = useAuth();
  const [profile, setProfile] = useState(null);
  const [msg, setMsg] = useState('');
  const [form, setForm] = useState({ realName: '', phone: '', email: '' });
  const [avatarFile, setAvatarFile] = useState(null);

  const load = useCallback(async () => {
    if (!userId) return;
    try {
      const data = await fetchProfile(userId);
      setProfile(data);
      setForm({
        realName: pickValue(data, ['realName', 'real_name'], ''),
        phone: pickValue(data, ['phone', 'mobile'], ''),
        email: pickValue(data, ['email'], ''),
      });
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleSave(e) {
    e.preventDefault();
    setMsg('保存中...');
    const formData = new FormData();
    formData.append('userId', userId);
    formData.append('real_name', form.realName);
    formData.append('phone', form.phone);
    formData.append('email', form.email);
    if (avatarFile) formData.append('file', avatarFile);

    try {
      await updateProfile(formData);
      if (avatarFile) {
        const avatarData = new FormData();
        avatarData.append('userId', userId);
        avatarData.append('file', avatarFile);
        await uploadAvatar(avatarData);
      }
      setMsg('保存成功');
      setAvatarFile(null);
      load();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  const identity = pickValue(profile, ['identity', 'roleType', 'role_type'], '');
  const roleName = ROLE_LABELS[String(identity)] || roleLabel || identity;
  const username = pickValue(profile, ['username', 'user_name'], '—');
  const displayName = form.realName || username;
  const createdAt = formatDate(pickValue(profile, ['createTime', 'create_time'], ''));
  const avatarSrc = resolveImage(pickValue(profile, ['image_url', 'avatarUrl', 'avatar'], ''));
  const completeness = useMemo(() => profileCompleteness(form), [form]);

  return (
    <div className="page-content profile-page">
      <PageHeader
        eyebrow="账户管理"
        title="个人中心"
        subtitle="查看与编辑个人账户信息，完善资料以便平台为您提供更精准的服务"
      />

      <div className="profile-layout">
        <aside className="profile-panel card">
          <div className="profile-panel-top">
            <div className="profile-avatar-wrap">
              <span className="profile-avatar-ring" aria-hidden="true" />
              <img src={avatarSrc} alt="头像" className="profile-avatar" />
            </div>
            <h2 className="profile-display-name">{displayName}</h2>
            <span className="profile-role-pill">{roleName}</span>
          </div>

          <div className="profile-panel-body">
            <div className="profile-info-grid">
              <div className="profile-info-item">
                <span className="profile-info-label">用户名</span>
                <span className="profile-info-value">{username}</span>
              </div>
              <div className="profile-info-item">
                <span className="profile-info-label">角色类型</span>
                <span className="profile-info-value">{roleName}</span>
              </div>
              <div className="profile-info-item profile-info-item-wide">
                <span className="profile-info-label">注册时间</span>
                <span className="profile-info-value">{createdAt || '—'}</span>
              </div>
            </div>

            <div className="profile-aside-block">
              <span className="profile-aside-label">账户提示</span>
              <p>
                完善真实姓名、手机号与邮箱后，可在融资、交易与预约流程中使用更完整的身份信息。头像支持 JPG、PNG 格式。
              </p>
            </div>
          </div>
        </aside>

        <div className="profile-main">
          <section className="card profile-form-card">
            <div className="card-section-head">
              <h2>编辑资料</h2>
              <p>更新联系方式与头像，信息将同步至您的平台账户</p>
            </div>

            <form onSubmit={handleSave}>
              <div className="profile-form-grid">
                <div className="form-row">
                  <label htmlFor="profile-realName">真实姓名</label>
                  <input
                    id="profile-realName"
                    value={form.realName}
                    onChange={(e) => setForm({ ...form, realName: e.target.value })}
                    required
                  />
                </div>
                <div className="form-row">
                  <label htmlFor="profile-phone">手机号</label>
                  <input
                    id="profile-phone"
                    value={form.phone}
                    onChange={(e) => setForm({ ...form, phone: e.target.value })}
                    required
                  />
                </div>
                <div className="form-row">
                  <label htmlFor="profile-email">邮箱</label>
                  <input
                    id="profile-email"
                    type="email"
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                    required
                  />
                </div>
                <div className="form-row">
                  <label htmlFor="profile-avatar">更换头像</label>
                  <input
                    id="profile-avatar"
                    type="file"
                    accept="image/*"
                    onChange={(e) => setAvatarFile(e.target.files?.[0] || null)}
                  />
                </div>
              </div>

              <div className="profile-form-actions">
                <button type="submit" className="btn btn-primary">
                  保存修改
                </button>
                {msg ? <p className="msg msg-info">{msg}</p> : null}
              </div>
            </form>
          </section>

          <div className="profile-highlights">
            <div className="profile-highlight profile-highlight--soft">
              <span className="profile-highlight-label">资料完整度</span>
              <span className="profile-highlight-value">{completeness}% 已填写</span>
            </div>
            <div className="profile-highlight profile-highlight--accent">
              <span className="profile-highlight-label">当前角色</span>
              <span className="profile-highlight-value">{roleName}</span>
            </div>
            <div className="profile-highlight profile-highlight--muted">
              <span className="profile-highlight-label">加入平台</span>
              <span className="profile-highlight-value">{createdAt || '待同步'}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
