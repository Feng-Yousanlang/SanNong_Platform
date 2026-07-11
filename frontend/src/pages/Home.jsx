import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchNews } from '../api/news';
import { DEFAULT_IMG } from '../api/utils';
import { newsImageSrc } from '../constants/seedImages';
import { useAuth } from '../context/AuthContext';
import { HOME_FEATURES } from '../constants/nav';
import PageHeader from '../components/PageHeader';

const ROLE_TIPS = {
  '1': [
    '发布农产品前建议完善产地与库存信息，便于买家检索。',
    '申请融资时可先查看还款计划，合理规划种植投入。',
    '专家预约支持在线咨询，请提前描述问题便于专家准备。',
  ],
  '2': [
    '商城支持加入购物车后统一下单，适合批量采购。',
    '求购平台可发布需求，等待农户主动报价对接。',
    '物价预测基于历史数据，仅供参考，请结合市场判断。',
  ],
  '3': [
    '及时处理预约申请，保持日程空闲时段准确。',
    '知识库内容可分享给农户与买家，扩大服务影响。',
  ],
  '4': [
    '贷款审批请核对申请人资料与还款能力评估。',
    '金融产品参数变更后，将影响新申请而非历史记录。',
  ],
  '5': [
    '用户管理支持查询与权限调整，操作前请确认身份。',
    '知识库维护请保持内容准确，避免误导平台用户。',
  ],
};

function HomeFeatureSidebar({ features, onNavigate }) {
  return (
    <aside className="home-sidebar home-sidebar-left card">
      <div className="home-sidebar-head">
        <h2>功能入口</h2>
        <p>{features.length} 个可用模块</p>
      </div>
      <nav className="home-feature-nav" aria-label="功能入口">
        {features.map((f) => (
          <button
            key={f.path + f.title}
            type="button"
            className={`home-feature-link home-feature-link--${f.accent}`}
            onClick={() => onNavigate(f.path)}
          >
            <span className="home-feature-link-title">{f.title}</span>
            <span className="home-feature-link-desc">{f.desc}</span>
          </button>
        ))}
      </nav>
    </aside>
  );
}

function HomeRightSidebar({
  username,
  roleLabel,
  identity,
  news,
  showNews,
  activeIdx,
  onSelectNews,
  onNavigate,
}) {
  const tips = ROLE_TIPS[String(identity)] || ROLE_TIPS['1'];

  return (
    <aside className="home-sidebar home-sidebar-right">
      <section className="card home-side-card">
        <div className="home-sidebar-head">
          <h2>账户概览</h2>
        </div>
        <div className="home-side-stat">
          <span className="home-side-stat-label">用户名</span>
          <span className="home-side-stat-value">{username || '—'}</span>
        </div>
        <div className="home-side-stat">
          <span className="home-side-stat-label">当前角色</span>
          <span className="home-side-stat-value">{roleLabel}</span>
        </div>
        <button type="button" className="btn btn-secondary btn-sm home-side-btn" onClick={() => onNavigate('/profile')}>
          进入个人中心
        </button>
      </section>

      {showNews && news.length > 0 ? (
        <section className="card home-side-card">
          <div className="home-sidebar-head">
            <h2>资讯快报</h2>
            <p>点击切换轮播或查看原文</p>
          </div>
          <ul className="home-news-list">
            {news.map((item, i) => (
              <li key={item.id ?? item.title ?? i}>
                <button
                  type="button"
                  className={`home-news-list-item ${i === activeIdx ? 'active' : ''}`}
                  onClick={() => onSelectNews(i)}
                >
                  <span className="home-news-list-index">{String(i + 1).padStart(2, '0')}</span>
                  <span className="home-news-list-title">{item.title || item.name || '未命名资讯'}</span>
                </button>
              </li>
            ))}
          </ul>
        </section>
      ) : null}

      <section className="card home-side-card home-side-card-accent">
        <div className="home-sidebar-head">
          <h2>平台提示</h2>
        </div>
        <ul className="home-tip-list">
          {tips.map((tip) => (
            <li key={tip}>{tip}</li>
          ))}
        </ul>
      </section>
    </aside>
  );
}

