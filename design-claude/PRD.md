# wrPlayer — Product Requirements Document

**Version:** 0.1 (MVP)
**Platform:** Android
**Audio formats:** MP3 only (MVP)
**Target:** Personal use → Play Store release

---

## 1. Overview

wrPlayer is a local music player designed for large, actively growing MP3 collections. It combines standard playback with two specialized workflows: a **Sort Mode** for triaging newly added tracks, and a **Play Mode** with tag-based queue building. All metadata is stored as embedded ID3 tags so it remains portable across players and platforms.

---

## 2. Core Concepts

### 2.1 Track States

Every track lives in one of three states, stored as a custom ID3 frame:

| State | `TXXX:STATUS` value | Meaning |
|---|---|---|
| Inbox | `inbox` | Newly discovered, not yet evaluated |
| Library | `library` | Kept and tagged; available in Play Mode |
| Deleted | *(file removed)* | Permanently deleted from storage |

### 2.2 Sort Score

Each track in the inbox accumulates a score persisted to the **app database** in the `sort_score` column (§10.3) — never written to ID3. Because it is persisted, a track's score survives app restarts and accumulates across sessions until it reaches a threshold.

- User taps **+1**: score += 1
- User taps **−1**: score −= 1
- A **+1 vote that brings the score to +2 or higher**: tag sheet opens (the track stays in the inbox until tags are confirmed — see §5.3). If the sheet is dismissed, the track stays at its current score; merely re-entering Sort Mode later does **not** reopen it — the sheet fires again only on the **next +1 vote** (e.g. +2 → +3). There is no upper cap on the score.
- Score reaches **−2**: track is **immediately and permanently deleted** (no confirmation dialog). Because deletion happens at exactly −2, the score never goes below −2.

**Session:** A "session" for a track is a single occasion on which that track is given a score — i.e. the span from when the track begins playing in Sort Mode until it advances to the next track. Each session contributes at most a net ±1 to the persisted score, so reaching ±2 requires votes across two separate sessions. A **+1** vote stays changeable while the track plays (it can be deselected, or replaced by a −1) but does not stack; a **−1** vote advances immediately and is final (see §5.2).

A +1 vote is changeable until the track is left; a −1 vote is final. No other undo is provided.

### 2.3 Tag Schema (ID3v2.4)

| Concept | ID3 Field | Type | Notes |
|---|---|---|---|
| Title | `TIT2` | Standard | |
| Artist | `TPE1` | Standard | |
| Album | `TALB` | Standard | |
| Genre | `TCON` | Standard | Predefined list + freeform |
| BPM | `TBPM` | Standard | Detected on first play; stored in DB until promotion, then written to file; user-overridable |
| Mood | `TMOO` | Standard (v2.4) | Predefined list + freeform |
| Pace bucket | `TXXX:PACE` | Custom | `slow` / `medium` / `fast`; derived from BPM, overridable |
| Custom labels | `TXXX:LABELS` | Custom | Multiple values stored as native ID3v2.4 null-separated list |
| Track status | `TXXX:STATUS` | Custom | `inbox` / `library` |

Artist and Album are exposed as tag dimensions in the Queue Editor (read from standard fields, not duplicated).

---

## 3. Navigation Structure

```
App
├── Mode Selector  (persistent — tab bar or prominent toggle on all main screens)
│
├── Sort Mode  (shared Now Playing UI + sort action panel)
│   └── Tag Sheet  (modal — fires when score hits +2)
│
├── Play Mode  (shared Now Playing UI + play action panel)
│   ├── Queue Editor
│   └── Current Queue
│
└── Settings
    └── Watched Folders
```

Queue Editor and Current Queue are both accessible directly from the Play Mode screen (two separate buttons in the action panel).

**First launch / no watched folders:** If no watched folder is configured, the app opens directly on the Settings → Watched Folders screen so the user can pick one before anything else. The same applies if all folders are later removed.

---

## 4. Shared Now Playing UI

Both Sort Mode and Play Mode use the same Now Playing screen layout. Only the **bottom action panel** differs between modes. The score is never shown in Sort Mode to avoid biasing votes.

### 4.1 Shared Layout

