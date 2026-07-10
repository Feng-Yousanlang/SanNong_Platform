import { useCallback, useEffect, useState } from 'react';
import { addKnowledge, deleteKnowledge, fetchKnowledgeList, updateKnowledge } from '../api/knowledge';
import { formatDate } from '../api/utils';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';

export default function Knowledge() {
  const { userId, identity } = useAuth();
  const canEdit = ['3', '5'].includes(identity);

  const [page, setPage] = useState(1);
  const [query, setQuery] = useState('');
  const [items, setItems] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [msg, setMsg] = useState('');
  const [form, setForm] = useState({ title: '', content: '', category: '' });
  const [editingId, setEditingId] = useState(null);

  const load = useCallback(async () => {
    setMsg('加载中...');
    try {
      const res = await fetchKnowledgeList(page, 5, query);
      const list = Array.isArray(res.data) ? res.data : res.data?.list || [];
      setItems(list);
      setTotalPages(res.data?.totalPages || res.totalPages || 1);
      setMsg('');
    } catch (err) {
      setMsg(err.message);
    }
  }, [page, query]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = { ...form, userId: Number(userId) };
    try {
      const res = editingId
        ? await updateKnowledge({ ...payload, id: editingId })
        : await addKnowledge(payload);
      setMsg(res.message || '保存成功');
      setForm({ title: '', content: '', category: '' });
      setEditingId(null);
      load();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  return (
    <div>
      <PageHeader title="知识库" subtitle="农业知识检索与内容管理" />

      <section className="card">
        <div className="grid-2">
          <div className="form-row">
            <label>搜索关键词</label>
            <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="输入关键词后搜索" />
          </div>
          <div className="form-row" style={{ justifyContent: 'flex-end' }}>
            <label>&nbsp;</label>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => {
                setPage(1);
                load();
              }}
            >
              搜索
            </button>
          </div>
        </div>
        {msg ? <p className="msg msg-info">{msg}</p> : null}
      </section>

      {canEdit && (
        <section className="card">
          <h3>{editingId ? '编辑知识' : '发布知识'}</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <label>标题</label>
              <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
            </div>
            <div className="form-row">
              <label>分类</label>
              <input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
            </div>
            <div className="form-row">
              <label>内容</label>
              <textarea value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} required />
            </div>
            <div className="item-actions">
              <button type="submit" className="btn btn-primary">
                {editingId ? '更新' : '发布'}
              </button>
              {editingId ? (
                <button type="button" className="btn btn-secondary" onClick={() => { setEditingId(null); setForm({ title: '', content: '', category: '' }); }}>
                  取消编辑
                </button>
              ) : null}
            </div>
          </form>
        </section>
      )}

      <section className="card">
        <h3>知识列表</h3>
        {items.length ? (
          items.map((item) => (
            <div key={item.id} className="item-card" style={{ marginBottom: '0.75rem' }}>
              <h4>{item.title}</h4>
              <p>
                {item.source ? `[${item.source}] ` : ''}
                {formatDate(item.publish || item.createTime || item.createdAt)}
              </p>
              {item.url ? (
                <p>
                  <a href={item.url} target="_blank" rel="noopener noreferrer">
                    阅读原文
                  </a>
                </p>
              ) : null}
              <p style={{ whiteSpace: 'pre-wrap' }}>{item.content || item.source || '暂无摘要'}</p>
              {canEdit ? (
                <div className="item-actions">
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => {
                      setEditingId(item.id);
                      setForm({ title: item.title, content: item.content, category: item.category || '' });
                    }}
                  >
                    编辑
                  </button>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={async () => {
                      if (!confirm('确定删除？')) return;
                      await deleteKnowledge({ id: item.id, userId: Number(userId) });
                      load();
                    }}
                  >
                    删除
                  </button>
                </div>
              ) : null}
            </div>
          ))
        ) : (
          <div className="empty">暂无知识条目</div>
        )}
        <div className="item-actions" style={{ marginTop: '1rem' }}>
          <button type="button" className="btn btn-secondary btn-sm" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
            上一页
          </button>
          <span>
            第 {page} / {totalPages} 页
          </span>
          <button type="button" className="btn btn-secondary btn-sm" disabled={page >= totalPages} onClick={() => setPage((p) => p + 1)}>
            下一页
          </button>
        </div>
      </section>
    </div>
  );
}
