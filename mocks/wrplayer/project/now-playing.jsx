// now-playing.jsx — Shared Now Playing layout (PRD §4.1) used by Sort & Play modes.
// Only the bottom action panel differs between modes. Exports the shell plus the
// two mode screens (Sort Mode and Play Mode) and their action panels.

// ─────────────────────────────────────────────────────────────
// Scrubbable progress bar + position / duration (PRD §4.1)
// ─────────────────────────────────────────────────────────────
function ProgressBar({ pos, dur, accent = false }) {
  const pct = Math.max(0, Math.min(1, pos / dur)) * 100;
  const fill = accent ? 'var(--accent)' : 'var(--text)';
  return (
    <div style={{ width: '100%' }}>
      <div style={{ position: 'relative', height: 6, borderRadius: 3, background: 'var(--surface-3)' }}>
        <div style={{ position: 'absolute', inset: 0, width: pct + '%', borderRadius: 3, background: fill }} />
        <div style={{
          position: 'absolute', top: '50%', left: pct + '%', transform: 'translate(-50%,-50%)',
          width: 14, height: 14, borderRadius: '50%', background: fill,
          border: '2.5px solid var(--surface)', boxShadow: '0 1px 4px rgba(0,0,0,0.18)',
        }} />
      </div>
      <div style={{
        display: 'flex', justifyContent: 'space-between', marginTop: 9,
        fontFamily: 'var(--mono-font)', fontSize: 11.5, fontWeight: 500, color: 'var(--text-3)',
      }}>
        <span>{fmtTime(pos)}</span>
        <span>{fmtTime(dur)}</span>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Transport controls — Play/Pause center; Prev hidden in Sort Mode;
// in Sort Mode Next acts as Skip (PRD §4.1)
// ─────────────────────────────────────────────────────────────
function Transport({ showPrev = true, playing = true }) {
  const sideBtn = (name) => (
    <div style={{
      width: 52, height: 52, borderRadius: '50%', display: 'flex', alignItems: 'center',
      justifyContent: 'center', color: 'var(--text)',
    }}>
      <Icon name={name} size={28} />
    </div>
  );
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 22 }}>
      {showPrev ? sideBtn('prev') : <div style={{ width: 52 }} />}
      <div style={{
        width: 72, height: 72, borderRadius: '50%', display: 'flex', alignItems: 'center',
        justifyContent: 'center', background: 'var(--accent)', color: 'var(--accent-fg)',
        boxShadow: '0 12px 30px -10px var(--accent)',
      }}>
        <Icon name={playing ? 'pause' : 'play'} size={32} />
      </div>
      {sideBtn('next')}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Shell — top bar, art, meta, scrub, transport, then a mode panel
// ─────────────────────────────────────────────────────────────
function NowPlaying({ mode, subtitle, track, showPrev, accentBar, panel, dim, scanning = false }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      <TopBar mode={mode} subtitle={subtitle} scanning={scanning} />

      {/* gear + scan area beneath the toggle is part of TopBar in spec; here we add a thin meta strip */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 16,
        padding: '0 18px', height: 0, overflow: 'visible',
      }} />

      {/* main content */}
      <div style={{
        flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column',
        padding: '22px 26px 0', alignItems: 'center',
      }}>
        <div style={{ width: '78%', marginTop: 8 }}>
          <Cover seedHue={track.hue} radius={20} mark={track.mark} />
        </div>

        <div style={{ width: '100%', marginTop: 26, textAlign: 'center' }}>
          <div style={{
            fontSize: 24, fontWeight: 800, color: 'var(--text)', letterSpacing: '-0.02em',
            overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          }}>{track.title}</div>
          <div style={{ fontSize: 15.5, fontWeight: 600, color: 'var(--text-2)', marginTop: 5 }}>{track.artist}</div>
          <div style={{ fontSize: 13.5, fontWeight: 500, color: 'var(--text-3)', marginTop: 2 }}>{track.album}</div>
        </div>

        <div style={{ width: '100%', marginTop: 26 }}>
          <ProgressBar pos={track.pos} dur={track.dur} accent={accentBar} />
        </div>

        <div style={{ width: '100%', marginTop: 22 }}>
          <Transport showPrev={showPrev} />
        </div>
      </div>

      {panel}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Sort Mode action panel — [ −1 ]  [ Skip ]  [ +1 ]  (PRD §4.1, §5.2)
// plusSelected: the +1 vote is "selected" and changeable until the track leaves
// ─────────────────────────────────────────────────────────────
function SortPanel({ plusSelected = false, dark }) {
  const NEG = 24, POS = 152; // red hue / green hue
  const negBg = dark ? `oklch(0.30 0.06 ${NEG})` : `oklch(0.96 0.035 ${NEG})`;
  const negFg = dark ? `oklch(0.82 0.13 ${NEG})` : `oklch(0.52 0.18 ${NEG})`;
  const posBg = dark ? `oklch(0.32 0.06 ${POS})` : `oklch(0.95 0.04 ${POS})`;
  const posFg = dark ? `oklch(0.82 0.12 ${POS})` : `oklch(0.46 0.13 ${POS})`;
  const posSelBg = dark ? `oklch(0.58 0.13 ${POS})` : `oklch(0.60 0.14 ${POS})`;

  const big = (extra) => ({
    flex: 1, height: 64, borderRadius: 18, display: 'flex', alignItems: 'center',
    justifyContent: 'center', border: '1.5px solid transparent', ...extra,
  });

  return (
    <div style={{
      flexShrink: 0, padding: '14px 16px 20px', background: 'var(--surface-2)',
      borderTop: '1px solid var(--border)',
    }}>
      <div style={{ display: 'flex', gap: 11, alignItems: 'stretch' }}>
        <div style={big({ background: negBg, color: negFg, borderColor: dark ? `oklch(0.42 0.08 ${NEG})` : `oklch(0.88 0.07 ${NEG})` })}>
          <Icon name="minus" size={32} sw={2.8} />
        </div>
        <div style={big({
          flex: 0.7, background: 'var(--surface)', color: 'var(--text-2)',
          borderColor: 'var(--border)',
        })}>
          <Icon name="skip" size={26} sw={2.2} />
        </div>
        <div style={big(plusSelected
          ? { background: posSelBg, color: dark ? `oklch(0.16 0.02 ${POS})` : '#fff', boxShadow: `0 10px 26px -10px ${posSelBg}` }
          : { background: posBg, color: posFg, borderColor: dark ? `oklch(0.44 0.08 ${POS})` : `oklch(0.86 0.07 ${POS})` })}>
          <Icon name="plus" size={32} sw={2.8} />
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Play Mode action panel — read-only tag chips + Edit, then nav buttons (PRD §4.1)
// ─────────────────────────────────────────────────────────────
function PlayPanel({ scheme, dark }) {
  const tags = [['genre', 'Rock'], ['mood', 'Hype'], ['pace', 'Fast'], ['labels', 'gym']];
  const navBtn = (icon, label) => (
    <div style={{
      flex: 1, height: 48, borderRadius: 14, display: 'flex', alignItems: 'center',
      justifyContent: 'center', gap: 8, background: 'var(--surface)',
      border: '1.5px solid var(--border)', color: 'var(--text)', fontSize: 14, fontWeight: 600,
    }}>
      <Icon name={icon} size={18} sw={2} />{label}
    </div>
  );
  return (
    <div style={{
      flexShrink: 0, padding: '14px 16px 20px', background: 'var(--surface-2)',
      borderTop: '1px solid var(--border)',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 7, marginBottom: 13, flexWrap: 'nowrap', overflow: 'hidden' }}>
        <div style={{ display: 'flex', gap: 7, flex: 1, minWidth: 0, overflow: 'hidden' }}>
          {tags.map(([dim, label]) => {
            const s = chipStyle(dim, 'on', scheme, dark);
            return <div key={label} style={{ ...s, padding: '6px 11px', fontSize: 12.5 }}>{label}</div>;
          })}
        </div>
        <div style={{
          flexShrink: 0, width: 40, height: 34, borderRadius: 10, display: 'flex',
          alignItems: 'center', justifyContent: 'center', background: 'var(--surface-3)',
          border: '1px solid var(--border)', color: 'var(--text-2)',
        }}>
          <Icon name="edit" size={17} sw={2} />
        </div>
      </div>
      <div style={{ display: 'flex', gap: 10 }}>
        {navBtn('sliders', 'Queue Editor')}
        {navBtn('list', 'Current Queue')}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// The two mode screens
// ─────────────────────────────────────────────────────────────
function SortNowPlaying({ scheme, dark, plusSelected = false }) {
  return (
    <NowPlaying
      mode="sort"
      subtitle="Sorting · 42 tracks"
      track={SORT_TRACK}
      showPrev={false}
      accentBar={false}
      dim={dark}
      scanning={true}
      panel={<SortPanel plusSelected={plusSelected} dark={dark} />}
    />
  );
}

function PlayNowPlaying({ scheme, dark }) {
  const t = { ...SORT_TRACK, pos: 96, dur: 248 };
  return (
    <NowPlaying
      mode="play"
      subtitle="Queue · 12 tracks"
      track={t}
      showPrev={true}
      accentBar={true}
      dim={dark}
      scanning={true}
      panel={<PlayPanel scheme={scheme} dark={dark} />}
    />
  );
}

Object.assign(window, {
  ProgressBar, Transport, NowPlaying, SortPanel, PlayPanel, SortNowPlaying, PlayNowPlaying,
});
