// queue-editor-a.jsx — Variation A: canonical vertical sections (PRD §6.2 faithful)
function QueueEditorA({ scheme, dark }) {
  const chipWrap = { display: 'flex', flexWrap: 'wrap', gap: 8 };
  const Section = ({ dim, addable }) =>
  <div style={{ marginBottom: 20 }}>
      <SectionLabel dim={dim} />
      <div style={chipWrap}>
        {FACETS[dim].map(([label, count]) =>
      <FacetChip key={label} dim={dim} label={label} count={count} scheme={scheme} dark={dark} />
      )}
        {addable &&
      <div style={{
        display: 'flex', alignItems: 'center', gap: 5, padding: '7px 12px', borderRadius: 999,
        fontSize: 13, fontWeight: 600, color: 'var(--text-3)', border: '1px dashed var(--border)'
      }}><Icon name="plus" size={13} sw={2.4} />Add</div>
      }
      </div>
    </div>;


  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      <TopBar mode="play" subtitle="Queue · 12 tracks" />

      {/* header: title + reset */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '14px 18px 12px', flexShrink: 0
      }}>
        <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--text)', letterSpacing: '-0.01em' }}>Queue Editor</span>
        <div style={{
          display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, fontWeight: 600, color: 'var(--text-2)'
        }}><Icon name="reset" size={15} sw={2.2} />Reset</div>
      </div>

      {/* presets */}
      <div style={{ padding: '0 18px 12px', flexShrink: 0 }}>
        <PresetRow />
      </div>

      {/* scroll body */}
      <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '0 18px' }}>
        {/* active filters */}
        <div style={{
          padding: '12px 13px', borderRadius: 14, background: 'var(--surface-2)',
          border: '1px solid var(--border)', marginBottom: 14
        }} data-comment-anchor="34d219e825-div-44-9">
          <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.06em', textTransform: 'uppercase', color: 'var(--text-3)', marginBottom: 9 }}>Active filters</div>
          <ActiveFilters scheme={scheme} dark={dark} />
        </div>

        {/* search */}
        <div style={{ marginBottom: 18 }}><SearchInput /></div>

        <Section dim="genre" addable />
        <Section dim="mood" addable />
        <Section dim="pace" />
        {/* fade hint that more sections follow (Labels / Artist / Album) */}
        <div style={{ height: 10 }} />
      </div>

      <CTABar />
    </div>);

}

Object.assign(window, { QueueEditorA });