**Persistent top bar (above all content, both modes):**
```
[Sort | Play]   Sorting: 42 tracks            ⟳  ⚙   ← Sort Mode
[Sort | Play]   Queue: 12 tracks              ⟳  ⚙   ← Play Mode
```
Mode toggle is a segmented button in the top-left. To its right, a title line shows the size of the active list for the current mode (inbox count in Sort Mode, queue track count in Play Mode). A **Settings button (gear icon ⚙) sits in the top-right corner** and opens the Settings screen (§3) from any main screen — this is the entry point to Watched Folders and manual rescan during normal use. This bar is always visible and always shows the current mode.

**Scan progress indicator:** While a background reconciliation walk (§8.2) is running, a small spinner (⟳) appears in the top-right corner of this bar, immediately to the left of the Settings gear. It disappears when the walk completes. This is the only surfaced indication of scan activity; the rest of the UI stays fully usable during the walk.

**Mode transition behavior:**

*Switching to Sort Mode:*
Always shows a Sort Order picker (bottom sheet) before entering the mode. The picker pre-selects the last used order. Options: Newest First / Random / Closest to Threshold. Confirming the order begins playback of the first inbox track. If the inbox is empty, the mode is entered with nothing playing and the empty-inbox state is shown (see §5.4). Dismissing the sheet cancels the mode switch and returns to Play Mode.

*Switching to Play Mode:*
- Queue has tracks (the persisted queue from §6.3 counts, even if currently paused/stopped) → go directly to Now Playing, restored at the saved track and position
- Queue is empty → go directly to Queue Editor

**Main content (both modes):**
- Album art (embedded; placeholder if absent)
- Title, Artist, Album
- Playback progress bar (scrubable)
- Current position / total duration
- Play / Pause, Previous, Next controls

**Bottom action panel — Sort Mode:**
```
[ −1 ]          [ Skip ]          [ +1 ]
```
Large, thumb-reachable buttons spanning the full width.

**Bottom action panel — Play Mode:**
```
Tags: [ Rock ]  [ Hype ]  [ Fast ]  [ gym ]   [ Edit ··· ]
[ Queue Editor ]                    [ Current Queue ]
```
Track's tags shown as read-only color-coded chips (one color per tag dimension — see §6.2). "Edit" (overflow or long-press) opens the Tag Sheet. Queue Editor and Current Queue buttons navigate to their respective screens.

---

## 5. Sort Mode

### 5.1 Feed

The inbox feed presents tracks not yet in the Library. Feed order is chosen in the Sort Order picker that appears on every entry into Sort Mode (see §4.1). The last chosen order is remembered and pre-selected next time.

- **Newest first** — ordered by file modification date descending
- **Random** — random shuffle of all inbox tracks
- **Closest to threshold** — tracks ordered by `abs(score)` descending, so tracks nearest either threshold (+2 or −2) come first; negative scores are included. Ties are ordered arbitrarily. (Helps clear the inbox faster.)

**Static queue per entry:** The sort queue is built once, when the user enters Sort Mode, from the current inbox in the chosen order. It is **static** for the duration of that entry — it only advances forward (skipped or voted tracks are not reinserted) and is not otherwise reordered, even if scores change mid-session. The top-bar counter decrements as the user progresses through it. A skipped track therefore reappears only on the **next** entry into Sort Mode, when a fresh queue is built. (The Play Mode queue follows the same principle — built on demand, advances forward only — see §6.2.)

### 5.2 Voting Behavior

- **−1 tap** → applies −1 and advances immediately to next track; **final** (no window to change it)
- **+1 tap** → marks the +1 button "selected", plays to end of track, then advances
- **Skip** → advances immediately, score unchanged
- **Track ends with no vote** → advances automatically

**A +1 vote is changeable until the track is left:** After a +1 tap the button is "selected" (visually marked) and the track keeps playing. Until the track advances the user can:
- Tap **+1** again to deselect it (the +1 is undone; the score returns to what it was at the start of this playback).
- Tap **−1** to replace it — this casts a −1, which (like any −1) is final and advances immediately.

