// queue-shared.jsx — shared building blocks for the three Queue Editor layouts
// Mock selection state (matches PRD example: (Rock OR Jazz) AND Hype AND NOT Slow)
const QSEL = {
  genre: { Rock: 'on', Jazz: 'on' },
  mood: { Hype: 'on' },
  pace: { Slow: 'off' }
};
const MATCH_COUNT = 58;

function stateOf(dim, label) {
  return QSEL[dim] && QSEL[dim][label] || 'idle';
}

// Count pill inside a chip
function CountTag({ n, on, scheme }) {
  const solid = on === 'on' && scheme === 'solid';
  return (
    <span style={{
      fontFamily: 'var(--mono-font)', fontSize: 11.5, fontWeight: 600,
      opacity: solid ? 0.7 : 0.55, marginLeft: 1
    }}>{n}</span>);

}

function FacetChip({ dim, label, count, scheme, dark, showCount = true, excludeDash = true }) {
  const st = stateOf(dim, label);
  const style = chipStyle(dim, st, scheme, dark);
  return (
    <div style={style}>
      {excludeDash && st === 'off' && <span style={{ fontFamily: 'var(--mono-font)', fontWeight: 700, marginRight: -2 }}>−</span>}
      {scheme === 'outline' && st === 'on' &&
      <span style={{ width: 7, height: 7, borderRadius: '50%', background: dimDot(dim, dark), flexShrink: 0 }} />
      }
      <span>{label}</span>
      {showCount && count != null && <CountTag n={count} on={st} scheme={scheme} />}
    </div>);

}

// Active filters row — included + excluded chips with remove affordance
function ActiveFilters({ scheme, dark, wrap = true, showDim = true, excludeDash = true }) {
  const items = [];
  Object.keys(QSEL).forEach((dim) => {
    Object.keys(QSEL[dim]).forEach((label) => items.push([dim, label, QSEL[dim][label]]));
  });
  return (
    <div style={{
      display: 'flex', gap: 8, flexWrap: wrap ? 'wrap' : 'nowrap',
      overflowX: wrap ? 'visible' : 'auto', alignItems: 'center'
    }}>
      {items.map(([dim, label, st]) => {
        const s = chipStyle(dim, st, scheme === 'solid' ? 'solid' : 'tinted', dark);
        return (
          <div key={dim + label} style={{ ...s, paddingRight: 8 }}>
            {excludeDash && st === 'off' && <span style={{ fontFamily: 'var(--mono-font)', fontWeight: 700 }}>−</span>}
            {showDim && <span style={{ fontSize: 12, opacity: 0.7, fontWeight: 600 }}>{DIMS[dim].label}:</span>}
            <span>{label}</span>
            <span style={{ opacity: 0.6, marginLeft: 1, display: 'flex' }}><Icon name="close" size={13} sw={2.6} /></span>
          </div>);

      })}
    </div>);

}

function PresetRow() {
  const presets = ['morning run', 'gym', 'late night'];
  const pill = (label, active, key) =>
  <div key={key} style={{
    padding: '7px 13px', borderRadius: 999, fontSize: 13, fontWeight: 600,
    whiteSpace: 'nowrap', flexShrink: 0,
    background: active ? 'var(--text)' : 'var(--surface-3)',
    color: active ? 'var(--surface)' : 'var(--text-2)',
    border: '1px solid', borderColor: active ? 'var(--text)' : 'var(--border)'
  }}>{label}</div>;

  return (
    <div style={{ display: 'flex', gap: 8, overflowX: 'auto', paddingBottom: 2 }} data-comment-anchor="80a7ff89dc-div-78-5">
      {presets.map((p, i) => pill(p, i === 1, p))}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 5, padding: '7px 12px', borderRadius: 999,
        fontSize: 13, fontWeight: 600, color: 'var(--text-2)', whiteSpace: 'nowrap', flexShrink: 0,
        border: '1px dashed var(--border)'
      }}><Icon name="plus" size={14} sw={2.4} />New</div>
    </div>);

}

function SearchInput({ placeholder = 'Filter tags…' }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 9, padding: '9px 13px',
      borderRadius: 12, background: 'var(--surface-3)', border: '1px solid var(--border)',
      color: 'var(--text-3)'
    }}>
      <Icon name="search" size={17} sw={2.2} />
      <span style={{ fontSize: 13.5, color: 'var(--text-3)' }}>{placeholder}</span>
    </div>);

}

function SectionLabel({ dim, right, rule = true }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 9, marginBottom: 11 }}>
      <span style={{ width: 8, height: 8, borderRadius: '50%', background: dimDot(dim, document.documentElement.dataset.theme === 'dark') }} />
      <span style={{
        fontSize: 11.5, fontWeight: 700, letterSpacing: '0.07em', textTransform: 'uppercase',
        color: 'var(--text-2)'
      }}>{DIMS[dim].label}</span>
      {rule && <span style={{ flex: 1, height: 1, background: 'var(--border)' }} />}
      {right}
    </div>);

}

// Bottom CTA bar — identical across all three layouts (isolates the layout variable)
function CTABar({ count = MATCH_COUNT, showCount = true, countInButton = false, previewNode = null, previewPos = null, queueLabel = 'Add to Queue' }) {
  const ghost = (icon, label) =>
  <div style={{
    flex: 1, height: 46, borderRadius: 13, display: 'flex', alignItems: 'center',
    justifyContent: 'center', gap: 7, background: 'var(--surface)',
    border: '1.5px solid var(--border)', color: 'var(--text)',
    fontSize: 13.5, fontWeight: 600
  }}>
      <Icon name={icon} size={18} sw={2} />{label}
    </div>;

  return (
    <div style={{
      flexShrink: 0, padding: '13px 16px 18px', background: 'var(--surface-2)',
      borderTop: '1px solid var(--border)'
    }}>
      {showCount && !countInButton && <div style={{
        display: 'flex', alignItems: 'baseline', justifyContent: 'center', gap: 7, marginBottom: 11
      }}>
        <span style={{ fontFamily: 'var(--mono-font)', fontSize: 17, fontWeight: 700, color: 'var(--accent)' }}>{count}</span>
        <span style={{ fontSize: 13, color: 'var(--text-2)', fontWeight: 500 }}>tracks match</span>
      </div>}
      <div style={{ display: 'flex', gap: 10, marginBottom: 10 }}>
        <div style={{
          flex: previewPos === 'main' ? 1 : 1, minWidth: 0,
          height: 50, borderRadius: 14, display: 'flex', alignItems: 'center', justifyContent: 'center',
          gap: 9, background: 'var(--accent)', color: 'var(--accent-fg)', fontSize: 15.5, fontWeight: 700,
          boxShadow: '0 10px 26px -10px var(--accent)'
        }}>
          <Icon name="shuffle" size={19} sw={2.2} />Shuffle &amp; Play
          {countInButton && <span style={{
            marginLeft: 2, fontFamily: 'var(--mono-font)', fontSize: 13.5, fontWeight: 700,
            padding: '2px 9px', borderRadius: 999,
            background: 'rgba(255,255,255,0.22)', color: 'var(--accent-fg)'
          }}>{count}</span>}
        </div>
        {previewPos === 'main' && previewNode}
      </div>
      <div style={{ display: 'flex', gap: 10 }}>
        {ghost('queueAdd', queueLabel)}
        {ghost('playNext', 'Play Next')}
        {previewPos === 'secondary' && previewNode}
      </div>
    </div>);

}

Object.assign(window, {
  QSEL, MATCH_COUNT, stateOf, FacetChip, ActiveFilters, PresetRow, SearchInput, SectionLabel, CTABar
});