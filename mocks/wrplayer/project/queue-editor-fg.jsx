// queue-editor-fg.jsx — Variations F & G: preview button relocated to the CTA bar
// Shared base with variant E's body, but the press-&-hold "Preview" button now
// lives in the CTA area instead of crowding the preset row — which lets the
// preset bar run full width and keep its "+ New" affordance visible.
//   • F → preview on the first CTA row, beside Shuffle & Play
//   • G → preview on the second CTA row, beside Play Next
function QueueModalBase({ scheme, dark, previewPos, showClose = true, excludeDash = true, queueLabel = 'Add to Queue', showGrabber = true, forcePreview = false, previewFull = false }) {
  const [preview, setPreview] = React.useState(false);
  const open = preview || forcePreview;
  const chipWrap = { display: 'flex', flexWrap: 'wrap', gap: 8 };

  const Section = ({ dim }) =>
    <div style={{ marginBottom: 18 }}>
      <SectionLabel dim={dim} rule={false} />
      <div style={chipWrap}>
        {FACETS[dim].map(([label, count]) =>
          <FacetChip key={label} dim={dim} label={label} count={count} scheme={scheme} dark={dark} excludeDash={excludeDash} />
        )}
      </div>
    </div>;

  const tracks = [
    { t: 'Tessellate', a: 'Halcyon Bench', hue: 256, mark: 'ring' },
    { t: 'Carrier Lines', a: 'Kō Mirin', hue: 196, mark: 'bar' },
    { t: 'Slow Ferric', a: 'Vetiver Theory', hue: 88, mark: 'block' },
    { t: 'North Cell', a: 'Sable Unit', hue: 305, mark: 'ring' },
    { t: 'Driftwork', a: 'Halcyon Bench', hue: 152, mark: 'bar' },
    { t: 'Ridgeline', a: 'Kō Mirin', hue: 256, mark: 'block' },
  ];

  // longer set for the full-height expanded preview
  const fullList = [
    ...tracks,
    { t: 'Pale Index', a: 'Kō Mirin', hue: 305, mark: 'block' },
    { t: 'Underpass', a: 'Vetiver Theory', hue: 58, mark: 'ring' },
    { t: 'Long Exposure', a: 'Sable Unit', hue: 196, mark: 'bar' },
    { t: 'Null Island', a: 'Halcyon Bench', hue: 88, mark: 'block' },
    { t: 'Glass Run', a: 'Sable Unit', hue: 256, mark: 'ring' },
    { t: 'Even Field', a: 'Vetiver Theory', hue: 152, mark: 'bar' },
    { t: 'Marrowlight', a: 'Halcyon Bench', hue: 305, mark: 'bar' },
  ];
  const showList = previewFull ? fullList : tracks;

  const TrackRow = (tr, i) =>
    <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '8px 0' }}>
      <div style={{ width: 40 }}><Cover seedHue={tr.hue} mark={tr.mark} radius={8} /></div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tr.t}</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-2)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{tr.a}</div>
      </div>
      <span style={{ fontFamily: 'var(--mono-font)', fontSize: 11.5, color: 'var(--text-3)' }}>{`3:${10 + ((tr.hue + i) % 49)}`}</span>
    </div>;

  const hold = {
    onPointerDown: (e) => { setPreview(true); try { e.currentTarget.setPointerCapture(e.pointerId); } catch (_) {} },
    onPointerUp: () => setPreview(false),
    onPointerLeave: () => setPreview(false),
    onPointerCancel: () => setPreview(false),
  };

  // preview button — tall (beside main CTA) for F, ghost-row height for G
  const tall = previewPos === 'main';
  const previewNode =
    <div {...hold} style={{
      flex: tall ? '0 0 auto' : 1, minWidth: 0,
      height: tall ? 50 : 46, padding: tall ? '0 18px' : 0, borderRadius: tall ? 14 : 13,
      display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 7,
      userSelect: 'none', touchAction: 'none', cursor: 'pointer', whiteSpace: 'nowrap',
      fontSize: tall ? 14.5 : 13.5, fontWeight: tall ? 650 : 600,
      transition: 'background .12s, color .12s, border-color .12s',
      background: open ? 'var(--accent)' : 'var(--surface)',
      color: open ? 'var(--accent-fg)' : 'var(--text)',
      border: '1.5px solid', borderColor: open ? 'var(--accent)' : 'var(--border)',
    }}>
      <Icon name="list" size={18} sw={2} />Preview
    </div>;

  return (
    <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      {/* modal grabber */}
      {showGrabber && <div style={{ flexShrink: 0, display: 'flex', justifyContent: 'center', paddingTop: 9 }}>
        <span style={{ width: 38, height: 4, borderRadius: 999, background: 'var(--border)' }} />
      </div>}

      {/* modal header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 16px 12px', flexShrink: 0 }}>
        <span style={{ fontSize: 20, fontWeight: 700, color: 'var(--text)', letterSpacing: '-0.015em', flex: 1 }}>Queue Editor</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 13, fontWeight: 600, color: 'var(--text-2)' }}>
          <Icon name="reset" size={15} sw={2.2} />Reset
        </div>
        {showClose && <div style={{
          width: 32, height: 32, borderRadius: 999, display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'var(--surface-3)', color: 'var(--text-2)',
        }}><Icon name="close" size={17} sw={2.4} /></div>}
      </div>

      {/* preset bar — full width now, keeps the "+ New" affordance */}
      <div style={{ padding: '0 16px 12px', flexShrink: 0 }}><PresetRow /></div>

      {/* scroll body */}
      <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '0 16px' }}>
        <div style={{ marginBottom: 14 }}>
          <ActiveFilters scheme={scheme} dark={dark} showDim={false} excludeDash={excludeDash} />
        </div>
        <div style={{ marginBottom: 18 }}><SearchInput /></div>
        <Section dim="genre" />
        <Section dim="mood" />
        <Section dim="pace" />
        <div style={{ height: 10 }} />
      </div>

      {/* CTA bar — count merged into main button, preview button placed per variant */}
      <CTABar showCount={false} countInButton={true} previewNode={previewNode} previewPos={previewPos} queueLabel={queueLabel} />

      {/* hold-to-preview overlay (forced open + full-height in the expanded artboard) */}
      <div style={{
        position: 'absolute', inset: 0, zIndex: 6, pointerEvents: 'none',
        opacity: open ? 1 : 0, transition: 'opacity .14s',
      }}>
        <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.22)' }} />
        <div style={previewFull ? {
          position: 'absolute', left: 0, right: 0, top: 0, bottom: 0,
          display: 'flex', flexDirection: 'column',
          background: 'var(--surface-2)', borderTopLeftRadius: 22, borderTopRightRadius: 22,
          boxShadow: '0 -18px 44px -14px rgba(0,0,0,0.4)', padding: '12px 16px 20px',
          transform: open ? 'translateY(0)' : 'translateY(16px)', transition: 'transform .16s',
        } : {
          position: 'absolute', left: 0, right: 0, bottom: 0, maxHeight: '74%',
          display: 'flex', flexDirection: 'column',
          background: 'var(--surface-2)', borderTopLeftRadius: 22, borderTopRightRadius: 22,
          boxShadow: '0 -18px 44px -14px rgba(0,0,0,0.4)', padding: '12px 16px 20px',
          transform: open ? 'translateY(0)' : 'translateY(16px)', transition: 'transform .16s',
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
            {showList.map(TrackRow)}
          </div>
        </div>
      </div>
    </div>
  );
}

function QueueEditorF(props) { return <QueueModalBase {...props} previewPos="main" />; }
function QueueEditorG(props) { return <QueueModalBase {...props} previewPos="secondary" />; }
// H — refined G: no in-modal close (back nav dismisses), no redundant exclude dash, "Enqueue" label
function QueueEditorH(props) { return <QueueModalBase {...props} previewPos="secondary" showClose={false} excludeDash={false} queueLabel="Enqueue" showGrabber={false} />; }
// H (expanded) — depicts the held state: full-height preview shown open
function QueueEditorHPreview(props) { return <QueueModalBase {...props} previewPos="secondary" showClose={false} excludeDash={false} queueLabel="Enqueue" showGrabber={false} forcePreview previewFull />; }

Object.assign(window, { QueueEditorF, QueueEditorG, QueueEditorH, QueueEditorHPreview });
