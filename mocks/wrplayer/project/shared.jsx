// shared.jsx — wrPlayer design system: dimension palette, chips, icons, sample data
// Exported to window for use across screen files.

// ─────────────────────────────────────────────────────────────
// Tag dimensions (PRD §6.2) — hue per dimension, consistent C/L within a scheme
// ─────────────────────────────────────────────────────────────
const DIMS = {
  genre:  { label: 'Genre',  hue: 256 },
  mood:   { label: 'Mood',   hue: 305 },
  pace:   { label: 'Pace',   hue: 152 },
  labels: { label: 'Labels', hue: 58  },
  artist: { label: 'Artist', hue: 196 },
  album:  { label: 'Album',  hue: 88  },
};

// chipStyle(dimension, state, scheme, dark)
// state: 'idle' | 'on' (included) | 'off' (excluded)
// scheme: 'tinted' | 'solid' | 'outline'
function chipStyle(dim, state, scheme, dark) {
  const H = (DIMS[dim] && DIMS[dim].hue) ?? 256;
  const base = {
    display: 'inline-flex', alignItems: 'center', gap: 7,
    padding: '7px 12px', borderRadius: 999, fontSize: 13.5,
    fontWeight: 550, lineHeight: 1, whiteSpace: 'nowrap',
    border: '1.5px solid transparent', boxSizing: 'border-box',
    fontFamily: 'var(--ui-font)', letterSpacing: '0.01em',
    transition: 'none',
  };
  // EXCLUDED — always red, strikethrough
  if (state === 'off') {
    const rh = 24;
    return { ...base,
      background: dark ? `oklch(0.30 0.07 ${rh})` : `oklch(0.96 0.035 ${rh})`,
      color: dark ? `oklch(0.80 0.12 ${rh})` : `oklch(0.52 0.18 ${rh})`,
      borderColor: dark ? `oklch(0.45 0.10 ${rh})` : `oklch(0.85 0.10 ${rh})`,
      borderStyle: 'dashed',
      textDecoration: 'line-through',
      textDecorationThickness: '1.5px',
    };
  }
  // IDLE — neutral
  if (state === 'idle') {
    return { ...base,
      background: 'var(--chip-idle-bg)',
      color: 'var(--chip-idle-fg)',
      borderColor: 'var(--chip-idle-bd)',
    };
  }
  // INCLUDED — colored per scheme
  if (scheme === 'solid') {
    return { ...base,
      background: dark ? `oklch(0.58 0.13 ${H})` : `oklch(0.60 0.15 ${H})`,
      color: dark ? `oklch(0.16 0.02 ${H})` : '#fff',
      borderColor: 'transparent',
      fontWeight: 650,
    };
  }
  if (scheme === 'outline') {
    return { ...base,
      background: 'transparent',
      color: dark ? `oklch(0.82 0.11 ${H})` : `oklch(0.46 0.14 ${H})`,
      borderColor: dark ? `oklch(0.55 0.11 ${H})` : `oklch(0.70 0.13 ${H})`,
    };
  }
  // tinted (default)
  return { ...base,
    background: dark ? `oklch(0.32 0.055 ${H})` : `oklch(0.955 0.045 ${H})`,
    color: dark ? `oklch(0.84 0.11 ${H})` : `oklch(0.44 0.14 ${H})`,
    borderColor: dark ? `oklch(0.40 0.06 ${H})` : `oklch(0.90 0.06 ${H})`,
  };
}

// A small color "dot" for a dimension (used in legends / mono scheme)
function dimDot(dim, dark) {
  const H = (DIMS[dim] && DIMS[dim].hue) ?? 256;
  return dark ? `oklch(0.70 0.13 ${H})` : `oklch(0.58 0.15 ${H})`;
}

