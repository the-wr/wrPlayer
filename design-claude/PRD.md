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

Each track in the inbox accumulates a score stored in the **app database** (not in ID3 tags):

- User taps **+1**: score += 1
- User taps **−1**: score −= 1
- Score reaches **+2**: track moves to Library → tag sheet opens
- Score reaches **−2**: track is **immediately and permanently deleted**

Scores are intentional single taps; no undo is provided.

### 2.3 Tag Schema (ID3v2.4)

| Concept | ID3 Field | Type | Notes |
|---|---|---|---|
| Title | `TIT2` | Standard | |
| Artist | `TPE1` | Standard | |
| Album | `TALB` | Standard | |
| Genre | `TCON` | Standard | Predefined list + freeform |
| BPM | `TBPM` | Standard | Auto-detected on promotion to library; user-overridable |
| Mood | `TMOO` | Standard (v2.4) | Predefined list + freeform |
| Pace bucket | `TXXX:PACE` | Custom | `slow` / `medium` / `fast`; derived from BPM, overridable |
| Custom labels | `TXXX:LABELS` | Custom | Comma-separated freeform strings |
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

---

## 4. Shared Now Playing UI

Both Sort Mode and Play Mode use the same Now Playing screen layout. Only the **bottom action panel** differs between modes. The score is never shown in Sort Mode to avoid biasing votes.

### 4.1 Shared Layout

**Persistent top bar (above all content, both modes):**
```
[Sort | Play]   Sorting: 42 tracks          ← Sort Mode
[Sort | Play]   Queue: 12 tracks            ← Play Mode
```
Mode toggle is a segmented button in the top-left. To its right, a title line shows the size of the active list for the current mode (inbox count in Sort Mode, queue track count in Play Mode). This bar is always visible and always shows the current mode.

**Mode transition behavior:**

*Switching to Sort Mode:*
Always shows a Sort Order picker (bottom sheet) before entering the mode. The picker pre-selects the last used order. Options: Newest First / Random / Closest to Threshold. Confirming the order begins playback of the first inbox track. Dismissing the sheet cancels the mode switch and returns to Play Mode.

*Switching to Play Mode:*
- Queue has tracks and playback is active → go directly to Now Playing
- Queue is empty or nothing is playing → go directly to Queue Editor

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
- **Closest to threshold** — tracks with score nearest to ±2 first (helps clear the inbox faster)

### 5.2 Voting Behavior

- **−1 tap** → advances immediately to next track
- **+1 tap** → plays to end of track, then advances
- **Skip** → advances immediately, score unchanged
- **Track ends with no vote** → advances automatically

Score is never displayed on screen. The user votes by feel, not by watching a counter.

### 5.3 Tag Sheet (on score reaching +2)

Fires as a modal bottom sheet before the track is moved to Library.

**BPM detection:** Kicks off when the tag sheet opens (not on import). While detection runs, the Pace and BPM rows show a spinner. When complete, values populate automatically. If the sheet is confirmed before detection finishes, BPM/Pace are left blank and can be edited later.

**Tag fields — all shown as chip groups, each chip individually removable:**
- **Genre** — predefined chips + "＋ Add" freeform input
- **Mood** — predefined chips + "＋ Add" freeform input
- **Pace** — `Slow` / `Medium` / `Fast` chips (single-select); auto-set from BPM, tappable to change
- **BPM** — numeric value shown as a chip; tap to edit manually
- **Labels** — freeform chips; tap "＋ Add" to enter text

**Pre-fill logic:** If tracks by the same artist or on the same album already exist in the Library, their Genre, Mood, Pace, and Labels chips are pre-selected. All pre-filled chips are removable — there is no separate accept/reject step.

**Confirm action:** Writes all currently selected tags to the MP3 file, sets `TXXX:STATUS=library`, moves file to Library folder (see §8.1), removes track from inbox feed.

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
[ gym ]  [ commute ]  [ late-night ]  ...

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
- Tags that would produce zero **included** results are hidden
- Track count on each chip reflects the count if that chip is toggled to included given the current selection

**Text filter:** A search/filter input at the top narrows visible chips by name.

**Saved presets:**
- A horizontal scrollable row of named presets at the top
- Tapping a preset loads its saved chip state instantly
- "Save current as preset" action available when filters are active
- Presets can be renamed or deleted via long-press

**CTAs:**
- **Shuffle & Play** — replaces current queue with a shuffled set of matching tracks and starts playback
- **Add to Queue** — appends matching tracks (shuffled) to current queue
- **Play Next** — inserts matching tracks after the currently playing track

### 6.3 Current Queue Screen

**Displayed:** Ordered list of queued tracks (title, artist, album art thumbnail)

**Interactions:**
- Drag handle to reorder
- Swipe to remove
- Tap to jump to that track
- Currently playing track highlighted

---

## 7. Tag Editing (Post-Library)

The same Tag Sheet used in Sort Mode is reachable from Play Mode for any Library track (via overflow menu on Now Playing, or via a track detail view). Saving writes tags back to the MP3 file immediately.

---

## 8. File Discovery & Storage Layout

### 8.1 Folder Structure

```
<watched folder>/
├── <any structure>   ← new files placed here by the user; treated as inbox
└── Library/          ← app moves files here on +2 promotion
```

