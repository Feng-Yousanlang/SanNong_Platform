import { useCallback, useEffect, useState } from 'react';
import { deleteUser, fetchUserList, updateUserPermission } from '../api/admin';
import { ROLE_LABELS } from '../constants/roles';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';

export default function Admin() {
  const { userId } = useAuth();
  const [users, setUsers] = useState([]);
  const [filterId, setFilterId] = useState('');
  const [msg, setMsg] = useState('');

  const load = useCallback(
    async (filterValue = '') => {
      try {
        const queryId = filterValue ? Number(filterValue) : Number(userId);
        const data = await fetchUserList(queryId);
        setUsers(Array.isArray(data) ? data : []);
        setMsg('');
      } catch (err) {
        setMsg(err.response?.data?.message || err.message);
      }
    },
    [userId],
  );

  useEffect(() => {
    load();
  }, [load]);

  async function handleUpdateRole(targetUserId) {
    const newRole = prompt('请输入新角色 (1:农户 2:买家 3:专家 4:银行 5:管理员):');
    if (!newRole) return;
    try {
      const res = await updateUserPermission({
        userId: Number(userId),
        roleType: parseInt(newRole, 10),
        userId_change: targetUserId,
      });
      alert(res.message || '权限已更新');
      load();
    } catch (err) {
      alert(err.response?.data?.message || err.message);
    }
  }

  async function handleDelete(targetUserId) {
    if (!confirm('确定删除该用户？')) return;
    try {
      const res = await deleteUser({ userId: Number(userId), userId_delete: targetUserId });
      alert(res.message || '用户已删除');
      load();
    } catch (err) {
      alert(err.response?.data?.message || err.message);
    }
  }

  return (
    <div>
      <PageHeader title="用户管理" subtitle="平台管理员 · 用户列表与权限管理" />

      <section className="card">
        <div className="grid-2">
          <div className="form-row">
            <label>按用户 ID 筛选</label>
            <input value={filterId} onChange={(e) => setFilterId(e.target.value)} placeholder="留空则查询全部" />
          </div>
          <div className="form-row">
            <label>&nbsp;</label>
            <button type="button" className="btn btn-primary" onClick={() => load(filterId)}>
              查询
            </button>
          </div>
        </div>
        {msg ? <p className="msg msg-info">{msg}</p> : null}

        <div className="grid-2" style={{ marginTop: '1rem' }}>
          {users.length ? (
            users.map((u) => (
              <div key={u.userId} className="item-card">
                <h4>{u.username}</h4>
                <p>ID：{u.userId}</p>
                <p>角色：{ROLE_LABELS[String(u.roleType)] || u.roleType}</p>
                <div className="item-actions">
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => handleUpdateRole(u.userId)}>
                    修改权限
                  </button>
                  <button type="button" className="btn btn-danger btn-sm" onClick={() => handleDelete(u.userId)}>
                    删除用户
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="empty">暂无用户数据</div>
          )}
        </div>
      </section>
    </div>
  );
}
