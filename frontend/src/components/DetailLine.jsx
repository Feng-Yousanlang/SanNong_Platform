export default function DetailLine({ label, value }) {
  return (
    <div className="detail-line">
      <span className="detail-label">{label}</span>
      <span className="detail-value">{value ?? '—'}</span>
    </div>
  );
}
