import { useCallback, useEffect, useState } from 'react';
import {
  askAiExpert,
  askExpert,
  cancelAppointment,
  createAppointment,
  fetchExpertDetail,
  fetchExpertSchedule,
  fetchExperts,
  fetchPendingAppointments,
  fetchUserAppointments,
  reviewAppointment,
} from '../api/experts';
import { formatDate } from '../api/utils';
import DetailLine from '../components/DetailLine';
import Modal from '../components/Modal';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';

const TIME_SLOTS = [
  { value: '09:00-10:00', label: '09:00 - 10:00' },
  { value: '10:00-11:00', label: '10:00 - 11:00' },
  { value: '14:00-15:00', label: '14:00 - 15:00' },
  { value: '15:00-16:00', label: '15:00 - 16:00' },
  { value: '16:00-17:00', label: '16:00 - 17:00' },
];

function asList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.records)) return data.records;
  if (Array.isArray(data?.list)) return data.list;
  if (Array.isArray(data?.experts)) return data.experts;
  return [];
}

function getExpertId(expert) {
  return expert?.expertId ?? expert?.id ?? null;
}

function getExpertName(expert) {
  return expert?.expertName ?? expert?.name ?? `专家 #${getExpertId(expert)}`;
}

export default function Appointment() {
  const { userId, identity } = useAuth();
  const isExpert = identity === '3';

  const [tab, setTab] = useState(isExpert ? 'pending' : 'experts');
  const [experts, setExperts] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [pending, setPending] = useState([]);
  const [schedule, setSchedule] = useState([]);
  const [msg, setMsg] = useState('');

  const [selectedExpert, setSelectedExpert] = useState(null);
  const [bookForm, setBookForm] = useState({ date: '', timeSlot: '', topic: '', remark: '' });
  const [question, setQuestion] = useState('');
  const [aiQuestion, setAiQuestion] = useState('');
  const [aiAnswer, setAiAnswer] = useState('');

  const loadExperts = useCallback(async () => {
    try {
      const data = await fetchExperts();
      setExperts(asList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, []);

  const loadUserSide = useCallback(async () => {
    if (!userId) return;
    try {
      const data = await fetchUserAppointments(userId);
      setAppointments(asList(data));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  const loadExpertSide = useCallback(async () => {
    if (!userId) return;
    try {
      const [p, s] = await Promise.all([
        fetchPendingAppointments(userId),
        fetchExpertSchedule({ userId, page: 1, size: 50 }),
      ]);
      setPending(asList(p));
      setSchedule(asList(s));
    } catch (err) {
      setMsg(err.message);
    }
  }, [userId]);

  useEffect(() => {
    loadExperts();
    if (isExpert) loadExpertSide();
    else loadUserSide();
  }, [isExpert, loadExperts, loadUserSide, loadExpertSide]);

  async function openExpertModal(expert) {
    setMsg('');
    setBookForm({ date: '', timeSlot: '', topic: '', remark: '' });
    try {
      const id = getExpertId(expert);
      if (id) {
        const detail = await fetchExpertDetail(id);
        setSelectedExpert({ ...expert, ...detail });
      } else {
        setSelectedExpert(expert);
      }
    } catch {
      setSelectedExpert(expert);
    }
  }

  function closeExpertModal() {
    setSelectedExpert(null);
  }

  async function handleBook(e) {
    e.preventDefault();
    if (!selectedExpert) return;
    const expertId = getExpertId(selectedExpert);
    const [startTime, endTime] = bookForm.timeSlot.split('-').map((s) => s.trim());
    setMsg('提交预约中...');
    try {
      const res = await createAppointment({
        userId: Number(userId),
        expertId: Number(expertId),
        date: bookForm.date,
        startTime,
        endTime,
        topic: bookForm.topic,
        remark: bookForm.remark,
        status: 'pending',
      });
      setMsg(res.message || '预约已提交，等待专家确认');
      closeExpertModal();
      loadUserSide();
      setTab('mine');
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleReview(item, approved) {
    setMsg('处理中...');
    try {
      const res = await reviewAppointment({
        appointmentId: item.appointmentId || item.expert_appointment_id || item.id,
        userId: Number(userId),
        action: approved ? 1 : 0,
      });
      setMsg(res.message || '操作成功');
      loadExpertSide();
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleAsk(e) {
    e.preventDefault();
    setMsg('提问中...');
    try {
      const res = await askExpert({ userId: Number(userId), question });
      setMsg(res.message || '问题已提交');
      setQuestion('');
    } catch (err) {
      setMsg(err.response?.data?.message || err.message);
    }
  }

  async function handleAiAsk(e) {
    e.preventDefault();
    setAiAnswer('思考中...');
    try {
      const res = await askAiExpert({ question: aiQuestion });
      setAiAnswer(res.data?.answer || res.message || JSON.stringify(res));
    } catch (err) {
      setAiAnswer(err.response?.data?.message || err.message);
    }
  }

  const tabs = isExpert
    ? [
        { id: 'pending', label: '待审核' },
        { id: 'schedule', label: '日程' },
      ]
    : [
        { id: 'experts', label: '专家列表' },
        { id: 'mine', label: '我的预约' },
        { id: 'ask', label: '在线提问' },
        { id: 'ai', label: 'AI 助手' },
      ];

  return (
    <div>
      <PageHeader title="专家预约" subtitle="点击专家卡片查看详情并提交预约" />
      {msg ? <p className="msg msg-info">{msg}</p> : null}

      <div className="tabs">
        {tabs.map((t) => (
          <button key={t.id} type="button" className={`tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'experts' && (
        <section className="card">
          <div className="grid-3">
            {experts.length ? (
              experts.map((ex) => (
                <div
                  key={getExpertId(ex)}
                  className="item-card clickable-card"
                  onClick={() => openExpertModal(ex)}
                  onKeyDown={(e) => e.key === 'Enter' && openExpertModal(ex)}
                  role="button"
                  tabIndex={0}
                >
                  <h4>{getExpertName(ex)}</h4>
                  <p>{ex.field || ex.specialty || '农业专家'}</p>
                  <p>{ex.expertDescription || ex.introduction || ex.description || '点击查看详情并预约'}</p>
                </div>
              ))
            ) : (
              <div className="empty">暂无专家</div>
            )}
          </div>
        </section>
      )}

      {tab === 'mine' && (
        <section className="card">
          <h3>我的预约</h3>
          {appointments.length ? (
            appointments.map((a) => (
              <div key={a.appointmentId || a.expert_appointment_id || a.id} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <h4>预约 #{a.appointmentId || a.expert_appointment_id || a.id}</h4>
                <p>专家：{a.expertName || a.expertId}</p>
                <p>
                  时间：{a.date || ''} {a.startTime || ''}
                  {a.endTime ? ` - ${a.endTime}` : ''}
                </p>
                <p>主题：{a.topic || '—'}</p>
                <p>状态：{a.status || a.statusName || '—'}</p>
                {(a.status === 'pending' || a.status === '待审核') && (
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={async () => {
                      await cancelAppointment({
                        appointmentId: a.appointmentId || a.expert_appointment_id || a.id,
                        userId: Number(userId),
                      });
                      loadUserSide();
                    }}
                  >
                    取消预约
                  </button>
                )}
              </div>
            ))
          ) : (
            <div className="empty">暂无预约，请先从专家列表选择专家</div>
          )}
        </section>
      )}

      {tab === 'ask' && (
        <section className="card">
          <h3>向专家提问</h3>
          <form onSubmit={handleAsk}>
            <div className="form-row">
              <label>问题内容</label>
              <textarea value={question} onChange={(e) => setQuestion(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary">
              提交问题
            </button>
          </form>
        </section>
      )}

      {tab === 'ai' && (
        <section className="card">
          <h3>AI 农业助手</h3>
          <form onSubmit={handleAiAsk}>
            <div className="form-row">
              <label>你的问题</label>
              <textarea value={aiQuestion} onChange={(e) => setAiQuestion(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary">
              提问
            </button>
          </form>
          {aiAnswer ? (
            <div className="item-card" style={{ marginTop: '1rem', whiteSpace: 'pre-wrap' }}>
              {aiAnswer}
            </div>
          ) : null}
        </section>
      )}

      {tab === 'pending' && isExpert && (
        <section className="card">
          <h3>待审核预约</h3>
          {pending.length ? (
            pending.map((a) => (
              <div key={a.appointmentId || a.expert_appointment_id || a.id} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <h4>预约 #{a.appointmentId || a.expert_appointment_id || a.id}</h4>
                <p>用户 ID：{a.userId}</p>
                <p>
                  时间：{a.date} {a.startTime}
                  {a.endTime ? ` - ${a.endTime}` : ''}
                </p>
                <p>主题：{a.topic || '—'}</p>
                <p>备注：{a.remark || '—'}</p>
                <div className="item-actions">
                  <button type="button" className="btn btn-primary btn-sm" onClick={() => handleReview(a, true)}>
                    通过
                  </button>
                  <button type="button" className="btn btn-danger btn-sm" onClick={() => handleReview(a, false)}>
                    拒绝
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="empty">暂无待审核预约</div>
          )}
        </section>
      )}

      {tab === 'schedule' && isExpert && (
        <section className="card">
          <h3>预约日程</h3>
          {schedule.length ? (
            schedule.map((a) => (
              <div key={a.appointmentId || a.expert_appointment_id || a.id} className="item-card" style={{ marginBottom: '0.75rem' }}>
                <p>
                  时间：{a.date} {a.startTime}
                  {a.endTime ? ` - ${a.endTime}` : ''}
                </p>
                <p>用户：{a.userId}</p>
                <p>主题：{a.topic || '—'}</p>
                <p>状态：{a.status || '—'}</p>
              </div>
            ))
          ) : (
            <div className="empty">暂无日程</div>
          )}
        </section>
      )}

      <Modal open={Boolean(selectedExpert)} title={selectedExpert ? getExpertName(selectedExpert) : '专家详情'} onClose={closeExpertModal} wide>
        {selectedExpert ? (
          <>
            <DetailLine label="专业领域" value={selectedExpert.field || selectedExpert.specialty} />
            <DetailLine label="专家介绍" value={selectedExpert.expertDescription || selectedExpert.introduction} />
            <DetailLine label="联系电话" value={selectedExpert.expertPhone || selectedExpert.phone} />
            <DetailLine label="邮箱" value={selectedExpert.expertEmail || selectedExpert.email} />
            <DetailLine label="案例" value={selectedExpert.example} />

            {!isExpert ? (
              <>
                <hr className="modal-divider" />
                <h4 style={{ margin: '0 0 0.75rem' }}>提交预约</h4>
                <form onSubmit={handleBook}>
                  <div className="form-row">
                    <label>预约日期</label>
                    <input
                      type="date"
                      value={bookForm.date}
                      min={new Date().toISOString().slice(0, 10)}
                      onChange={(e) => setBookForm({ ...bookForm, date: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-row">
                    <label>预约时段</label>
                    <select
                      value={bookForm.timeSlot}
                      onChange={(e) => setBookForm({ ...bookForm, timeSlot: e.target.value })}
                      required
                    >
                      <option value="">请选择时段</option>
                      {TIME_SLOTS.map((slot) => (
                        <option key={slot.value} value={slot.value}>
                          {slot.label}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="form-row">
                    <label>咨询主题</label>
                    <input value={bookForm.topic} onChange={(e) => setBookForm({ ...bookForm, topic: e.target.value })} required />
                  </div>
                  <div className="form-row">
                    <label>备注（可选）</label>
                    <textarea value={bookForm.remark} onChange={(e) => setBookForm({ ...bookForm, remark: e.target.value })} />
                  </div>
                  <button type="submit" className="btn btn-primary">
                    提交预约
                  </button>
                </form>
              </>
            ) : null}
          </>
        ) : null}
      </Modal>
    </div>
  );
}
