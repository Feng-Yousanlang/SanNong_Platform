import { useCallback, useEffect, useState } from 'react';
import {
  createFarmerProduct,
  deleteFarmerProduct,
  fetchFarmerPendingOrders,
  fetchFarmerProducts,
  fetchSoldOutProducts,
  sendProduct,
} from '../api/products';
import { DEFAULT_IMG, resolveImage } from '../api/utils';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';

function asList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.records)) return data.records;
  return [];
}

export default function Products() {
  const { userId } = useAuth();
  const [tab, setTab] = useState('mine');
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [sold, setSold] = useState([]);
  const [msg, setMsg] = useState('');
  const [form, setForm] = useState({ productName: '', price: '', totalVolumn: '', producer: '' });
  const [imgFile, setImgFile] = useState(null);

  const loadProducts = useCallback(async () => {
    if (!userId) return;
    try {
      const data = await fetchFarmerProducts(userId);
      setProducts(asList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  const loadOrders = useCallback(async () => {
    if (!userId) return;
    try {
      const [pending, soldOut] = await Promise.all([fetchFarmerPendingOrders(userId), fetchSoldOutProducts(userId)]);
      setOrders(asList(pending));
      setSold(asList(soldOut));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  useEffect(() => {
    loadProducts();
    loadOrders();
  }, [loadProducts, loadOrders]);

  async function handleCreate(e) {
    e.preventDefault();
    const formData = new FormData();
    formData.append('productName', form.productName);
    formData.append('price', form.price);
    formData.append('userId', userId);
    formData.append('totalVolumn', form.totalVolumn);
    if (form.producer) formData.append('producer', form.producer);
    if (imgFile) formData.append('file', imgFile);
    setMsg('发布中...');
    try {
      const res = await createFarmerProduct(formData);
      setMsg(res.message || '发布成功');
      setForm({ productName: '', price: '', totalVolumn: '', producer: '' });
      setImgFile(null);
      loadProducts();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  return (
    <div>
      <PageHeader title="农产品管理" subtitle="发布商品、处理订单与查看销售记录" />
      {msg ? <p className="msg msg-info">{msg}</p> : null}

      <div className="tabs">
        {[
          { id: 'mine', label: '我的商品' },
          { id: 'publish', label: '发布商品' },
          { id: 'orders', label: '待发货' },
          { id: 'sold', label: '已售出' },
        ].map((t) => (
          <button key={t.id} type="button" className={`tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'publish' && (
        <section className="card">
          <form onSubmit={handleCreate}>
            <div className="grid-2">
              <div className="form-row">
                <label>商品名称</label>
                <input value={form.productName} onChange={(e) => setForm({ ...form, productName: e.target.value })} required />
              </div>
              <div className="form-row">
                <label>单价 (元/kg)</label>
                <input type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} required />
              </div>
              <div className="form-row">
                <label>库存 (kg)</label>
                <input type="number" value={form.totalVolumn} onChange={(e) => setForm({ ...form, totalVolumn: e.target.value })} required />
              </div>
              <div className="form-row">
                <label>产地</label>
                <input value={form.producer} onChange={(e) => setForm({ ...form, producer: e.target.value })} />
              </div>
            </div>
            <div className="form-row">
              <label>商品图片</label>
              <input type="file" accept="image/*" onChange={(e) => setImgFile(e.target.files?.[0] || null)} />
            </div>
            <button type="submit" className="btn btn-primary">
              发布商品
            </button>
          </form>
        </section>
      )}

      {tab === 'mine' && (
        <section className="card">
          <div className="grid-3">
            {products.length ? (
              products.map((p) => (
                <div key={p.productId || p.id} className="item-card">
                  <img
                    src={resolveImage(p.productImg || p.imageUrl)}
                    alt={p.productName}
                    onError={(e) => {
                      e.currentTarget.src = DEFAULT_IMG;
                    }}
                  />
                  <h4>{p.productName}</h4>
                  <p>¥ {p.price} · 库存 {p.totalVolumn ?? p.stock}</p>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={async () => {
                      if (!confirm('确定下架该商品？')) return;
                      await deleteFarmerProduct({ userId: Number(userId), productId: p.productId || p.id });
                      loadProducts();
                    }}
                  >
                    下架
                  </button>
                </div>
              ))
            ) : (
              <div className="empty">暂无商品</div>
            )}
          </div>
        </section>
      )}

      {tab === 'orders' && (
        <section className="card">
          {orders.length ? (
            orders.map((o) => (
              <div key={o.purchaseId || o.id} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <h4>{o.productName}</h4>
                <p>数量：{o.quantity} · 买家 ID：{o.buyerId || o.userId}</p>
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={async () => {
                    await sendProduct({ userId: Number(userId), purchaseId: o.purchaseId || o.id });
                    loadOrders();
                  }}
                >
                  确认发货
                </button>
              </div>
            ))
          ) : (
            <div className="empty">暂无待发货订单</div>
          )}
        </section>
      )}

      {tab === 'sold' && (
        <section className="card">
          {sold.length ? (
            sold.map((o) => (
              <div key={o.purchaseId || o.id} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <h4>{o.productName}</h4>
                <p>数量：{o.quantity} · 状态：{o.status || '已完成'}</p>
              </div>
            ))
          ) : (
            <div className="empty">暂无销售记录</div>
          )}
        </section>
      )}
    </div>
  );
}