// ─────────────────────────────────────────────────────────────
// Icons — minimal stroke set (functional UI glyphs only)
// ─────────────────────────────────────────────────────────────
function Icon({ name, size = 22, stroke = 'currentColor', sw = 2, fill = 'none' }) {
  const p = { fill, stroke, strokeWidth: sw, strokeLinecap: 'round', strokeLinejoin: 'round' };
  const paths = {
    play:   <path d="M7 5l12 7-12 7V5z" fill={stroke} stroke="none" />,
    pause:  <g fill={stroke} stroke="none"><rect x="6" y="5" width="4" height="14" rx="1"/><rect x="14" y="5" width="4" height="14" rx="1"/></g>,
    prev:   <g fill={stroke} stroke="none"><path d="M18 5v14l-9-7 9-7z"/><rect x="5" y="5" width="2.6" height="14" rx="1"/></g>,
    next:   <g fill={stroke} stroke="none"><path d="M6 5v14l9-7-9-7z"/><rect x="16.4" y="5" width="2.6" height="14" rx="1"/></g>,
    minus:  <path d="M5 12h14" {...p} />,
    plus:   <path d="M12 5v14M5 12h14" {...p} />,
    skip:   <g {...p}><path d="M5 5v14l8-7-8-7z"/><path d="M13 5v14l8-7-8-7z"/></g>,
    search: <g {...p}><circle cx="11" cy="11" r="7"/><path d="M21 21l-4-4"/></g>,
    close:  <path d="M6 6l12 12M18 6L6 18" {...p} />,
    reset:  <g {...p}><path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v4h4"/></g>,
    shuffle:<g {...p}><path d="M16 4h4v4"/><path d="M4 20l16-16"/><path d="M4 4l5 5"/><path d="M16 20h4v-4"/><path d="M14 14l6 6"/></g>,
    queueAdd:<g {...p}><path d="M4 7h11M4 12h11M4 17h7"/><path d="M18 13v6M15 16h6"/></g>,
    playNext:<g {...p}><path d="M4 7h13M4 12h13M4 17h7"/><path d="M17 14l4 3-4 3v-6z" fill={stroke} stroke="none"/></g>,
    list:   <g {...p}><path d="M4 7h16M4 12h16M4 17h16"/></g>,
    edit:   <g {...p}><path d="M5 19h14"/><path d="M14 6l4 4-9 9H5v-4l9-9z"/></g>,
    chevDown:<path d="M6 9l6 6 6-6" {...p} />,
    chevRight:<path d="M9 6l6 6-6 6" {...p} />,
    grip:   <g fill={stroke} stroke="none"><circle cx="9" cy="6" r="1.6"/><circle cx="15" cy="6" r="1.6"/><circle cx="9" cy="12" r="1.6"/><circle cx="15" cy="12" r="1.6"/><circle cx="9" cy="18" r="1.6"/><circle cx="15" cy="18" r="1.6"/></g>,
    sliders:<g {...p}><path d="M4 6h10M18 6h2M4 12h2M10 12h10M4 18h8M16 18h4"/><circle cx="15" cy="6" r="2" fill={stroke} stroke="none"/><circle cx="8" cy="12" r="2" fill={stroke} stroke="none"/><circle cx="13" cy="18" r="2" fill={stroke} stroke="none"/></g>,
    note:   <g {...p}><path d="M9 18V5l10-2v13"/><circle cx="6" cy="18" r="3" fill={stroke} stroke="none"/><circle cx="16" cy="16" r="3" fill={stroke} stroke="none"/></g>,
    repeat: <g {...p}><path d="M17 3l3 3-3 3"/><path d="M4 12V10a4 4 0 0 1 4-4h12"/><path d="M7 21l-3-3 3-3"/><path d="M20 12v2a4 4 0 0 1-4 4H4"/></g>,
    heart:  <path d="M12 20s-7-4.6-9.3-9C1.3 8 2.6 5 5.6 5c1.9 0 3.1 1.1 3.8 2.2.7-1.1 1.9-2.2 3.8-2.2 3 0 4.3 3 2.9 6-2.3 4.4-9.3 9-9.3 9z" {...p} />,
    gear:   <g {...p}><circle cx="12" cy="12" r="3.1"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></g>,
  };
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" style={{ display: 'block', flexShrink: 0 }}>
      {paths[name]}
    </svg>
  );
}

