import { useCallback, useEffect, useState } from 'react';
import {
  addToCart,
  cancelPurchase,
  checkoutCartItem,
  fetchBuyerProductDetail,
  fetchBuyerProducts,
  fetchCart,
  fetchPurchaseHistory,
  fetchSavedAddress,
  purchaseProduct,
  receiveProduct,
  removeFromCart,
} from '../api/products';
import { DEFAULT_IMG, resolveImage } from '../api/utils';
import DetailLine from '../components/DetailLine';
import Modal from '../components/Modal';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';

function asList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.records)) return data.records;
  if (Array.isArray(data?.list)) return data.list;
  return [];
}

function parseAmount(value) {
  const amount = parseInt(String(value), 10);
  if (!Number.isFinite(amount) || amount <= 0) return null;
  return amount;
}

function openAlipayPage(html) {
  if (!html) return;
  const payWindow = window.open('', '_blank');
  if (payWindow) {
    payWindow.document.open();
    payWindow.document.write(html);
    payWindow.document.close();
  }
}

export default function Buy() {
  const { userId } = useAuth();
  const [tab, setTab] = useState('catalog');
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [orders, setOrders] = useState([]);
  const [selected, setSelected] = useState(null);
  const [quantity, setQuantity] = useState('1');
  const [msg, setMsg] = useState('');

  const [purchaseOpen, setPurchaseOpen] = useState(false);
  const [purchaseTarget, setPurchaseTarget] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [address, setAddress] = useState('');

  const loadCatalog = useCallback(async () => {
    try {
      const data = await fetchBuyerProducts(24);
      setProducts(asList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, []);

  const loadCart = useCallback(async () => {
    if (!userId) return;
    try {
      const data = await fetchCart(userId);
      setCart(asList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  const loadOrders = useCallback(async () => {
    if (!userId) return;
    try {
      const data = await fetchPurchaseHistory(userId);
      setOrders(asList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  const loadAddresses = useCallback(async () => {
    if (!userId) return [];
    try {
      const data = await fetchSavedAddress(userId);
      const list = asList(data);
      setAddresses(list);
      if (list.length) {
        const first = list[0].address_name || list[0].addressName || '';
        setAddress(first);
      }
      return list;
    } catch {
      setAddresses([]);
      return [];
    }
  }, [userId]);

  useEffect(() => {
    loadCatalog();
    loadCart();
    loadOrders();
  }, [loadCatalog, loadCart, loadOrders]);

  async function openProductModal(productId) {
    setMsg('');
    setQuantity('1');
    try {
      const detail = await fetchBuyerProductDetail(productId);
      setSelected(detail);
    } catch (err) {
      setMsg(err.message);
    }
  }

  function closeProductModal() {
    setSelected(null);
  }

  async function handleAddToCart() {
    if (!selected) return;
    const amount = parseAmount(quantity);
    if (!amount) {
      setMsg('请输入有效的购买数量');
      return;
    }
    setMsg('加入购物车...');
    try {
      const res = await addToCart({
        userId: Number(userId),
        productId: Number(selected.productId || selected.id),
        amount,
      });
      setMsg(res.message || '已加入购物车');
      loadCart();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function openPurchaseModal() {
    if (!selected) return;
    const amount = parseAmount(quantity);
    if (!amount) {
      setMsg('请输入有效的购买数量');
      return;
    }
    setPurchaseTarget({
      productId: selected.productId || selected.id,
      productName: selected.productName,
      amount,
    });
    setMsg('');
    await loadAddresses();
    setPurchaseOpen(true);
  }

  function closePurchaseModal() {
    setPurchaseOpen(false);
    setPurchaseTarget(null);
    setAddress('');
  }

  async function handleDirectPurchase(e) {
    e.preventDefault();
    if (!purchaseTarget) return;
    const getAddress = address.trim();
    if (!getAddress) {
      setMsg('请填写或选择收货地址');
      return;
    }
    setMsg('创建订单中...');
    try {
      const res = await purchaseProduct({
        userId: Number(userId),
        productId: Number(purchaseTarget.productId),
        amount: purchaseTarget.amount,
        getAddress,
      });
      if (res.data?.alipay) {
        openAlipayPage(res.data.alipay);
        setMsg(res.message || '订单已创建，请在新窗口完成支付');
      } else {
        setMsg(res.message || '购买成功');
      }
      closePurchaseModal();
      closeProductModal();
      loadOrders();
      setTab('orders');
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleCheckout() {
    if (!cart.length) return;
    setMsg('结算中...');
    try {
      for (const item of cart) {
        await checkoutCartItem({ cartId: item.cartId || item.id });
      }
      setMsg('下单成功');
      loadCart();
      loadOrders();
      setTab('orders');
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  return (
    <div>
      <PageHeader title="农产品商城" subtitle="点击商品查看详情，可加入购物车或直接购买" />
      {msg ? <p className="msg msg-info">{msg}</p> : null}

      <div className="tabs">
        {[
          { id: 'catalog', label: '商品列表' },
          { id: 'cart', label: `购物车 (${cart.length})` },
          { id: 'orders', label: '购买记录' },
        ].map((t) => (
          <button key={t.id} type="button" className={`tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'catalog' && (
        <section className="card">
          <div className="grid-3">
            {products.length ? (
              products.map((p) => (
                <div
                  key={p.productId || p.id}
                  className="item-card clickable-card"
                  onClick={() => openProductModal(p.productId || p.id)}
                  onKeyDown={(e) => e.key === 'Enter' && openProductModal(p.productId || p.id)}
                  role="button"
                  tabIndex={0}
                >
                  <img
                    src={resolveImage(p.productImg || p.imageUrl)}
                    alt={p.productName}
                    onError={(e) => {
                      e.currentTarget.src = DEFAULT_IMG;
                    }}
                  />
                  <h4>{p.productName || p.name}</h4>
                  <p>¥ {p.price ?? '—'} / kg</p>
                  <p>库存：{p.surplus ?? p.totalVolumn ?? p.stock ?? '—'}</p>
                </div>
              ))
            ) : (
              <div className="empty">暂无商品，请让农户先发布</div>
            )}
          </div>
        </section>
      )}

      {tab === 'cart' && (
        <section className="card">
          <h3>购物车</h3>
          {cart.length ? (
            <>
              {cart.map((item) => (
                <div key={item.cartId || item.id || item.productId} className="item-card" style={{ marginBottom: '0.75rem' }}>
                  <h4>{item.productName || item.name}</h4>
                  <p>
                    数量：{item.amount ?? item.quantity ?? '—'} · 单价：¥ {item.price}
                  </p>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={async () => {
                      await removeFromCart({ cartId: item.cartId || item.id });
                      loadCart();
                    }}
                  >
                    移除
                  </button>
                </div>
              ))}
              <button type="button" className="btn btn-primary" onClick={handleCheckout}>
                结算下单
              </button>
            </>
          ) : (
            <div className="empty">购物车为空，去商品列表挑选吧</div>
          )}
        </section>
      )}

      {tab === 'orders' && (
        <section className="card">
          <h3>购买记录</h3>
          {orders.length ? (
            orders.map((o) => (
              <div key={o.purchaseId || o.id} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <h4>{o.productName || `订单 #${o.purchaseId}`}</h4>
                <p>数量：{o.amount ?? o.quantity} · 金额：¥ {o.totalPrice ?? o.amount}</p>
                <p>状态：{o.status || o.statusName || '—'}</p>
                <div className="item-actions">
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={async () => {
                      await receiveProduct({ userId: Number(userId), purchaseId: o.purchaseId || o.id });
                      loadOrders();
                    }}
                  >
                    确认收货
                  </button>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={async () => {
                      await cancelPurchase({ userId: Number(userId), purchaseId: o.purchaseId || o.id });
                      loadOrders();
                    }}
                  >
                    取消订单
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="empty">暂无购买记录</div>
          )}
        </section>
      )}

      <Modal open={Boolean(selected)} title={selected?.productName || '商品详情'} onClose={closeProductModal} wide>
        {selected ? (
          <div className="section-split">
            <img
              src={resolveImage(selected.productImg || selected.imageUrl)}
              alt={selected.productName}
              style={{ width: '100%', borderRadius: 12, maxHeight: 280, objectFit: 'cover' }}
              onError={(e) => {
                e.currentTarget.src = DEFAULT_IMG;
              }}
            />
            <div>
              <DetailLine label="名称" value={selected.productName} />
              <DetailLine label="价格" value={`¥ ${selected.price} / kg`} />
              <DetailLine label="产地" value={selected.producer} />
              <DetailLine label="库存" value={selected.surplus ?? selected.totalVolumn ?? selected.stock} />
              <div className="form-row">
                <label>购买数量</label>
                <input type="number" min="1" value={quantity} onChange={(e) => setQuantity(e.target.value)} />
              </div>
              <div className="item-actions">
                <button type="button" className="btn btn-secondary" onClick={handleAddToCart}>
                  加入购物车
                </button>
                <button type="button" className="btn btn-primary" onClick={openPurchaseModal}>
                  立即购买
                </button>
              </div>
            </div>
          </div>
        ) : null}
      </Modal>

      <Modal open={purchaseOpen} title="直接购买" onClose={closePurchaseModal}>
        {purchaseTarget ? (
          <form onSubmit={handleDirectPurchase}>
            <DetailLine label="商品" value={purchaseTarget.productName} />
            <DetailLine label="数量" value={`${purchaseTarget.amount} kg`} />
            <DetailLine
              label="预估金额"
              value={`¥ ${((selected?.price || 0) * purchaseTarget.amount).toFixed(2)}`}
            />
            {addresses.length ? (
              <div className="form-row">
                <label>选择收货地址</label>
                <select
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                >
                  {addresses.map((addr) => {
                    const text = addr.address_name || addr.addressName || '';
                    return (
                      <option key={addr.addressId || text} value={text}>
                        {text}
                      </option>
                    );
                  })}
                </select>
              </div>
            ) : null}
            <div className="form-row">
              <label>收货地址</label>
              <input
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="请输入详细收货地址"
                required
              />
            </div>
            <div className="item-actions">
              <button type="button" className="btn btn-secondary" onClick={closePurchaseModal}>
                取消
              </button>
              <button type="submit" className="btn btn-primary">
                确认购买
              </button>
            </div>
          </form>
        ) : null}
      </Modal>
    </div>
  );
}