A −1 is never changeable, because it advances immediately. The net effect is that a single playback session contributes at most ±1 to the score. The selected state resets when the track is revisited in a later session. (This is why reaching −2 requires a −1 vote in each of two separate sessions — see §2.2.)

**Score commit timing:** The session's score change is committed to the DB (`sort_score`, §10.3) when the track is left (i.e. on advance). A −1 commits as the track advances; a +1 commits its final selected/deselected state at advance.

**Crossing +2 while playing:** A +1 tap that brings the score to +2 or higher opens the Tag Sheet immediately — it does not wait for the track to finish. While open, the sheet **covers the bottom action panel**, so the vote for the underlying track cannot be changed while it is up (this removes any ambiguity about voting against an open sheet). The Tag Sheet is bound to the track it was opened for; if playback advances to the next track while the sheet is still open, confirming the sheet applies tags to the **original** track, not the one now playing. Dismissing the sheet without confirming leaves the original track in the inbox at its current score (see §5.3).

Score is never displayed on screen. The user votes by feel, not by watching a counter.

### 5.3 Tag Sheet (on score reaching +2)

Fires as a modal bottom sheet the moment a +1 vote brings the score to +2 or higher. The track is **not** moved to Library at this point — it stays in the inbox until the sheet is confirmed (see Confirm action below). If the sheet is dismissed without confirming, the track remains in the inbox at its current score and will be triaged again on a later pass; the sheet does not auto-reopen on later entries — only the **next +1 vote** reopens it (see §2.2).

**BPM detection:** Runs in the background on first play of the track (see §9). By the time a track reaches +2 it has usually been played at least twice, so BPM should already be in the DB when the tag sheet opens. If detection has not finished by the time the sheet opens, BPM and Pace are simply left blank (no spinner, no waiting); the user can fill them in here or later via tag editing.

**Track details — editable text fields (top of sheet):**
- **Title** — text field, pre-filled from `TIT2`
- **Artist** — text field, pre-filled from `TPE1`
- **Album** — text field, pre-filled from `TALB`

**Filename pre-fill:** If the file has no `TIT2` / `TPE1` tags, the Title and Artist fields are pre-filled by parsing the filename. The extension is stripped first, then the remaining name is matched against an ordered list of known patterns; the first pattern that matches supplies the extracted fields. Examples:
- `$artist - $track_name`
- `$artist/$album/$number - $track_name` (using parent folders)
- `$number - $track_name`

If no pattern matches, the whole filename (extension stripped) is placed in Title and Artist is left blank. The user can edit any field before confirming; the edited values are written to the standard ID3 fields on confirm.

**Tag fields — all shown as chip groups, each chip individually removable:**
- **Genre** — predefined chips + "＋ Add" freeform input (multi-select)
- **Mood** — predefined chips + "＋ Add" freeform input (multi-select)
- **Pace** — `Slow` / `Medium` / `Fast` chips (single-select); auto-set from BPM, tappable to change
- **BPM** — numeric value shown as a chip; tap to edit manually
- **Labels** — freeform chips; tap "＋ Add" to enter text

**Freeform tag input rule:** Genre, Mood, and Label values accept any printable character — digits, hyphens, ampersands (e.g. `Hip-Hop`, `R&B`) are all permitted. Multiple values within a dimension are stored as a **native ID3v2.4 null-separated (`0x00`) list** in the file, which is the spec's own multi-value mechanism and is read correctly by other players (preserving portability — §1). The null byte cannot occur in user input, so there is no delimiter-collision risk and no character needs to be disallowed.

**Pre-fill logic:** If tracks by the same artist or on the same album already exist in the Library, their Genre, Mood, Pace, and Labels chips are pre-selected. When existing tracks disagree on a value, the **most common** value wins: for the single-select Pace, the most common bucket; for the multi-value Genre / Mood / Labels, each value held by the plurality of the matching tracks is pre-selected. All pre-filled chips are removable — there is no separate accept/reject step.

**Required fields for Confirm:** Confirm is disabled until the track has **Title**, **Artist**, and **at least one** other tag from Genre / Mood / Pace / Labels. A **Pace auto-derived from a detected BPM counts** toward this requirement (it makes the track reachable via the Pace filter), so a track with a detected BPM may satisfy Confirm without the user picking any tag manually. (Requiring one descriptive tag guarantees the track is reachable in the Queue Editor; without it a promoted track could match no filter.) BPM and Album are optional.