// ─────────────────────────────────────────────────────────────
// Persistent top bar — [Sort | Play] segmented + active-list title (PRD §4.1)
// ─────────────────────────────────────────────────────────────
function TopBar({ mode, subtitle, scanning = false }) {
  const seg = (label, active) => (
    <div style={{
      flex: 1, textAlign: 'center', padding: '7px 0', borderRadius: 8,
      fontSize: 13.5, fontWeight: 600, letterSpacing: '0.01em',
      background: active ? 'var(--accent)' : 'transparent',
      color: active ? 'var(--accent-fg)' : 'var(--text-2)',
    }}>{label}</div>
  );
  // subtitle is "Sorting · 42 tracks" / "Queue · 12 tracks"
  const parts = String(subtitle).split('·');
  const head = parts[0].trim();
  const rest = parts.slice(1).join('·').trim();
  const m = rest.match(/^([\d,]+)\s*(.*)$/);
  const num = m ? m[1] : rest;
  const unit = m ? m[2] : '';
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '12px 14px 12px 16px', flexShrink: 0,
      borderBottom: '1px solid var(--border)', background: 'var(--surface)',
    }}>
      <div style={{
        display: 'flex', width: 122, padding: 3, borderRadius: 11,
        background: 'var(--surface-3)', gap: 3, flexShrink: 0,
      }}>
        {seg('Sort', mode === 'sort')}
        {seg('Play', mode === 'play')}
      </div>
      {/* active-list size — count on a single line (PRD §4.1) */}
      <div style={{ minWidth: 0, flex: 1, display: 'flex', alignItems: 'baseline', gap: 5 }}>
        <span style={{ fontSize: 20, fontWeight: 800, color: 'var(--text)', letterSpacing: '-0.02em' }}>{num}</span>
        <span style={{
          fontSize: 13, fontWeight: 600, color: 'var(--text-3)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        }}>{unit}</span>
      </div>
      {/* sync (scan) indicator + settings gear (PRD §4.1) */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 2, flexShrink: 0 }}>
        {scanning && (
          <div style={{
            width: 36, height: 36, borderRadius: '50%', display: 'flex',
            alignItems: 'center', justifyContent: 'center', color: 'var(--text-3)',
          }}>
            <span className="wr-spin" style={{ display: 'flex' }}><Icon name="reset" size={19} sw={2.2} /></span>
          </div>
        )}
        <div style={{
          width: 36, height: 36, borderRadius: '50%', display: 'flex',
          alignItems: 'center', justifyContent: 'center', color: 'var(--text-2)',
        }}>
          <Icon name="gear" size={21} sw={1.8} />
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Album art placeholder — minimal generated cover (no slop gradients)
// ─────────────────────────────────────────────────────────────
function Cover({ seedHue = 256, size = '100%', radius = 16, mark = 'block' }) {
  return (
    <div style={{
      width: size, aspectRatio: '1 / 1', borderRadius: radius, position: 'relative',
      overflow: 'hidden', flexShrink: 0,
      background: `var(--cover-bg)`,
      border: '1px solid var(--border)',
    }}>
      <div style={{
        position: 'absolute', inset: 0,
        background: `linear-gradient(150deg, oklch(var(--cover-l1) 0.05 ${seedHue}) 0%, oklch(var(--cover-l2) 0.04 ${seedHue}) 100%)`,
      }} />
      {mark === 'block' && (
        <div style={{
          position: 'absolute', left: '50%', top: '50%', transform: 'translate(-50%,-50%)',
          width: '34%', height: '34%', borderRadius: 6,
          background: `oklch(var(--cover-mark) 0.10 ${seedHue})`,
        }} />
      )}
      {mark === 'ring' && (
        <div style={{
          position: 'absolute', left: '50%', top: '50%', transform: 'translate(-50%,-50%)',
          width: '40%', height: '40%', borderRadius: '50%',
          border: `2.5px solid oklch(var(--cover-mark) 0.10 ${seedHue})`,
        }} />
      )}
      {mark === 'bar' && (
        <div style={{
          position: 'absolute', left: '18%', right: '18%', top: '50%', transform: 'translateY(-50%)',
          height: '6%', borderRadius: 4,
          background: `oklch(var(--cover-mark) 0.10 ${seedHue})`,
        }} />
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Sample data
// ─────────────────────────────────────────────────────────────
const SORT_TRACK = {
  title: 'Tessellate',
  artist: 'Halcyon Bench',
  album: 'Soft Machinery',
  pos: 78, dur: 214, // seconds
  hue: 256, mark: 'ring',
};

// Queue Editor facets — tag, count
const FACETS = {
  genre:  [['Rock', 47], ['Electronic', 31], ['Ambient', 24], ['Jazz', 12], ['Hip-Hop', 9], ['Folk', 6]],
  mood:   [['Hype', 22], ['Chill', 18], ['Focus', 11], ['Melancholy', 8], ['Dark', 5]],
  pace:   [['Fast', 28], ['Medium', 19], ['Slow', 4]],
  labels: [['gym', 16], ['commute', 13], ['late-night', 9], ['deep-work', 7]],
  artist: [['Halcyon Bench', 34], ['Kō Mirin', 21], ['Vetiver Theory', 14], ['Sable Unit', 8]],
  album:  [['Soft Machinery', 12], ['Long Exposure', 8], ['Null Island', 5]],
};

function fmtTime(s) {
  const m = Math.floor(s / 60), sec = Math.floor(s % 60);
  return `${m}:${String(sec).padStart(2, '0')}`;
}

Object.assign(window, {
  DIMS, chipStyle, dimDot, Icon, TopBar, Cover, SORT_TRACK, FACETS, fmtTime,
});
