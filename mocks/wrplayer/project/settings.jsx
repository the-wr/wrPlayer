// settings.jsx — Settings → Watched Folders screen (PRD §3, §8).
// SAF watched folders list, add-folder (folder picker), manual rescan, scan status.

function FolderRow({ name, path, kind, unavailable = false }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 13, padding: '13px 14px',
      borderRadius: 14, background: 'var(--surface-2)', border: '1px solid var(--border)'
    }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 14.5, fontWeight: 700, color: 'var(--text)', display: 'flex', alignItems: 'center', gap: 7 }}>
          {name}
          <span style={{
            fontSize: 10, fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase',
            padding: '2px 6px', borderRadius: 5, color: 'var(--text-3)', background: 'var(--surface-3)'
          }}>{kind === 'sd' ? 'SD Card' : 'Internal'}</span>
        </div>
        <div style={{
          fontSize: 12, color: 'var(--text-3)', marginTop: 3, fontFamily: 'var(--mono-font)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'
        }}>{path}</div>
        {unavailable &&
        <div style={{ fontSize: 11.5, fontWeight: 600, color: `oklch(0.55 0.16 24)`, marginTop: 3 }}>Unavailable — card unmounted</div>}
      </div>
      <div style={{ color: 'var(--text-3)', display: 'flex', flexShrink: 0 }}><Icon name="close" size={17} sw={2.2} /></div>
    </div>);

}

function Settings({ scheme, dark }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--surface)' }}>
      {/* header with back chevron */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 12, padding: '14px 16px 12px',
        flexShrink: 0, borderBottom: '1px solid var(--border)'
      }}>
        <div style={{ color: 'var(--text)', display: 'flex' }}><Icon name="chevRight" size={22} sw={2.2} /></div>
        <span style={{ fontSize: 18, fontWeight: 800, color: 'var(--text)', letterSpacing: '-0.02em' }}>Settings</span>
      </div>

      <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '18px 18px 0' }}>
        {/* section header */}
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
          <span style={{
            fontSize: 11.5, fontWeight: 700, letterSpacing: '0.07em', textTransform: 'uppercase', color: 'var(--text-2)'
          }}>Watched Folders</span>
          <span style={{ fontFamily: 'var(--mono-font)', fontSize: 11.5, color: 'var(--text-3)' }}>2 active</span>
        </div>
        <div style={{ fontSize: 12.5, color: 'var(--text-3)', marginBottom: 14, lineHeight: 1.4 }}>
          New files in these folders appear in the inbox. Library/ is created on first promotion.
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 9 }}>
          <FolderRow name="Music" kind="internal" path="/storage/emulated/0/Music" />
          <FolderRow name="Downloads" kind="internal" path="/storage/emulated/0/Download/new" />
          <FolderRow name="SD · Archive" kind="sd" path="/storage/0BFA-1C3E/Beats" unavailable />
        </div>

        {/* add folder */}
        <div style={{
          marginTop: 11, height: 50, borderRadius: 14, display: 'flex', alignItems: 'center',
          justifyContent: 'center', gap: 9, border: '1.5px dashed var(--border)',
          color: 'var(--accent)', fontSize: 14.5, fontWeight: 700
        }}>
          <Icon name="plus" size={19} sw={2.4} />Add folder
        </div>
        <div style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 8, lineHeight: 1.4, textAlign: 'center' }}>
          Opens the system folder picker (SAF) to grant read &amp; write access.
        </div>

        {/* scan — big CTA button */}
        <div style={{ height: 1, background: 'var(--border)', margin: '20px 0 16px' }} />
        <div style={{
          height: 52, borderRadius: 14, display: 'flex', alignItems: 'center', justifyContent: 'center',
          gap: 9, background: 'var(--accent)', color: 'var(--accent-fg)', fontSize: 15, fontWeight: 700,
          boxShadow: '0 12px 28px -12px var(--accent)'
        }} data-comment-anchor="894295a085-div-83-9">
          <Icon name="reset" size={19} sw={2.2} />Rescan now
        </div>
        <div style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 8, lineHeight: 1.4, textAlign: 'center' }}>
          Last scanned 6 min ago · 1,331 tracks cached
        </div>
      </div>
    </div>);

}

Object.assign(window, { Settings });