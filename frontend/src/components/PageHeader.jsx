export default function PageHeader({ title, subtitle, actions, eyebrow }) {
  return (
    <div className="page-hero">
      <div className="page-hero-pattern" aria-hidden="true" />
      <div className="page-header">
        <div className="page-header-text">
          {eyebrow ? <p className="page-eyebrow">{eyebrow}</p> : null}
          <h1>{title}</h1>
          {subtitle ? <p className="page-subtitle">{subtitle}</p> : null}
        </div>
        {actions ? <div className="page-actions">{actions}</div> : null}
      </div>
    </div>
  );
}
