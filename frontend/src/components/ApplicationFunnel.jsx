
const COLORS = {
  total:    { bar: '#534AB7', text: '#EEEDFE' },
  active:   { bar: '#185FA5', text: '#E6F1FB' },
  rejected: { bar: '#A32D2D', text: '#FCEBEB' },
  interview:{ bar: '#0F6E56', text: '#E1F5EE' },
  applied:  { bar: '#378ADD', text: '#E6F1FB' },
};

export default function ApplicationFunnel({ applications = [] }) {
  const total    = applications.length;
  const rejected = applications.filter(a => a.status?.toLowerCase() === 'rejected').length;
  const interview = applications.filter(a => a.status?.toLowerCase() === 'interview').length;
  const active   = total - rejected - interview;

  const pct = (n, of) => of === 0 ? 0 : Math.round((n / of) * 100);

  if (total === 0) return null;

  return (
    <div style={{ padding: '0 0 1rem' }}>
      <Row label="Total applications">
        <Segment width={100} color={COLORS.total} label={`${total} total`} />
      </Row>

      <Row label="By status">
        <Segment width={pct(active, total)}    color={COLORS.active}    label={`${active} applied`} />
        <Segment width={pct(rejected, total)}  color={COLORS.rejected}  label={`${rejected} didn't work out`} />
        <Segment width={pct(interview, total)} color={COLORS.interview} label={interview > 0 ? `${interview}` : ''} />
      </Row>

      <Legend items={[
        { color: COLORS.active.bar,    label: `Applied — ${active} (${pct(active, total)}%)` },
        { color: COLORS.rejected.bar,  label: `Rejected — ${rejected} (${pct(rejected, total)}%)` },
        { color: COLORS.interview.bar, label: `Interview — ${interview} (${pct(interview, total)}%)` },
      ]} />
    </div>
  );
}

function Row({ label, children }) {
  return (
    <div style={{ marginBottom: 4 }}>
      <div style={{ fontSize: 11, color: 'var(--color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>
        {label}
      </div>
      <div style={{ height: 36, borderRadius: 8, overflow: 'hidden', display: 'flex', gap: 3 }}>
        {children}
      </div>
    </div>
  );
}

function Segment({ width, color, label }) {
  if (width <= 0) return null;
  return (
    <div style={{
      width: `${width}%`,
      background: color.bar,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minWidth: 0,
      transition: 'width 0.4s ease',
    }}>
      <span style={{ fontSize: 13, fontWeight: 500, color: color.text, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', padding: '0 8px' }}>
        {label}
      </span>
    </div>
  );
}

function Legend({ items }) {
  return (
    <div style={{ display: 'flex', gap: 16, marginTop: 8, marginBottom: 16, flexWrap: 'wrap' }}>
      {items.map(({ color, label }) => (
        <div key={label} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: 'var(--color-text-secondary)' }}>
          <span style={{ width: 10, height: 10, borderRadius: 2, background: color, flexShrink: 0 }} />
          {label}
        </div>
      ))}
    </div>
  );
}