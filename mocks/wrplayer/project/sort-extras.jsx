// sort-extras.jsx — Sort Order picker bottom sheet (PRD §4.1) and the
// Empty Inbox / End-of-Queue state (PRD §5.4).

// ─────────────────────────────────────────────────────────────
// Sort Order picker — shown on every entry into Sort Mode, over Play Mode
// ─────────────────────────────────────────────────────────────
function SortOrderPicker({ scheme, dark }) {
  const opts = [
  { key: 'newest', title: 'Newest first', sub: 'By file date, newest at the top', icon: 'chevDown' },
  { key: 'random', title: 'Random', sub: 'Shuffle the whole inbox', icon: 'shuffle' },
  { key: 'threshold', title: 'Closest to threshold', sub: 'Most decisively voted first', icon: 'sliders' }];

  const selected = 'threshold';
  return (
    <div style={{ position: 'relative', height: '100%', width: '100%', overflow: 'hidden' }}>
      <div style={{ position: 'absolute', inset: 0 }}>
        <PlayNowPlaying scheme={scheme} dark={dark} />
      </div>
      <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.42)' }} />

      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 0,
        background: 'var(--surface-2)', borderRadius: '24px 24px 0 0',
        boxShadow: '0 -20px 50px -12px rgba(0,0,0,0.4)', padding: '10px 18px 22px'
      }}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 12 }}>
          <div style={{ width: 40, height: 4, borderRadius: 2, background: 'var(--border)' }} />
        </div>
        <div style={{ fontSize: 18, fontWeight: 800, color: 'var(--text)', letterSpacing: '-0.02em' }}>Sort order</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: 1, marginBottom: 16 }}>
          Builds a fresh queue from 42 inbox tracks
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 9 }}>
          {opts.map((o) => {
            const on = o.key === selected;
            return (
              <div key={o.key} style={{
                display: 'flex', alignItems: 'center', gap: 13, padding: '13px 14px', borderRadius: 14,
                background: on ? 'var(--surface)' : 'var(--surface)',
                border: '1.5px solid', borderColor: on ? 'var(--accent)' : 'var(--border)'
              }}>
                <div style={{
                  width: 38, height: 38, borderRadius: 10, flexShrink: 0, display: 'flex',
                  alignItems: 'center', justifyContent: 'center',
                  background: on ? 'var(--accent)' : 'var(--surface-3)',
                  color: on ? 'var(--accent-fg)' : 'var(--text-2)'
                }}><Icon name={o.icon} size={19} sw={2} /></div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 14.5, fontWeight: 700, color: 'var(--text)' }}>{o.title}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 1 }}>{o.sub}</div>
                </div>
                <div style={{
                  width: 21, height: 21, borderRadius: '50%', flexShrink: 0,
                  border: '2px solid', borderColor: on ? 'var(--accent)' : 'var(--border)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center'
                }}>
                  {on && <div style={{ width: 11, height: 11, borderRadius: '50%', background: 'var(--accent)' }} />}
                </div>
              </div>);

          })}
        </div>

        <div style={{
          marginTop: 16, height: 50, borderRadius: 14, display: 'flex', alignItems: 'center',
          justifyContent: 'center', gap: 9, background: 'var(--accent)', color: 'var(--accent-fg)',
          fontSize: 15.5, fontWeight: 700, boxShadow: '0 12px 28px -12px var(--accent)'
        }}>
          <Icon name="play" size={18} />Start sorting
        </div>
      </div>
    </div>);

}

// ─────────────────────────────────────────────────────────────
// End-of-queue / Empty inbox state (PRD §5.4)
// ─────────────────────────────────────────────────────────────
function SortEmptyState({ scheme, dark }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      <TopBar mode="sort" subtitle="Sorting · 0 tracks" />
      <div style={{
        flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column', alignItems: 'center',
        justifyContent: 'center', padding: '0 36px', textAlign: 'center'
      }}>
        <div style={{
          width: 84, height: 84, borderRadius: '50%', display: 'flex', alignItems: 'center',
          justifyContent: 'center', marginBottom: 22,
          background: dark ? `oklch(0.32 0.05 152)` : `oklch(0.95 0.04 152)`,
          color: dark ? `oklch(0.82 0.12 152)` : `oklch(0.50 0.13 152)`
        }}>
          <Icon name="heart" size={38} sw={2} />
        </div>
        <div style={{ fontSize: 21, fontWeight: 800, color: 'var(--text)', letterSpacing: '-0.02em' }}>Queue cleared</div>
        <div style={{ fontSize: 14, color: 'var(--text-2)', marginTop: 8, lineHeight: 1.5, maxWidth: 280 }}>
          You worked through this sort queue. 6 skipped tracks will return next time you enter Sort Mode.
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 30, width: '100%', maxWidth: 280 }}>
          <div style={{
            height: 50, borderRadius: 14, display: 'flex', alignItems: 'center', justifyContent: 'center',
            gap: 9, background: 'var(--accent)', color: 'var(--accent-fg)', fontSize: 15, fontWeight: 700,
            boxShadow: '0 12px 28px -12px var(--accent)'
          }}><Icon name="reset" size={18} sw={2.2} />Re-enter Sort Mode</div>
          <div style={{
            height: 50, borderRadius: 14, display: 'flex', alignItems: 'center', justifyContent: 'center',
            gap: 9, background: 'var(--accent)', color: 'var(--accent-fg)', fontSize: 15, fontWeight: 700,
            boxShadow: '0 12px 28px -12px var(--accent)'
          }} data-comment-anchor="3831bd8206-div-107-11"><Icon name="play" size={18} />Switch to Play Mode</div>
        </div>
      </div>
    </div>);

}

Object.assign(window, { SortOrderPicker, SortEmptyState });