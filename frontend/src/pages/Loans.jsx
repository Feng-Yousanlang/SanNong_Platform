import { useCallback, useEffect, useState } from 'react';
import {
  applyLoan,
  approveLoan,
  createLoanProduct,
  deleteLoanProduct,
  fetchApplications,
  fetchBankApprovals,
  fetchLoanProducts,
  fetchPendingLoans,
  fetchRepaymentPlan,
  fetchRepayments,
  submitRepay,
} from '../api/loans';
import { formatDate } from '../api/utils';
import DetailLine from '../components/DetailLine';
import Modal from '../components/Modal';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import {
  formatAmountRange,
  formatLoanRate,
  formatLoanTerm,
  getLoanProductId,
  getLoanProductName,
  parseLoanTags,
  resolveApplicationId,
} from '../utils/loanHelpers';

function normalizeList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.records)) return data.records;
  if (Array.isArray(data?.list)) return data.list;
  return [];
}

export default function Loans() {
  const { userId, identity } = useAuth();
  const isFarmer = identity === '1';
  const isBank = identity === '4';

  const [tab, setTab] = useState(isBank ? 'pending' : 'products');
  const [products, setProducts] = useState([]);
  const [applications, setApplications] = useState([]);
  const [pending, setPending] = useState([]);
  const [approvals, setApprovals] = useState([]);
  const [repayPlan, setRepayPlan] = useState([]);
  const [repayments, setRepayments] = useState([]);
  const [msg, setMsg] = useState('');

  const [selectedProduct, setSelectedProduct] = useState(null);
  const [applyForm, setApplyForm] = useState({ amount: '', term: '' });
  const [applyFile, setApplyFile] = useState(null);
  const [showCreateProduct, setShowCreateProduct] = useState(false);
  const [repayTarget, setRepayTarget] = useState(null);
  const [repayAmount, setRepayAmount] = useState('');
  const [productForm, setProductForm] = useState({ productName: '', rate: '', maxAmount: '', minAmount: '', term: '' });

  const loadProducts = useCallback(async () => {
    try {
      const data = await fetchLoanProducts();
      setProducts(normalizeList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, []);

  const loadApplications = useCallback(async () => {
    if (!userId) return;
    try {
      const data = await fetchApplications(userId);
      setApplications(normalizeList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  const loadBankData = useCallback(async () => {
    try {
      const [p, a] = await Promise.all([fetchPendingLoans(), fetchBankApprovals(userId)]);
      setPending(normalizeList(p));
      setApprovals(normalizeList(a));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  const loadRepayData = useCallback(async () => {
    if (!userId) return;
    try {
      const [plan, reps] = await Promise.all([fetchRepaymentPlan(userId), fetchRepayments(userId)]);
      setRepayPlan(normalizeList(plan));
      setRepayments(normalizeList(reps));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  useEffect(() => {
    loadProducts();
    if (isFarmer) {
      loadApplications();
      loadRepayData();
    }
    if (isBank) loadBankData();
  }, [isFarmer, isBank, loadProducts, loadApplications, loadRepayData, loadBankData]);

  function openProductModal(product) {
    const name = getLoanProductName(product);
    setSelectedProduct(product);
    setApplyForm({
      amount: product.maxAmount ? String(product.maxAmount) : '',
      term: product.term ? String(product.term) : '',
    });
    setApplyFile(null);
    setMsg('');
  }

  function closeProductModal() {
    setSelectedProduct(null);
    setApplyFile(null);
  }

  async function handleApply(e) {
    e.preventDefault();
    if (!selectedProduct) return;
    const formData = new FormData();
    formData.append('userId', userId);
    formData.append('productName', getLoanProductName(selectedProduct));
    formData.append('amount', applyForm.amount);
    formData.append('term', applyForm.term);
    if (applyFile) formData.append('documents[]', applyFile);
    setMsg('提交中...');
    try {
      const res = await applyLoan(formData);
      setMsg(res.message || '申请已提交');
      closeProductModal();
      loadApplications();
      setTab('applications');
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleBankApprove(item, decision) {
    const applicationId = resolveApplicationId(item);
    if (!applicationId) {
      setMsg('申请 ID 无效');
      return;
    }
    setMsg('审批提交中...');
    try {
      const res = await approveLoan({
        userId: Number(userId),
        applicationId: Number(applicationId),
        decision: decision ? 1 : 0,
        remark: decision ? '审批通过' : '审批拒绝',
      });
      setMsg(res.message || '审批完成');
      loadBankData();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleRepay(e) {
    e.preventDefault();
    if (!repayTarget) return;
    setMsg('还款提交中...');
    try {
      const res = await submitRepay({
        userId: Number(userId),
        applicationId: Number(repayTarget.applicationId),
        amount: Number(repayAmount),
      });
      setMsg(res.message || '还款成功');
      setRepayTarget(null);
      setRepayAmount('');
      loadRepayData();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleCreateProduct(e) {
    e.preventDefault();
    try {
      const res = await createLoanProduct({
        name: productForm.productName,
        productName: productForm.productName,
        fpName: productForm.productName,
        rate: Number(productForm.rate),
        annualRate: Number(productForm.rate),
        maxAmount: Number(productForm.maxAmount),
        minAmount: productForm.minAmount ? Number(productForm.minAmount) : undefined,
        term: Number(productForm.term),
      });
      setMsg(res.message || '产品已创建');
      setShowCreateProduct(false);
      setProductForm({ productName: '', rate: '', maxAmount: '', minAmount: '', term: '' });
      loadProducts();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  const tabs = isBank
    ? [
        { id: 'pending', label: '待审批' },
        { id: 'approvals', label: '审批记录' },
        { id: 'products', label: '贷款产品' },
      ]
    : isFarmer
      ? [
          { id: 'products', label: '贷款产品' },
          { id: 'applications', label: '我的申请' },
          { id: 'repay', label: '还款' },
        ]
      : [{ id: 'products', label: '贷款产品' }];

  return (
    <div>
      <PageHeader
        title="融资服务"
        subtitle="点击产品查看详情并申请；审批与还款在对应列表中直接操作"
        actions={
          isBank ? (
            <button type="button" className="btn btn-primary btn-sm" onClick={() => setShowCreateProduct(true)}>
              新建产品
            </button>
          ) : null
        }
      />
      {msg ? <p className="msg msg-info">{msg}</p> : null}

      <div className="tabs">
        {tabs.map((t) => (
          <button key={t.id} type="button" className={`tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'products' && (
        <section className="card">
          <h3>贷款产品</h3>
          <p className="page-subtitle">点击卡片查看详情{isFarmer ? '并提交申请' : isBank ? '或删除产品' : ''}</p>
          <div className="grid-2">
            {products.length ? (
              products.map((p) => (
                <div
                  key={getLoanProductId(p) || getLoanProductName(p)}
                  className="item-card clickable-card"
                  onClick={() => openProductModal(p)}
                  onKeyDown={(e) => e.key === 'Enter' && openProductModal(p)}
                  role="button"
                  tabIndex={0}
                >
                  <h4>{getLoanProductName(p)}</h4>
                  <p>利率：{formatLoanRate(p)}</p>
                  <p>额度：{formatAmountRange(p)}</p>
                  <p>期限：{formatLoanTerm(p)}</p>
                  {parseLoanTags(p).length ? (
                    <div className="tag-list">
                      {parseLoanTags(p).map((tag) => (
                        <span key={tag} className="tag">
                          {tag}
                        </span>
                      ))}
                    </div>
                  ) : null}
                </div>
              ))
            ) : (
              <div className="empty">暂无贷款产品</div>
            )}
          </div>
        </section>
      )}

      {tab === 'applications' && isFarmer && (
        <section className="card">
          <h3>我的申请</h3>
          {applications.length ? (
            applications.map((a) => (
              <div key={resolveApplicationId(a)} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <h4>{a.productName || a.fpName || `申请 #${resolveApplicationId(a)}`}</h4>
                <p>金额：{a.amount ?? '—'} 元 · 期限：{a.term ?? '—'} 月</p>
                <p>状态：{a.status ?? a.statusName ?? '—'}</p>
                <p>时间：{formatDate(a.applyTime || a.createTime)}</p>
              </div>
            ))
          ) : (
            <div className="empty">暂无申请，请先从「贷款产品」选择产品申请</div>
          )}
        </section>
      )}

      {tab === 'repay' && isFarmer && (
        <>
          <section className="card">
            <h3>还款计划</h3>
            <p className="page-subtitle">点击某条计划可直接发起还款</p>
            {repayPlan.length ? (
              repayPlan.map((row, i) => (
                <div
                  key={row.id || `${row.applicationId}-${row.period}-${i}`}
                  className="item-card clickable-card"
                  style={{ marginBottom: '0.75rem' }}
                  onClick={() => {
                    setRepayTarget(row);
                    setRepayAmount(String(row.amount ?? row.payAmount ?? ''));
                  }}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      setRepayTarget(row);
                      setRepayAmount(String(row.amount ?? row.payAmount ?? ''));
                    }
                  }}
                >
                  <p>
                    申请 #{row.applicationId} · 第 {row.period ?? '—'} 期
                  </p>
                  <p>
                    应还：{row.amount ?? '—'} 元 · 到期：{formatDate(row.dueDate)}
                  </p>
                  <p>状态：{row.status ?? '—'}</p>
                </div>
              ))
            ) : (
              <div className="empty">暂无还款计划</div>
            )}
          </section>
          <section className="card">
            <h3>还款记录</h3>
            {repayments.length ? (
              repayments.map((r, i) => (
                <div key={r.id || i} className="item-card" style={{ marginBottom: '0.75rem' }}>
                  <p>
                    申请 #{r.applicationId} · 金额 {r.amount ?? r.payAmount} 元
                  </p>
                  <p>时间：{formatDate(r.repayTime || r.payDate || r.date)}</p>
                </div>
              ))
            ) : (
              <div className="empty">暂无还款记录</div>
            )}
          </section>
        </>
      )}

      {tab === 'pending' && isBank && (
        <section className="card">
          <h3>待审批申请</h3>
          {pending.length ? (
            pending.map((item) => {
              const appId = resolveApplicationId(item);
              return (
                <div key={appId} className="item-card" style={{ marginBottom: '0.75rem' }}>
                  <h4>申请 #{appId ?? '—'}</h4>
                  <p>用户：{item.userName || item.userId || '—'}</p>
                  <p>产品：{item.productName || item.fpName || '—'}</p>
                  <p>
                    金额：{item.amount ?? '—'} 元 · 期限：{item.term ?? '—'} 月
                  </p>
                  <p>申请时间：{formatDate(item.applyTime || item.createTime)}</p>
                  <div className="item-actions">
                    <button type="button" className="btn btn-primary btn-sm" onClick={() => handleBankApprove(item, true)}>
                      同意
                    </button>
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => handleBankApprove(item, false)}>
                      拒绝
                    </button>
                  </div>
                </div>
              );
            })
          ) : (
            <div className="empty">暂无待审批申请</div>
          )}
        </section>
      )}

      {tab === 'approvals' && isBank && (
        <section className="card">
          <h3>审批记录</h3>
          {approvals.length ? (
            approvals.map((r, i) => (
              <div key={r.recordId || i} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <p>结果：{r.decision ?? '—'}</p>
                <p>备注：{r.remark ?? r.opinion ?? '—'}</p>
                <p>时间：{formatDate(r.date || r.approveTime)}</p>
              </div>
            ))
          ) : (
            <div className="empty">暂无审批记录</div>
          )}
        </section>
      )}

      <Modal
        open={Boolean(selectedProduct)}
        title={selectedProduct ? getLoanProductName(selectedProduct) : '产品详情'}
        onClose={closeProductModal}
        wide={isFarmer}
      >
        {selectedProduct ? (
          <>
            <DetailLine label="产品名称" value={getLoanProductName(selectedProduct)} />
            <DetailLine label="额度范围" value={formatAmountRange(selectedProduct)} />
            <DetailLine label="年化利率" value={formatLoanRate(selectedProduct)} />
            <DetailLine label="贷款期限" value={formatLoanTerm(selectedProduct)} />
            <DetailLine label="产品简介" value={selectedProduct.fpDescription || selectedProduct.description} />
            <DetailLine label="负责人" value={selectedProduct.fpManagerName} />
            <DetailLine label="联系电话" value={selectedProduct.fpManagerPhone} />

            {isFarmer ? (
              <>
                <hr className="modal-divider" />
                <h4 style={{ margin: '0 0 0.75rem' }}>提交贷款申请</h4>
                <form onSubmit={handleApply}>
                  <div className="form-row">
                    <label>申请金额（元）</label>
                    <input
                      type="number"
                      value={applyForm.amount}
                      onChange={(e) => setApplyForm({ ...applyForm, amount: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-row">
                    <label>贷款期限（月）</label>
                    <input
                      type="number"
                      value={applyForm.term}
                      onChange={(e) => setApplyForm({ ...applyForm, term: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-row">
                    <label>资质材料（可选）</label>
                    <input type="file" onChange={(e) => setApplyFile(e.target.files?.[0] || null)} />
                  </div>
                  <button type="submit" className="btn btn-primary">
                    提交申请
                  </button>
                </form>
              </>
            ) : null}

            {isBank ? (
              <div className="item-actions" style={{ marginTop: '1rem' }}>
                <button
                  type="button"
                  className="btn btn-danger btn-sm"
                  onClick={async () => {
                    if (!confirm('确定删除该产品？')) return;
                    await deleteLoanProduct(getLoanProductId(selectedProduct));
                    closeProductModal();
                    loadProducts();
                  }}
                >
                  删除产品
                </button>
              </div>
            ) : null}
          </>
        ) : null}
      </Modal>

      <Modal open={Boolean(repayTarget)} title="提交还款" onClose={() => setRepayTarget(null)}>
        {repayTarget ? (
          <form onSubmit={handleRepay}>
            <DetailLine label="申请 ID" value={repayTarget.applicationId} />
            <DetailLine label="期数" value={repayTarget.period} />
            <DetailLine label="应还金额" value={repayTarget.amount} />
            <div className="form-row">
              <label>本次还款金额</label>
              <input type="number" step="0.01" value={repayAmount} onChange={(e) => setRepayAmount(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary">
              确认还款
            </button>
          </form>
        ) : null}
      </Modal>

      <Modal open={showCreateProduct} title="新建贷款产品" onClose={() => setShowCreateProduct(false)} wide>
        <form onSubmit={handleCreateProduct}>
          <div className="grid-2">
            <div className="form-row">
              <label>产品名称</label>
              <input value={productForm.productName} onChange={(e) => setProductForm({ ...productForm, productName: e.target.value })} required />
            </div>
            <div className="form-row">
              <label>利率 (%)</label>
              <input type="number" step="0.01" value={productForm.rate} onChange={(e) => setProductForm({ ...productForm, rate: e.target.value })} required />
            </div>
            <div className="form-row">
              <label>最低额度</label>
              <input type="number" value={productForm.minAmount} onChange={(e) => setProductForm({ ...productForm, minAmount: e.target.value })} />
            </div>
            <div className="form-row">
              <label>最高额度</label>
              <input type="number" value={productForm.maxAmount} onChange={(e) => setProductForm({ ...productForm, maxAmount: e.target.value })} required />
            </div>
            <div className="form-row">
              <label>期限（月）</label>
              <input type="number" value={productForm.term} onChange={(e) => setProductForm({ ...productForm, term: e.target.value })} required />
            </div>
          </div>
          <button type="submit" className="btn btn-primary">
            创建产品
          </button>
        </form>
      </Modal>
    </div>
  );
}
