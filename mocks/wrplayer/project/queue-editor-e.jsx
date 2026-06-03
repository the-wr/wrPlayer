// queue-editor-e.jsx — Variation E: refined modal, evolved from D per review
// • keeps the preset bar (re-added — D had dropped it)
// • Build/Preview tab → press-&-hold "Preview" button: a light, short-lived
//   matching-tracks overlay that shows only while the button is held
// • match count moved off the top and merged into the Shuffle & Play CTA
function QueueEditorE({ scheme, dark }) {
  const [preview, setPreview] = React.useState(false);
  const chipWrap = { display: 'flex', flexWrap: 'wrap', gap: 8 };

  const Section = ({ dim }) =>
  <div style={{ marginBottom: 18 }}>
      <SectionLabel dim={dim} rule={false} />
      <div style={chipWrap}>
        {FACETS[dim].map(([label, count]) =>
      <FacetChip key={label} dim={dim} label={label} count={count} scheme={scheme} dark={dark} />
      )}
      </div>
    </div>;

  // sample matches for the hold-to-preview overlay
  const tracks = [
  { t: 'Tessellate', a: 'Halcyon Bench', hue: 256, mark: 'ring' },
  { t: 'Carrier Lines', a: 'Kō Mirin', hue: 196, mark: 'bar' },
  { t: 'Slow Ferric', a: 'Vetiver Theory', hue: 88, mark: 'block' },
  { t: 'North Cell', a: 'Sable Unit', hue: 305, mark: 'ring' },
  { t: 'Driftwork', a: 'Halcyon Bench', hue: 152, mark: 'bar' },
  { t: 'Ridgeline', a: 'Kō Mirin', hue: 256, mark: 'block' }];


  const hold = {
    onPointerDown: (e) => {setPreview(true);try {e.currentTarget.setPointerCapture(e.pointerId);} catch (_) {}},
    onPointerUp: () => setPreview(false),
    onPointerLeave: () => setPreview(false),
    onPointerCancel: () => setPreview(false)
  };

  return (
    <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      {/* modal grabber */}
      <div style={{ flexShrink: 0, display: 'flex', justifyContent: 'center', paddingTop: 9 }}>
        <span style={{ width: 38, height: 4, borderRadius: 999, background: 'var(--border)' }} />
      </div>

      {/* modal header: title + reset + close */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 12,
        padding: '12px 16px 12px', flexShrink: 0
      }}>
        <span style={{ fontSize: 20, fontWeight: 700, color: 'var(--text)', letterSpacing: '-0.015em', flex: 1 }}>Queue Editor</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 13, fontWeight: 600, color: 'var(--text-2)' }}>
          <Icon name="reset" size={15} sw={2.2} />Reset
        </div>
        <div style={{
          width: 32, height: 32, borderRadius: 999, display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'var(--surface-3)', color: 'var(--text-2)'
        }}><Icon name="close" size={17} sw={2.4} /></div>
      </div>

      {/* preset bar (kept) + hold-to-preview */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '0 16px 12px', flexShrink: 0 }} data-comment-anchor="af519f657d-div-60-7">
        <div style={{ flex: 1, minWidth: 0 }}><PresetRow /></div>
        <div {...hold} style={{
          flexShrink: 0, display: 'flex', alignItems: 'center', gap: 6, padding: '8px 13px', borderRadius: 999,
          fontSize: 13, fontWeight: 600, whiteSpace: 'nowrap', userSelect: 'none', touchAction: 'none', cursor: 'pointer',
          background: preview ? 'var(--accent)' : 'var(--surface-3)',
          color: preview ? 'var(--accent-fg)' : 'var(--text-2)',
          border: '1px solid', borderColor: preview ? 'var(--accent)' : 'var(--border)',
          transition: 'background .12s, color .12s'
        }}>
          <Icon name="list" size={15} sw={2.2} />Preview
        </div>
      </div>

      {/* scroll body */}
      <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '0 16px' }}>
        {/* active filters — multiline, no container, no dimension prefix */}
        <div style={{ marginBottom: 14 }}>
          <ActiveFilters scheme={scheme} dark={dark} showDim={false} />
        </div>

        {/* search */}
        <div style={{ marginBottom: 18 }}><SearchInput /></div>

        <Section dim="genre" />
        <Section dim="mood" />
        <Section dim="pace" />
        <div style={{ height: 10 }} />
      </div>

      {/* CTA bar — count merged into the primary button */}
      <CTABar showCount={false} countInButton={true} />

      {/* hold-to-preview overlay — light, present only while held */}
      <div style={{
        position: 'absolute', inset: 0, zIndex: 6, pointerEvents: 'none',
        opacity: preview ? 1 : 0, transition: 'opacity .14s'
      }}>
        <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.22)' }} />
        <div style={{
          position: 'absolute', left: 0, right: 0, bottom: 0, maxHeight: '74%',
          display: 'flex', flexDirection: 'column',
          background: 'var(--surface-2)', borderTopLeftRadius: 22, borderTopRightRadius: 22,
          boxShadow: '0 -18px 44px -14px rgba(0,0,0,0.4)',
          padding: '12px 16px 20px',
          transform: preview ? 'translateY(0)' : 'translateY(16px)', transition: 'transform .16s'
        }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 10 }}>
            <span style={{ width: 38, height: 4, borderRadius: 999, background: 'var(--border)' }} />
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 8 }}>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 7 }}>
              <span style={{ fontFamily: 'var(--mono-font)', fontSize: 20, fontWeight: 700, color: 'var(--accent)' }}>{MATCH_COUNT}</span>
              <span style={{ fontSize: 13.5, fontWeight: 600, color: 'var(--text)' }}>matching tracks</span>
            </div>
            <span style={{ fontSize: 12, color: 'var(--text-3)', fontWeight: 500 }}>Release to close</span>
          </div>
          <div style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
            {tracks.map((tr, i) =>
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '8px 0' }}>
                <div style={{ width: 40 }}><Cover seedHue={tr.hue} mark={tr.mark} radius={8} /></div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tr.t}</div>
                  <div style={{ fontSize: 12.5, color: 'var(--text-2)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tr.a}</div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>);

}

Object.assign(window, { QueueEditorE });