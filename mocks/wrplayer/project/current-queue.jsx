// current-queue.jsx — Current Queue screen (PRD §6.3).
// Ordered list of queued tracks: drag handle, art thumb, title/artist, now-playing highlight.

const QUEUE_TRACKS = [
  { title: 'Carrier Lines', artist: 'Kō Mirin', hue: 196, mark: 'bar', playing: false, done: true },
  { title: 'Tessellate', artist: 'Halcyon Bench', hue: 256, mark: 'ring', playing: true },
  { title: 'Slow Ferric', artist: 'Vetiver Theory', hue: 152, mark: 'block' },
  { title: 'North Cell', artist: 'Sable Unit', hue: 88, mark: 'ring' },
  { title: 'Driftwork', artist: 'Halcyon Bench', hue: 256, mark: 'bar' },
  { title: 'Pale Index', artist: 'Kō Mirin', hue: 305, mark: 'block' },
  { title: 'Underpass', artist: 'Vetiver Theory', hue: 58, mark: 'ring' },
  { title: 'Long Exposure', artist: 'Sable Unit', hue: 196, mark: 'bar' },
];

function QueueRow({ t, swipe = false }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 13, padding: '10px 12px',
      borderRadius: 14, position: 'relative',
      background: t.playing ? 'var(--surface-2)' : 'transparent',
      border: t.playing ? '1px solid var(--border)' : '1px solid transparent',
      boxShadow: t.playing ? '0 6px 18px -12px rgba(0,0,0,0.4)' : 'none',
      transform: swipe ? 'translateX(-46px)' : 'none',
    }}>
      <div style={{ color: 'var(--text-3)', display: 'flex', flexShrink: 0, opacity: 0.7 }}>
        <Icon name="grip" size={20} />
      </div>
      <div style={{ width: 46, flexShrink: 0, position: 'relative' }}>
        <Cover seedHue={t.hue} radius={9} mark={t.mark} />
        {t.playing && (
          <div style={{
            position: 'absolute', inset: 0, borderRadius: 9, background: 'rgba(0,0,0,0.38)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff',
          }}><Icon name="pause" size={18} /></div>
        )}
      </div>
      <div style={{ flex: 1, minWidth: 0, opacity: t.done ? 0.45 : 1 }}>
        <div style={{
          fontSize: 14.5, fontWeight: t.playing ? 800 : 650,
          color: t.playing ? 'var(--accent)' : 'var(--text)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        }}>{t.title}</div>
        <div style={{ fontSize: 12.5, fontWeight: 500, color: 'var(--text-3)', marginTop: 1 }}>{t.artist}</div>
      </div>
      {t.playing
        ? <Equalizer />
        : <span style={{ fontFamily: 'var(--mono-font)', fontSize: 11.5, color: 'var(--text-3)' }}>3:2{t.hue % 9}</span>}
      {swipe && (
        <div style={{
          position: 'absolute', right: -46, top: 0, bottom: 0, width: 46,
          background: `oklch(0.60 0.16 24)`, borderRadius: 14,
          display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff',
        }}><Icon name="close" size={18} sw={2.4} /></div>
      )}
    </div>
  );
}

// tiny animated-looking equalizer for the now-playing row
function Equalizer() {
  const bars = [10, 16, 7, 13];
  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', gap: 2.5, height: 16, flexShrink: 0 }}>
      {bars.map((h, i) => (
        <span key={i} style={{ width: 3, height: h, borderRadius: 2, background: 'var(--accent)', opacity: 0.85 }} />
      ))}
    </div>
  );
}

function CurrentQueue({ scheme, dark }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      <TopBar mode="play" subtitle="Queue · 12 tracks" />

      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '14px 18px 10px', flexShrink: 0,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
          <Icon name="chevDown" size={20} sw={2.2} />
          <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--text)', letterSpacing: '-0.01em' }}>Current Queue</span>
        </div>
        <span style={{ fontFamily: 'var(--mono-font)', fontSize: 12.5, fontWeight: 600, color: 'var(--text-3)' }}>12</span>
      </div>

      {/* up-next hint */}
      <div style={{
        padding: '0 18px 8px', flexShrink: 0, fontSize: 11, fontWeight: 700,
        letterSpacing: '0.06em', textTransform: 'uppercase', color: 'var(--text-3)',
      }}>Drag to reorder · swipe to remove</div>

      <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '0 8px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {QUEUE_TRACKS.map((t, i) => <QueueRow key={i} t={t} swipe={i === 4} />)}
        </div>
      </div>

      {/* mini now-playing footer */}
      <div style={{
        flexShrink: 0, padding: '12px 16px 18px', background: 'var(--surface-2)',
        borderTop: '1px solid var(--border)', display: 'flex', alignItems: 'center', gap: 13,
      }}>
        <div style={{ width: 44, flexShrink: 0 }}><Cover seedHue={256} radius={10} mark="ring" /></div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--text)' }}>Tessellate</div>
          <div style={{ fontSize: 12, color: 'var(--text-3)' }}>Halcyon Bench</div>
        </div>
        <div style={{
          width: 48, height: 48, borderRadius: '50%', display: 'flex', alignItems: 'center',
          justifyContent: 'center', background: 'var(--accent)', color: 'var(--accent-fg)',
        }}><Icon name="pause" size={24} /></div>
      </div>
    </div>
  );
}

Object.assign(window, { CurrentQueue, QUEUE_TRACKS });