**Confirm action:** Writes all currently selected tags **and the Title / Artist / Album text fields** to the MP3 file, sets `TXXX:STATUS=library`, moves the file to the Library folder if it is not already there (see §8.1), removes track from the inbox feed. Tags are always applied to the track the sheet was opened for, even if playback has since advanced. Until Confirm is pressed the track stays in the inbox.

### 5.4 Empty Inbox State

When the inbox is empty, Sort Mode shows a prompt to rescan or a confirmation that everything has been sorted.

---

## 6. Play Mode

### 6.1 Now Playing Screen

Uses the shared Now Playing layout (§4.1) with the Play Mode bottom action panel. Tag chips reflect the track's current embedded tags. Playback behavior:

- Queue ends → playback stops
- Headphone disconnect → playback pauses
- Audio focus lost (call, other app) → playback pauses
- Android media notification with transport controls
- Lock screen media session

### 6.2 Queue Editor Screen

**Purpose:** Build a set of tracks by tag combination and add them to the queue.

**State persistence:** The chip selection state (included, excluded, unselected) is retained in memory for the lifetime of the app session. Navigating away to Now Playing, switching to Sort Mode and back, or opening the Current Queue screen does not reset the editor. On app restart the state resets to empty. A **Reset** button in the top-right corner clears all chip selections immediately.

**Layout:**

```
[ Saved Presets: "morning run"  "gym"  "late night"  +New ]   [ Reset ]

Active filters: [ Genre: Rock × ]  [ Mood: Hype × ]  [ − Slow × ]

──── Genre ────────────────────────────────────────
[ Rock (47) ]  [ Electronic (31) ]  [ Jazz (12) ]  ...

──── Mood ─────────────────────────────────────────
[ Hype (22) ]  [ Chill (18) ]  [ Focus (11) ]  ...

──── Pace ─────────────────────────────────────────
[ Fast (28) ]  [ Medium (19) ]  [ Slow (4) ]

──── Labels ───────────────────────────────────────
[ gym (15) ]  [ commute (9) ]  [ late-night (6) ]  ...

──── Artist ───────────────────────────────────────
[ Artist A (34) ]  [ Artist B (21) ]  ...

──── Album ────────────────────────────────────────
[ Album X (12) ]  [ Album Y (8) ]  ...

         [ Shuffle & Play ]  [ Add to Queue ]  [ Play Next ]
                              58 tracks match
```

**Tag dimensions and color coding:**

| Dimension | Color | Source |
|---|---|---|
| Genre | Blue | `TCON` |
| Mood | Purple | `TMOO` |
| Pace | Green | `TXXX:PACE` |
| Labels | Orange | `TXXX:LABELS` |
| Artist | Teal | `TPE1` |
| Album | Amber | `TALB` |

Colors are applied consistently across the whole app: tag sheet chips, now-playing tag display, queue editor chips, and active filter chips all use the same color per dimension.

**Chip states — three-state cycle (tap to advance):**
1. **Unselected** — neutral/gray, no effect on filter
2. **Included** (first tap) — colored, tracks must have this tag (OR with others in same dimension)
3. **Excluded** (second tap) — red with strikethrough, tracks with this tag are removed from results regardless of other filters
4. **Back to unselected** (third tap)

**Active filters row** at the top shows included chips (colored, × to remove) and excluded chips (red with −, × to remove) for quick overview and removal.

**Filter logic:**
- Within a dimension: included chips combine as **OR**; excluded chips are always **AND NOT**
- Across dimensions: included sets combine as **AND** (e.g. (Rock OR Jazz) AND Hype AND NOT Slow)
- Tags within each dimension are sorted by matching track count, descending
- Tags that would produce zero **included** results are hidden — **except** a tag the user has actively set to excluded, which always stays visible so the exclusion can be removed
- Track count on each chip reflects the count if that chip is toggled to included given the current selection. A chip currently in the **excluded** state shows that same prospective included-count (not its exclusion effect)

