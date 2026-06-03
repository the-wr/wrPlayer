// tag-sheet.jsx — Tag Sheet modal bottom sheet (PRD §5.3 / §7).
// Fires when a +1 vote brings score to +2. Covers the bottom action panel.
// Rendered here over a dimmed Sort Mode Now Playing screen.

// A single editable text field row (Title / Artist / Album)
function TextField({ label, value, faded = false }) {
  return (
    <div style={{ marginBottom: 11 }}>
      <div style={{
        fontSize: 10.5, fontWeight: 700, letterSpacing: '0.07em', textTransform: 'uppercase',
        color: 'var(--text-3)', marginBottom: 5,
      }}>{label}</div>
      <div style={{
        padding: '10px 13px', borderRadius: 11, background: 'var(--surface)',
        border: '1px solid var(--border)', fontSize: 14.5, fontWeight: 600,
        color: faded ? 'var(--text-3)' : 'var(--text)',
      }}>{value}</div>
    </div>
  );
}

// Chip group for a tag dimension; `sel` lists selected labels, rest are addable
function ChipGroup({ dim, options, sel, scheme, dark, single = false, add = true }) {
  return (
    <div style={{ marginBottom: 16 }}>
      <SectionLabel dim={dim} rule={false} />
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        {options.map((label) => {
          const on = sel.includes(label);
          const s = chipStyle(dim, on ? 'on' : 'idle', scheme, dark);
          return (
            <div key={label} style={{ ...s, paddingRight: on ? 8 : 12 }}>
              {scheme === 'outline' && on &&
                <span style={{ width: 7, height: 7, borderRadius: '50%', background: dimDot(dim, dark) }} />}
              <span>{label}</span>
              {on && <span style={{ opacity: 0.6, display: 'flex' }}><Icon name="close" size={12} sw={2.6} /></span>}
            </div>
          );
        })}
        {add && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 5, padding: '7px 12px', borderRadius: 999,
            fontSize: 13, fontWeight: 600, color: 'var(--text-3)', border: '1px dashed var(--border)',
          }}><Icon name="plus" size={13} sw={2.4} />Add</div>
        )}
      </div>
    </div>
  );
}

function TagSheet({ scheme, dark }) {
  return (
    <div style={{ position: 'relative', height: '100%', width: '100%', overflow: 'hidden' }}>
      {/* dimmed underlying Sort screen */}
      <div style={{ position: 'absolute', inset: 0 }}>
        <SortNowPlaying scheme={scheme} dark={dark} plusSelected={true} />
      </div>
      <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.42)' }} />

      {/* bottom sheet */}
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 0, top: 64,
        background: 'var(--surface-2)', borderRadius: '24px 24px 0 0',
        boxShadow: '0 -20px 50px -12px rgba(0,0,0,0.4)',
        display: 'flex', flexDirection: 'column',
      }}>
        {/* grabber */}
        <div style={{ display: 'flex', justifyContent: 'center', padding: '10px 0 4px', flexShrink: 0 }}>
          <div style={{ width: 40, height: 4, borderRadius: 2, background: 'var(--border)' }} />
        </div>

        {/* header */}
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '6px 18px 12px', flexShrink: 0, borderBottom: '1px solid var(--border)',
        }}>
          <div>
            <div style={{ fontSize: 18, fontWeight: 800, color: 'var(--text)', letterSpacing: '-0.02em' }}>Add to Library</div>
            <div style={{ fontSize: 12.5, color: 'var(--text-3)', fontWeight: 500, marginTop: 1 }}>Tag this track to keep it</div>
          </div>
          <div style={{
            width: 32, height: 32, borderRadius: '50%', display: 'flex', alignItems: 'center',
            justifyContent: 'center', background: 'var(--surface-3)', color: 'var(--text-2)',
          }}><Icon name="close" size={17} sw={2.2} /></div>
        </div>

        {/* scroll body */}
        <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '14px 18px 6px' }}>
          <div style={{ display: 'flex', gap: 11, marginBottom: 6 }}>
            <div style={{ width: 56, flexShrink: 0 }}><Cover seedHue={SORT_TRACK.hue} radius={11} mark={SORT_TRACK.mark} /></div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <TextField label="Title" value="Tessellate" />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 11 }}>
            <div style={{ flex: 1 }}><TextField label="Artist" value="Halcyon Bench" /></div>
            <div style={{ flex: 1 }}><TextField label="Album" value="Soft Machinery" /></div>
          </div>

          <div style={{ height: 1, background: 'var(--border)', margin: '8px 0 16px' }} />

          <ChipGroup dim="genre" scheme={scheme} dark={dark}
            options={['Electronic', 'Ambient', 'Rock', 'Jazz']} sel={['Electronic', 'Ambient']} />
          <ChipGroup dim="mood" scheme={scheme} dark={dark}
            options={['Focus', 'Chill', 'Melancholy', 'Dark']} sel={['Focus']} />

          {/* Pace + BPM row */}
          <div style={{ marginBottom: 16 }}>
            <SectionLabel dim="pace" rule={false} />
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
              {['Slow', 'Medium', 'Fast'].map((label) => {
                const on = label === 'Medium';
                const s = chipStyle('pace', on ? 'on' : 'idle', scheme, dark);
                return <div key={label} style={s}>{label}</div>;
              })}
              <span style={{ width: 1, height: 20, background: 'var(--border)', margin: '0 2px' }} />
              <div style={{
                display: 'inline-flex', alignItems: 'center', gap: 6, padding: '7px 12px', borderRadius: 999,
                background: 'var(--surface-3)', border: '1px solid var(--border)', color: 'var(--text-2)',
                fontSize: 13, fontWeight: 600,
              }}>
                <span style={{ fontFamily: 'var(--mono-font)', fontWeight: 700, color: 'var(--text)' }}>120</span>
                <span style={{ fontSize: 11.5, color: 'var(--text-3)' }}>BPM</span>
                <Icon name="edit" size={13} sw={2} />
              </div>
            </div>
          </div>

          <ChipGroup dim="labels" scheme={scheme} dark={dark}
            options={['deep-work', 'commute']} sel={[]} />
        </div>

        {/* confirm */}
        <div style={{ flexShrink: 0, padding: '12px 18px 18px', borderTop: '1px solid var(--border)', background: 'var(--surface-2)' }}>
          <div style={{
            height: 52, borderRadius: 15, display: 'flex', alignItems: 'center', justifyContent: 'center',
            gap: 9, background: 'var(--accent)', color: 'var(--accent-fg)', fontSize: 15.5, fontWeight: 700,
            boxShadow: '0 12px 28px -12px var(--accent)',
          }}>
            <Icon name="heart" size={19} sw={2.2} />Confirm &amp; add to Library
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { TagSheet });
