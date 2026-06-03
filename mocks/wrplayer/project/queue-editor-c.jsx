// queue-editor-c.jsx — Variation C: boolean expression builder + live result preview
function QueueEditorC({ scheme, dark }) {
  const miniChip = (dim, label, excluded) => {
    const s = chipStyle(dim, excluded ? 'off' : 'on', scheme === 'solid' ? 'solid' : 'tinted', dark);
    return <span key={dim + label} style={{ ...s, padding: '4px 10px', fontSize: 12.5 }}>
      {excluded && <span style={{ fontFamily: 'var(--mono-font)', fontWeight: 700 }}>−</span>}{label}
    </span>;
  };
  const op = (t) =>
  <span style={{ fontFamily: 'var(--mono-font)', fontSize: 11.5, fontWeight: 700, color: 'var(--text-3)', letterSpacing: '0.05em', alignSelf: 'center' }}>{t}</span>;

  const paren = (t) =>
  <span style={{ fontFamily: 'var(--mono-font)', fontSize: 18, fontWeight: 400, color: 'var(--text-3)', alignSelf: 'center' }}>{t}</span>;


  const tracks = [
  { t: 'Tessellate', a: 'Halcyon Bench', dims: ['genre', 'mood', 'pace'], hue: 256, mark: 'ring' },
  { t: 'Carrier Lines', a: 'Kō Mirin', dims: ['genre', 'mood'], hue: 196, mark: 'bar' },
  { t: 'Slow Ferric', a: 'Vetiver Theory', dims: ['genre', 'pace', 'labels'], hue: 88, mark: 'block' },
  { t: 'North Cell', a: 'Sable Unit', dims: ['genre', 'mood', 'labels'], hue: 305, mark: 'ring' },
  { t: 'Driftwork', a: 'Halcyon Bench', dims: ['genre', 'mood'], hue: 152, mark: 'bar' }];


  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      <TopBar mode="play" subtitle="Queue · 12 tracks" />

      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '14px 18px 10px', flexShrink: 0
      }}>
        <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--text)', letterSpacing: '-0.01em' }}>Queue Editor</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, fontWeight: 600, color: 'var(--text-2)' }}>
          <Icon name="reset" size={15} sw={2.2} />Reset
        </div>
      </div>

      {/* formula card */}
      <div style={{ padding: '0 18px', flexShrink: 0 }}>
        <div style={{
          padding: '14px 15px', borderRadius: 16, background: 'var(--surface-2)', border: '1px solid var(--border)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 11 }}>
            <span style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.06em', textTransform: 'uppercase', color: 'var(--text-3)' }}>Filter expression</span>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 5 }} data-comment-anchor="221470f9ba-div-45-13">
              <span style={{ fontFamily: 'var(--mono-font)', fontSize: 19, fontWeight: 700, color: 'var(--accent)' }}>{MATCH_COUNT}</span>
              <span style={{ fontSize: 12, color: 'var(--text-2)', fontWeight: 500 }}>match</span>
            </div>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, alignItems: 'center' }}>
            {paren('(')}
            {miniChip('genre', 'Rock')}
            {op('OR')}
            {miniChip('genre', 'Jazz')}
            {paren(')')}
            {op('AND')}
            {miniChip('mood', 'Hype')}
            {op('AND NOT')}
            {miniChip('pace', 'Slow', true)}
            <span style={{
              display: 'inline-flex', alignItems: 'center', gap: 4, padding: '4px 10px', borderRadius: 999,
              fontSize: 12.5, fontWeight: 600, color: 'var(--text-3)', border: '1px dashed var(--border)'
            }}><Icon name="plus" size={12} sw={2.4} /></span>
          </div>
        </div>
      </div>

      {/* segmented build / preview */}
      <div style={{ padding: '14px 18px 10px', flexShrink: 0 }} data-comment-anchor="76f688dff1-div-69-7">
        <div style={{ display: 'flex', padding: 3, borderRadius: 11, background: 'var(--surface-3)', gap: 3 }}>
          {[['Build', false], ['Preview', true]].map(([l, act]) =>
          <div key={l} style={{
            flex: 1, textAlign: 'center', padding: '7px 0', borderRadius: 8, fontSize: 13, fontWeight: 600,
            background: act ? 'var(--surface)' : 'transparent', color: act ? 'var(--text)' : 'var(--text-2)',
            boxShadow: act ? '0 2px 8px -4px rgba(0,0,0,0.3)' : 'none',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6
          }}>
              {l === 'Build' && <Icon name="sliders" size={15} sw={2} />}
              {l === 'Preview' && <Icon name="list" size={15} sw={2} />}
              {l}{act && <span style={{ fontFamily: 'var(--mono-font)', fontSize: 11.5, color: 'var(--text-3)' }}>{MATCH_COUNT}</span>}
            </div>
          )}
        </div>
      </div>

      {/* matching tracks preview */}
      <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '0 18px' }}>
        {tracks.map((tr, i) =>
        <div key={i} style={{
          display: 'flex', alignItems: 'center', gap: 12, padding: '9px 0',
          borderBottom: i < tracks.length - 1 ? '1px solid var(--border)' : 'none'
        }}>
            <div style={{ width: 44 }}><Cover seedHue={tr.hue} mark={tr.mark} radius={9} /></div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14.5, fontWeight: 600, color: 'var(--text)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tr.t}</div>
              <div style={{ fontSize: 12.5, color: 'var(--text-2)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tr.a}</div>
            </div>
            <div style={{ display: 'flex', gap: 4 }}>
              {tr.dims.map((d) => <span key={d} style={{ width: 7, height: 7, borderRadius: '50%', background: dimDot(d, dark) }} />)}
            </div>
          </div>
        )}
      </div>

      <CTABar />
    </div>);

}

Object.assign(window, { QueueEditorC });