**Text filter:** A search/filter input at the top narrows visible chips by name.

**Saved presets:**
- A horizontal scrollable row of named presets at the top
- Tapping a preset loads its saved chip state instantly
- "Save current as preset" action available when filters are active
- Presets can be renamed or deleted via long-press
- **Stale chips:** If a preset references a tag value that no longer matches any track (e.g. a label that has since disappeared from the collection), the chip still loads but shows a count of zero, and a toast/dialog calls out that one or more saved filters no longer match anything.

**CTAs:**
- **Shuffle & Play** — replaces current queue with a shuffled set of matching tracks and starts playback
- **Add to Queue** — appends matching tracks (shuffled) to current queue
- **Play Next** — inserts matching tracks (shuffled) after the currently playing track

### 6.3 Current Queue Screen

**Displayed:** Ordered list of queued tracks (title, artist, album art thumbnail)

**Interactions:**
- Drag handle to reorder
- Swipe to remove
- Tap to jump to that track
- Currently playing track highlighted

**Persistence:** The current queue (and the playback position within it) persists across app restarts. There is no explicit "clear queue" action — **Shuffle & Play** from the Queue Editor replaces the entire queue each time it is used. An empty queue simply means nothing plays (the same way an empty inbox means Sort Mode plays nothing).

---

## 7. Tag Editing (Post-Library)

The same Tag Sheet used in Sort Mode is reachable from Play Mode for any Library track (via overflow menu on Now Playing, or via a track detail view). Saving writes tags back to the MP3 file immediately.

**Files are never renamed or moved by tag edits.** Changing Title / Artist / Album updates the ID3 fields only; the file keeps its existing path and stays where it is (already in `Library/`). File reorganization based on tags may be introduced later (out of scope for MVP).

---

## 8. File Discovery & Storage Layout

### 8.1 Folder Structure

```
<watched folder>/
├── <any structure>   ← new files placed here by the user; treated as inbox
└── Library/          ← app moves files here on +2 promotion
```

A file's inbox/library state is determined by its `TXXX:STATUS` tag, **not** by which folder it sits in (see §8.2). The `Library/` folder is a convenience destination for promoted files, not a classification mechanism.

On promotion, if the file is **not already under** the watched folder's `Library/` directory, it is moved there — to a flat `Library/` directory at the root of the same watched folder, regardless of its original subfolder nesting. The Library folder is created if it does not exist. If the file is **already under** `Library/`, it stays in place; only its tags are written. (This means re-tagging an existing library track never moves it.)

Example: `Music/new/artist/track.mp3` → `Music/Library/track.mp3`

If two inbox files share the same filename, the second promotion appends a numeric suffix (`track_2.mp3`).

### 8.2 Discovery

- **On app open (background reconciliation walk):** The app renders immediately from the DB's cached state — startup is never blocked on the filesystem. A full walk of all configured watched folders then runs **asynchronously** (background coroutine / WorkManager) and reconciles the DB as it goes: inserting newly found files, updating rows whose `file_mtime` changed, and removing rows for files that no longer exist on disk. UI counts (inbox size, faceted counts) update live as the walk progresses.
- **Classification:** A newly found MP3's state is classified **by the `TXXX:STATUS` tag only**: `library` if the tag is present and equals `library`, otherwise `inbox` with score 0. Folder location is never used for classification — a file sitting in `Library/` without the tag is treated as inbox, and a tagged file outside `Library/` is treated as library. This ensures already-sorted files (e.g. after a reinstall or DB wipe) are not re-triaged purely on the strength of the embedded tag, keeping the tag as the single source of truth.
- **Scan cost:** Files already in the DB are matched by `file_path` and skipped unless `file_mtime` changed, so a steady-state walk only pays for the directory enumeration plus ID3 reads of genuinely new files. The directory enumeration (especially over SAF / `DocumentFile`) is the dominant cost, which is why the walk runs off the main thread.
- **Manual rescan:** Button in Settings; triggers the same background reconciliation walk.
- **Live detection:** Out of scope for MVP — files added while the app is open are picked up on the next open or manual rescan, not in real time. (`FileObserver`-based live detection may be added later.)

