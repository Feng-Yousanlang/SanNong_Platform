import { useEffect, useState } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import { fetchPriceForecast } from '../api/products';
import PageHeader from '../components/PageHeader';
import { PRICE_PRODUCTS } from '../constants/nav';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

export default function Price() {
  const [productName, setProductName] = useState(PRICE_PRODUCTS[0]);
  const [chartData, setChartData] = useState(null);
  const [msg, setMsg] = useState('');

  async function loadForecast(name = productName) {
    setMsg('加载中...');
    try {
      const data = await fetchPriceForecast(name);
      const list = Array.isArray(data) ? data : [];
      if (!list.length) {
        setChartData(buildMockChart(name));
        setMsg('暂无后端预测数据，已展示演示曲线');
        return;
      }
      setChartData(buildChart(name, list));
      setMsg('');
    } catch {
      setChartData(buildMockChart(name));
      setMsg('预测接口未配置，已展示演示曲线');
    }
  }

  function buildChart(name, list) {
    return {
      labels: list.map((d) => d.date),
      datasets: [
        {
          label: `${name} 价格预测 (元/kg)`,
          data: list.map((d) => d.price),
          borderColor: '#2e7d32',
          backgroundColor: 'rgba(46, 125, 50, 0.1)',
          tension: 0.3,
        },
      ],
    };
  }

  function buildMockChart(name) {
    const base = (name.charCodeAt(0) % 5) + 2.5;
    const list = Array.from({ length: 14 }, (_, i) => ({
      date: new Date(Date.now() + i * 86400000).toISOString().slice(0, 10),
      price: +(base + Math.sin(i / 2) * 0.6 + i * 0.05).toFixed(2),
    }));
    return buildChart(name, list);
  }

  useEffect(() => {
    loadForecast(productName);
  }, []);

  return (
    <div>
      <PageHeader title="物价预测" subtitle="基于历史数据的农产品价格走势预测" />

      <section className="card">
        <div className="grid-2">
          <div className="form-row">
            <label>选择农产品</label>
            <select value={productName} onChange={(e) => setProductName(e.target.value)}>
              {PRICE_PRODUCTS.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </div>
          <div className="form-row">
            <label>&nbsp;</label>
            <button type="button" className="btn btn-primary" onClick={() => loadForecast(productName)}>
              预测
            </button>
          </div>
        </div>
        {msg ? <p className="msg msg-info">{msg}</p> : null}
        {chartData ? (
          <div className="chart-box">
            <Line
              data={chartData}
              options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'top' } },
                scales: {
                  y: {
                    beginAtZero: true,
                    ticks: { callback: (v) => `${v} 元/kg` },
                  },
                },
              }}
            />
          </div>
        ) : null}
      </section>
    </div>
  );
}
