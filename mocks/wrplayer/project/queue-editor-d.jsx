// queue-editor-d.jsx — Variation D: refined full-screen modal (synthesis of A/B/C feedback)
// • no persistent TopBar — editor is a full-screen modal (close affordance instead)
// • prominent match count, shown ONCE (dropped from the CTA bar to conserve space)
// • Build / Preview kept as a secondary tab
// • active filters: multiline like A, but no outer container (like B) and no dimension prefix (color-coded)
// • facet sections without "+ Add" (tags aren't created here) and without divider rules
function QueueEditorD({ scheme, dark }) {
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

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      {/* modal grabber */}
      <div style={{ flexShrink: 0, display: 'flex', justifyContent: 'center', paddingTop: 9 }}>
        <span style={{ width: 38, height: 4, borderRadius: 999, background: 'var(--border)' }} />
      </div>

      {/* modal header: title + reset + close */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 12,
        padding: '12px 16px 10px', flexShrink: 0
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

      {/* prominent result count + Build/Preview tab */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '0 16px 12px', flexShrink: 0
      }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }} data-comment-anchor="8f5cde77a4-div-47-9">
          <span style={{ fontFamily: 'var(--mono-font)', fontSize: 34, fontWeight: 700, color: 'var(--accent)', lineHeight: 1, letterSpacing: '-0.02em' }}>{MATCH_COUNT}</span>
          <span style={{ fontSize: 14, color: 'var(--text-2)', fontWeight: 600 }}>tracks match</span>
        </div>
        <div style={{ display: 'flex', padding: 3, borderRadius: 11, background: 'var(--surface-3)', gap: 3 }} data-comment-anchor="df9a931fad-div-51-9">
          {[['Build', true], ['Preview', false]].map(([l, act]) =>
          <div key={l} style={{
            padding: '7px 13px', borderRadius: 8, fontSize: 13, fontWeight: 600,
            background: act ? 'var(--surface)' : 'transparent', color: act ? 'var(--text)' : 'var(--text-2)',
            boxShadow: act ? '0 2px 8px -4px rgba(0,0,0,0.3)' : 'none',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6
          }}>
              <Icon name={l === 'Build' ? 'sliders' : 'list'} size={15} sw={2} />{l}
            </div>
          )}
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

      {/* CTA bar — count omitted (already prominent above) */}
      <CTABar showCount={false} />
    </div>);

}

Object.assign(window, { QueueEditorD });