### 8.3 SD Card Support and File Deletion

Android imposes restrictions on SD card write access that vary by OS version:

- **Android ≤ 9:** `WRITE_EXTERNAL_STORAGE` generally permits SD card writes and deletes.
- **Android 10–12:** Scoped storage limits arbitrary SD card access. SAF (Storage Access Framework) is required — the user picks the SD card folder via the system folder picker, granting the app a persistent `Uri` with read/write/delete rights to that tree.
- **Android 13+:** Same SAF requirement. `READ_MEDIA_AUDIO` is needed for playback; SAF grants delete rights.

**Consequence for Settings:** When the user adds a watched folder, the app uses the SAF folder picker for all folders (internal and SD card) to obtain a persistent `DocumentFile` URI. This URI grants delete permission within the tree, which is required for the −2 deletion path and for moving files on promotion. `DocumentFile.delete()` works reliably on SD card via SAF.

`MANAGE_EXTERNAL_STORAGE` is explicitly avoided — it requires Play Store policy justification and is unnecessary when SAF covers all needed operations.

**Document URIs as identity:** Each track is keyed in the DB by its persisted SAF document URI (§10.3). These URIs are stable for the lifetime of the file at a given path. **Promotion moves a file** (§8.1), which changes its document URI (external-storage document IDs are path-derived); the DB row must therefore be re-keyed to the new URI as part of the move, not left pointing at the old one.

### 8.4 Permissions

| Permission | When required |
|---|---|
| `READ_MEDIA_AUDIO` | Android 13+, for MediaStore playback |
| `READ_EXTERNAL_STORAGE` | Android ≤ 12, for playback |
| SAF persistent URI | All versions, for write/delete in watched folders |

---

## 9. BPM Detection

- Triggered on **first play** of a track (in Sort Mode or otherwise)
- Runs on a background thread concurrently with playback; does not block or interrupt the listening experience
- Result is stored in the **app DB** (`bpm_detected` column) alongside the sort score — not written to ID3 yet, since the track may still be deleted
- On promotion to Library (tag sheet confirmed), the cached BPM is written to the `TBPM` ID3 field and `TXXX:PACE` is derived and written at the same time. If detection has not finished by the time the sheet is confirmed, BPM/Pace are left blank and can be filled in later via tag editing — the result is not written retroactively
- Pace bucket derivation: `slow` < 90 BPM, `medium` 90–140, `fast` > 140 (thresholds TBD)
- Both values are shown as editable chips in the Tag Sheet; user can override either before confirming
- If detection fails, fields are left empty; user sets manually via tag editing

---

## 10. App Database (Room)

### 10.1 Role

ID3 tags are the **source of truth** for all metadata — they travel with the file and are readable by any player. The DB is a **queryable cache** of that data plus app-only state that has no ID3 equivalent (sort score, queue presets). All UI queries — including the faceted counts in Queue Editor — run against the DB only. Reading ID3 from disk at query time is not acceptable for large collections. Per-tag faceted counts and the prospective-count logic (§6.2) run against the normalized `track_tags` index (§10.3), not the null-separated blob columns in `tracks`.

### 10.2 Sync Strategy

| Event | Action |
|---|---|
| File discovered on scan | Read ID3 → insert row into `tracks` |
| App writes tags (Tag Sheet confirm) | Write ID3 → update DB row atomically |
| App deletes a file | Delete file → remove DB row |
| App open / manual rescan | Background reconciliation walk; insert missing, update changed (by mtime), remove deleted |

On any insert/update/delete of a `tracks` row, its `track_tags` rows are rebuilt (or removed) in the **same transaction**, so the index never drifts from the cache.

### 10.3 Schema

**`tracks` table**

