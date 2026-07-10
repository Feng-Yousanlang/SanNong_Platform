import { useCallback, useEffect, useState } from 'react';
import { deleteBuyRequest, fetchBuyRequestList, publishBuyRequest, searchBuyRequest } from '../api/buyRequest';
import { formatDate } from '../api/utils';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';

export default function Need() {
  const { userId } = useAuth();
  const [items, setItems] = useState([]);
  const [msg, setMsg] = useState('');
  const [keyword, setKeyword] = useState('');
  const [sort, setSort] = useState('time');
  const [form, setForm] = useState({ title: '', content: '', contact: '' });

  const load = useCallback(async () => {
    try {
      const data = await fetchBuyRequestList();
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setMsg(err.message);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function handlePublish(e) {
    e.preventDefault();
    try {
      const res = await publishBuyRequest({ ...form, userId: Number(userId) });
      setMsg(res.message || '发布成功');
      setForm({ title: '', content: '', contact: '' });
      load();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleSearch() {
    try {
      const data = await searchBuyRequest({ keyword, sort });
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setMsg(err.message);
    }
  }

  return (
    <div>
      <PageHeader title="求购平台" subtitle="发布与浏览农产品求购信息" />

      <section className="card">
        <h3>发布求购</h3>
        <form onSubmit={handlePublish}>
          <div className="form-row">
            <label>标题</label>
            <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
          </div>
          <div className="form-row">
            <label>详细需求</label>
            <textarea value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} required />
          </div>
          <div className="form-row">
            <label>联系方式</label>
            <input value={form.contact} onChange={(e) => setForm({ ...form, contact: e.target.value })} />
          </div>
          <button type="submit" className="btn btn-primary">
            发布
          </button>
        </form>
      </section>

      <section className="card">
        <h3>求购列表</h3>
        <div className="grid-2">
          <div className="form-row">
            <label>关键词</label>
            <input value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          </div>
          <div className="form-row">
            <label>排序</label>
            <select value={sort} onChange={(e) => setSort(e.target.value)}>
              <option value="time">按时间</option>
              <option value="title">按标题</option>
            </select>
          </div>
        </div>
        <button type="button" className="btn btn-secondary" onClick={handleSearch}>
          搜索
        </button>
        <button type="button" className="btn btn-secondary" style={{ marginLeft: '0.5rem' }} onClick={load}>
          刷新全部
        </button>
        {msg ? <p className="msg msg-info">{msg}</p> : null}

        {items.length ? (
          items.map((item) => (
            <div key={item.id || item.buyRequestId} className="item-card" style={{ marginTop: '0.75rem' }}>
              <h4>{item.title}</h4>
              <p>{item.content}</p>
              <p>
                联系：{item.contact || '—'} · {formatDate(item.createTime)}
              </p>
              {String(item.userId) === String(userId) ? (
                <button
                  type="button"
                  className="btn btn-danger btn-sm"
                  onClick={async () => {
                    if (!confirm('确定删除？')) return;
                    await deleteBuyRequest(item.id || item.buyRequestId, userId);
                    load();
                  }}
                >
                  删除
                </button>
              ) : null}
            </div>
          ))
        ) : (
          <div className="empty">暂无求购信息</div>
        )}
      </section>
    </div>
  );
}