export default function Home() {
  const { identity, roleLabel, username } = useAuth();
  const navigate = useNavigate();
  const [news, setNews] = useState([]);
  const [idx, setIdx] = useState(0);
  const [msg, setMsg] = useState('加载中...');

  const features = HOME_FEATURES[identity] || HOME_FEATURES['1'];
  const showNews = ['1', '2'].includes(String(identity));

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const list = await fetchNews();
        if (cancelled) return;
        setNews(Array.isArray(list) ? list : []);
        setMsg(list?.length ? '' : '暂无新闻');
      } catch (err) {
        if (!cancelled) setMsg(err.message || '新闻加载失败');
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (news.length <= 1) return undefined;
    const timer = setInterval(() => {
      setIdx((i) => (i + 1) % news.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [news.length]);

  const current = news[idx];

  return (
    <div className="page-content home-page">
      <div className="home-columns">
        <HomeFeatureSidebar features={features} onNavigate={navigate} />

        <div className="home-main">
          <PageHeader
            eyebrow={`当前身份 · ${roleLabel}`}
            title="欢迎回来"
            subtitle="三农服务平台 · 新闻资讯与功能入口"
          />

          <div className="home-banner">
            <div className="home-banner-block">
              <span className="home-banner-label">平台服务</span>
              <strong>一站式农业综合支持</strong>
            </div>
            <div className="home-banner-block">
              <span className="home-banner-label">覆盖角色</span>
              <strong>农户 · 买家 · 专家 · 银行 · 管理</strong>
            </div>
            <div className="home-banner-block">
              <span className="home-banner-label">可用模块</span>
              <strong>{features.length} 个功能入口</strong>
            </div>
          </div>

          {showNews ? (
            <section className="card card-news">
              <div className="card-section-head">
                <h2>新闻轮播</h2>
                <p>了解最新农业政策与市场动态</p>
              </div>
              {msg ? <p className="msg msg-info">{msg}</p> : null}
              {current ? (
                <>
                  <div className="news-carousel">
                    <a
                      className="news-slide"
                      href={current.newsUrl || current.url || '#'}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      <img
                        src={newsImageSrc(current)}
                        alt={current.title || current.name || '资讯'}
                        onError={(e) => {
                          e.currentTarget.src = DEFAULT_IMG;
                        }}
                      />
                      <div className="news-overlay">
                        <p className="news-overlay-tag">最新资讯</p>
                        <h3>{current.title || current.name || '查看详情'}</h3>
                      </div>
                    </a>
                  </div>
                  <div className="news-controls">
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => setIdx((i) => (i - 1 + news.length) % news.length)}
                    >
                      上一则
                    </button>
                    {news.map((_, i) => (
                      <button
                        key={i}
                        type="button"
                        className={`news-dot ${i === idx ? 'active' : ''}`}
                        aria-label={`第 ${i + 1} 则新闻`}
                        onClick={() => setIdx(i)}
                      />
                    ))}
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => setIdx((i) => (i + 1) % news.length)}
                    >
                      下一则
                    </button>
                  </div>
                </>
              ) : null}
            </section>
          ) : (
            <section className="card home-role-panel">
              <div className="card-section-head">
                <h2>工作台</h2>
                <p>请从左侧功能入口或右侧导航进入业务模块</p>
              </div>
              <div className="home-role-panel-body">
                <p>
                  当前以<strong>{roleLabel}</strong>身份登录，可使用 {features.length}{' '}
                  项平台功能。左侧栏提供快捷入口，右侧可查看账户信息与操作提示。
                </p>
              </div>
            </section>
          )}
        </div>

        <HomeRightSidebar
          username={username}
          roleLabel={roleLabel}
          identity={identity}
          news={news}
          showNews={showNews}
          activeIdx={idx}
          onSelectNews={setIdx}
          onNavigate={navigate}
        />
      </div>
    </div>
  );
}