| Column | Source | Notes |
|---|---|---|
| `document_uri` | SAF | **Primary key.** The persisted SAF `DocumentFile` URI. Stable across the app lifecycle and device reboots while the persisted permission is held and the file is not moved/renamed (see §8.3). |
| `file_path` | disk | Human-readable path, stored for clarity/display and pattern-based filename parsing; not the key. |
| `status` | `TXXX:STATUS` | `inbox` / `library`; from the `TXXX:STATUS` tag only — folder location is not used (see §8.2). |
| `sort_score` | app only | Not written to ID3 |
| `bpm_detected` | app only | Calculated on first play; written to `TBPM` on promotion |
| `title` | `TIT2` | |
| `artist` | `TPE1` | |
| `album` | `TALB` | |
| `genre` | `TCON` | Multi-value; in the file stored as native ID3v2.4 null-separated list. In the DB cache stored null-separated (`0x00`) for round-trip fidelity (see §5.3) |
| `mood` | `TMOO` | Multi-value; null-separated in file and DB cache |
| `pace` | `TXXX:PACE` | `slow` / `medium` / `fast` |
| `bpm` | `TBPM` | Integer |
| `labels` | `TXXX:LABELS` | Multi-value; null-separated in file and DB cache |
| `date_added` | app | Timestamp of first DB insert |
| `file_mtime` | disk | Used to detect external changes |
| `has_art` | ID3 `APIC` presence | Boolean; art itself is not cached in DB |

**`presets` table**

| Column | Notes |
|---|---|
| `name` | **Primary key.** User-assigned preset name; must be unique. Saving under an existing name overwrites that preset. |
| `filter_state` | Serialized tag filter (JSON) |

**`track_tags` table** (normalized index for faceted queries)

A derived, denormalized index that explodes the multi-value tag columns into one row per `(track, dimension, value)`. It is **not** a second source of truth — it is rebuilt from the owning `tracks` row whenever that row's tags are inserted or updated, inside the **same transaction**. It exists because the faceted counts and prospective-count logic in the Queue Editor (§6.2) cannot be computed efficiently from the null-separated blob columns in `tracks`.

| Column | Notes |
|---|---|
| `document_uri` | FK → `tracks.document_uri` (cascade delete) |
| `dimension` | `genre` / `mood` / `pace` / `labels` / `artist` / `album` |
| `value` | A single tag value (one of the null-separated entries, or the single Pace / Artist / Album value) |

Indexed on `(dimension, value)` for fast `GROUP BY` counts and on `document_uri` for rebuild/delete. Only **library** tracks need rows here (inbox tracks are never queried by tag), which keeps it small.

---

## 11. Predefined Tag Lists (MVP)

### Genres (initial set, user can add freeform)
Electronic, Rock, Hip-Hop, Jazz, Classical, Ambient, Folk, Metal, R&B, Pop, Funk, Soul, Reggae, Latin, World

### Moods (initial set, user can add freeform)
Hype, Energetic, Happy, Melancholy, Chill, Focus, Dark, Romantic, Nostalgic, Aggressive

### Pace buckets (fixed, derived from BPM)
Slow, Medium, Fast

---

## 12. Out of Scope (MVP)

- Audio formats other than MP3 (FLAC, OGG, M4A — future)
- Equalizer / audio effects
- Crossfade / gapless playback
- Classic Artist / Album / All Tracks browser
- Scrobbling (Last.fm etc.)
- Cloud sync or backup
- Widgets
- Configurable score thresholds
- Tag batch-editing across multiple tracks
- Export / import of presets
- Desktop companion app

---

## 13. Technical Stack (Recommended)

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Playback | Media3 (ExoPlayer) |
| Database | Room |
| ID3 read/write | JAudioTagger |
| BPM detection | TarsosDSP or BeatDetector library (TBD) |
| Background work | WorkManager |
| DI | Hilt |
| Navigation | Compose Navigation |

---

## 14. Open Questions (Post-MVP)

- BPM detection library selection and accuracy threshold
- Pace bucket BPM boundaries (currently 90/140 — adjust after testing)
- What happens to DB records when a file is moved outside the app

**Duplicate files (MVP behavior):** Duplicate detection is out of scope. Two files with the same content at different paths are treated as entirely separate tracks (separate DB rows, separate states/scores). No de-duplication is attempted.

**Note (intended behavior):** Because classification is by `TXXX:STATUS` tag only (§8.2), a file physically located in `Library/` but missing the tag is treated as inbox. It can therefore be voted down to −2 and permanently deleted, even though it sits in the Library folder. There is no folder-based safety net — this is intentional.
