// queue-editor-b.jsx — Variation B: left dimension rail, one facet group at a time
function QueueEditorB({ scheme, dark }) {
  const active = 'genre'; // selected dimension in the rail
  const dimKeys = Object.keys(DIMS);

  const activeCountFor = (dim) => Object.keys(QSEL[dim] || {}).length;

  const RailItem = ({ dim }) => {
    const sel = dim === active;
    const n = activeCountFor(dim);
    return (
      <div style={{
        display: 'flex', flexDirection: 'column', gap: 6, padding: '12px 10px',
        borderRadius: 13, position: 'relative',
        background: sel ? 'var(--surface)' : 'transparent',
        border: '1.5px solid', borderColor: sel ? 'var(--border)' : 'transparent',
        boxShadow: sel ? '0 4px 14px -8px rgba(0,0,0,0.25)' : 'none',
      }}>
        <span style={{ width: 9, height: 9, borderRadius: '50%', background: dimDot(dim, dark) }} />
        <span style={{
          fontSize: 12.5, fontWeight: sel ? 700 : 600,
          color: sel ? 'var(--text)' : 'var(--text-2)', letterSpacing: '0.005em',
        }}>{DIMS[dim].label}</span>
        {n > 0 && (
          <span style={{
            position: 'absolute', top: 9, right: 9, minWidth: 16, height: 16, padding: '0 4px',
            borderRadius: 8, background: 'var(--accent)', color: 'var(--accent-fg)',
            fontSize: 10.5, fontWeight: 700, fontFamily: 'var(--mono-font)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>{n}</span>
        )}
      </div>
    );
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      <TopBar mode="play" subtitle="Queue · 12 tracks" />

      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '14px 18px 12px', flexShrink: 0,
      }}>
        <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--text)', letterSpacing: '-0.01em' }}>Queue Editor</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, fontWeight: 600, color: 'var(--text-2)' }}>
          <Icon name="reset" size={15} sw={2.2} />Reset
        </div>
      </div>

      {/* active filters summary strip */}
      <div style={{ padding: '0 18px 12px', flexShrink: 0 }}>
        <ActiveFilters scheme={scheme} dark={dark} wrap={false} />
      </div>

      {/* split: rail | facet pane */}
      <div style={{ flex: 1, minHeight: 0, display: 'flex', gap: 12, padding: '0 14px' }}>
        {/* rail */}
        <div style={{
          width: 96, flexShrink: 0, display: 'flex', flexDirection: 'column', gap: 4,
          padding: 5, borderRadius: 16, background: 'var(--surface-3)', alignSelf: 'flex-start',
        }}>
          {dimKeys.map(dim => <RailItem key={dim} dim={dim} />)}
        </div>

        {/* facet pane for active dim */}
        <div style={{ flex: 1, minWidth: 0, minHeight: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          <div style={{ marginBottom: 13 }}><SearchInput placeholder="Filter genres…" /></div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 13 }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: dimDot(active, dark) }} />
            <span style={{ fontSize: 11.5, fontWeight: 700, letterSpacing: '0.07em', textTransform: 'uppercase', color: 'var(--text-2)' }}>{DIMS[active].label}</span>
            <span style={{ fontFamily: 'var(--mono-font)', fontSize: 11.5, color: 'var(--text-3)' }}>{FACETS[active].length}</span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 9, alignContent: 'flex-start' }}>
            {FACETS[active].map(([label, count]) => (
              <FacetChip key={label} dim={active} label={label} count={count} scheme={scheme} dark={dark} />
            ))}
            <div style={{
              display: 'flex', alignItems: 'center', gap: 5, padding: '7px 12px', borderRadius: 999,
              fontSize: 13, fontWeight: 600, color: 'var(--text-3)', border: '1px dashed var(--border)',
            }}><Icon name="plus" size={13} sw={2.4} />Add</div>
          </div>
        </div>
      </div>

      <CTABar />
    </div>
  );
}

Object.assign(window, { QueueEditorB });