Any MP3 in the watched folder tree that is not yet in the DB is treated as inbox — the user does not need to put files in a specific subfolder. On promotion, the file is moved to a flat `Library/` directory at the root of the same watched folder, regardless of its original subfolder nesting. The Library folder is created if it does not exist.

Example: `Music/new/artist/track.mp3` → `Music/Library/track.mp3`

If two inbox files share the same filename, the second promotion appends a numeric suffix (`track_2.mp3`).

### 8.2 Discovery

- **On app open:** Full scan of all configured watched folders. Any MP3 not in the DB is added as `inbox` with score 0.
- **Manual rescan:** Button in Settings; same behavior as on-open scan.
- **Live detection:** `FileObserver` monitors watched folders for new files while the app is open. New files are added to inbox immediately.

### 8.3 SD Card Support and File Deletion

Android imposes restrictions on SD card write access that vary by OS version:

- **Android ≤ 9:** `WRITE_EXTERNAL_STORAGE` generally permits SD card writes and deletes.
- **Android 10–12:** Scoped storage limits arbitrary SD card access. SAF (Storage Access Framework) is required — the user picks the SD card folder via the system folder picker, granting the app a persistent `Uri` with read/write/delete rights to that tree.
- **Android 13+:** Same SAF requirement. `READ_MEDIA_AUDIO` is needed for playback; SAF grants delete rights.

**Consequence for Settings:** When the user adds a watched folder, the app uses the SAF folder picker for all folders (internal and SD card) to obtain a persistent `DocumentFile` URI. This URI grants delete permission within the tree, which is required for the −2 deletion path and for moving files on promotion. `DocumentFile.delete()` works reliably on SD card via SAF.

`MANAGE_EXTERNAL_STORAGE` is explicitly avoided — it requires Play Store policy justification and is unnecessary when SAF covers all needed operations.

### 8.4 Permissions

| Permission | When required |
|---|---|
| `READ_MEDIA_AUDIO` | Android 13+, for MediaStore playback |
| `READ_EXTERNAL_STORAGE` | Android ≤ 12, for playback |
| SAF persistent URI | All versions, for write/delete in watched folders |

---

## 9. BPM Detection

- Triggered when a track reaches +2 score and the Tag Sheet opens — not on import, to avoid wasting CPU on tracks that may be deleted
- Runs on a background thread while the Tag Sheet is displayed; Pace and BPM rows show a spinner until complete
- If the user confirms the Tag Sheet before detection finishes, BPM and Pace are left blank and can be filled via tag editing later
- Detected BPM is written to `TBPM` ID3 field and cached in DB
- Pace bucket (`TXXX:PACE`) is derived: `slow` < 90 BPM, `medium` 90–140, `fast` > 140 (thresholds TBD)
- Both values are shown as editable chips in the Tag Sheet; user can override either
- If detection fails, fields are left empty; user sets manually

---

## 10. App Database (Room)

### 10.1 Role

ID3 tags are the **source of truth** for all metadata — they travel with the file and are readable by any player. The DB is a **queryable cache** of that data plus app-only state that has no ID3 equivalent (sort score, queue presets). All UI queries — including the faceted counts in Queue Editor — run against the DB only. Reading ID3 from disk at query time is not acceptable for large collections.

### 10.2 Sync Strategy

| Event | Action |
|---|---|
| File discovered on scan | Read ID3 → insert row into `tracks` |
| App writes tags (Tag Sheet confirm) | Write ID3 → update DB row atomically |
| App deletes a file | Delete file → remove DB row |
| FileObserver detects external file change | Re-read ID3 → update DB row |
| Manual rescan | Full folder walk; insert missing, update changed (by mtime), remove deleted |

### 10.3 Schema

**`tracks` table**

| Column | Source | Notes |
|---|---|---|
| `file_path` | disk | Primary key |
| `status` | `TXXX:STATUS` | `inbox` / `library` |
| `sort_score` | app only | Not written to ID3 |
| `title` | `TIT2` | |
| `artist` | `TPE1` | |
| `album` | `TALB` | |
| `genre` | `TCON` | Stored as comma-separated if multi-value |
| `mood` | `TMOO` | |
| `pace` | `TXXX:PACE` | `slow` / `medium` / `fast` |
| `bpm` | `TBPM` | Integer |
| `labels` | `TXXX:LABELS` | Comma-separated |
| `date_added` | app | Timestamp of first DB insert |
| `file_mtime` | disk | Used to detect external changes |
| `has_art` | ID3 `APIC` presence | Boolean; art itself is not cached in DB |

**`presets` table**

| Column | Notes |
|---|---|
| `name` | User-assigned preset name |
| `filter_state` | Serialized tag filter (JSON) |

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
| File watching | FileObserver / ContentObserver |
| DI | Hilt |
| Navigation | Compose Navigation |

---

## 14. Open Questions (Post-MVP)

- BPM detection library selection and accuracy threshold
- Pace bucket BPM boundaries (currently 90/140 — adjust after testing)
- Handling duplicate filenames on promotion (current spec: append numeric suffix)
- Handling duplicate files (same content, different path)
- What happens to DB records when a file is moved outside the app
