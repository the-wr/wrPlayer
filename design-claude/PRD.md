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
| BPM | `TBPM` | Standard | Auto-detected on import; user-overridable |
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
├── Sort Mode
│   ├── Track View
│   └── Tag Sheet  (modal — fires when score hits +2)
│
├── Play Mode
│   ├── Now Playing
│   ├── Queue Editor
│   └── Current Queue
│
└── Settings
    └── Watched Folders
```

Queue Editor and Current Queue are both accessible directly from Now Playing (two separate buttons).

---

## 4. Sort Mode

### 4.1 Feed

The inbox feed presents tracks not yet in the Library. Feed order is user-selectable via a control on the Sort Mode screen:

- **Newest first** — ordered by file modification date descending
- **Random** — random shuffle of all inbox tracks
- **Closest to threshold** — tracks with score nearest to ±2 first (helps clear the inbox faster)

### 4.2 Track View

**Displayed:**
- Album art (embedded, if present)
- Title, Artist, Album
- Current score (e.g. `+1`, `0`, `−1`)
- Playback progress bar (scrubable)

**Controls:**
- Large **+1** button (thumb-reachable)
- Large **−1** button (thumb-reachable)
- **Skip** — advances without voting

**Advance behavior:**
- **−1 tap** → advances immediately to next track
- **+1 tap** → plays to end of track, then advances
- **Skip** → advances immediately, score unchanged
- **Track ends with no vote** → advances automatically

### 4.3 Tag Sheet (on score reaching +2)

Fires as a modal bottom sheet before the track is moved to Library.

**Fields:**
- **Genre** — predefined chip list + "Add custom" option (multi-select)
- **Mood** — predefined chip list + "Add custom" option (multi-select)
- **Pace** — auto-set from BPM detection (`slow` / `medium` / `fast`), tappable to override
- **BPM** — auto-detected value shown, tappable to edit manually
- **Labels** — freeform text input, space or comma-separated

**Pre-fill logic:** If other tracks by the same artist or on the same album already exist in the Library, their Genre, Mood, Pace, and Labels are pre-populated. User can accept or modify.

**Confirm action:** Writes all tags to the MP3 file, sets `TXXX:STATUS=library`, moves file to Library folder (see §7), removes track from inbox feed.

### 4.4 Empty Inbox State

When the inbox is empty, Sort Mode shows a prompt to rescan or a confirmation that everything has been sorted.

---

## 5. Play Mode

### 5.1 Now Playing Screen

**Displayed:**
- Album art (large, center)
- Title, Artist, Album
- Playback progress bar (scrubable)
- Current position / total duration

**Controls:**
- Play / Pause
- Previous track
- Next track
- **Queue Editor** button → navigates to Queue Editor screen
- **Current Queue** button → navigates to Current Queue screen
- **Edit Tags** action (e.g. via overflow menu or long-press on track info) → opens Tag Sheet for the current track

**Playback behavior:**
- Queue ends → playback stops
- Headphone disconnect → playback pauses
- Audio focus lost (call, other app) → playback pauses
- Android media notification with transport controls
- Lock screen media session

### 5.2 Queue Editor Screen

**Purpose:** Build a set of tracks by tag combination and add them to the queue.

**Layout:**

```
[ Saved Presets: "morning run"  "gym"  "late night"  +New ]

Active filters: [ Genre: Rock × ]  [ Mood: Hype × ]

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

**Filter logic:**
- Chips within the same dimension: **OR** (e.g. Rock OR Jazz)
- Chips across dimensions: **AND** (e.g. (Rock OR Jazz) AND Hype)
- Tags within each dimension are sorted by matching track count, descending
- Tags that would produce zero results given current selection are hidden
- Track count on each chip reflects the count if that tag is added to the current selection

**Text filter:** A search/filter input at the top narrows visible chips by name (does not filter tracks directly).

**Active filters:** Shown as dismissible chips at the top of the screen; tapping × removes that filter.

**Saved presets:**
- A horizontal scrollable row of named presets at the top
- Tapping a preset loads its saved chip state instantly
- "Save current as preset" action available when filters are active
- Presets can be renamed or deleted via long-press

**CTAs:**
- **Shuffle & Play** — replaces current queue with a shuffled set of matching tracks and starts playback
- **Add to Queue** — appends matching tracks (shuffled) to current queue
- **Play Next** — inserts matching tracks after the currently playing track

### 5.3 Current Queue Screen

**Displayed:** Ordered list of queued tracks (title, artist, album art thumbnail)

**Interactions:**
- Drag handle to reorder
- Swipe to remove
- Tap to jump to that track
- Currently playing track highlighted

---

## 6. Tag Editing (Post-Library)

The same Tag Sheet used in Sort Mode is reachable from Play Mode for any Library track (via overflow menu on Now Playing, or via a track detail view). Saving writes tags back to the MP3 file immediately.

---

## 7. File Discovery & Storage Layout

### 7.1 Folder Structure

```
<watched folder>/
├── Inbox/       ← newly added files land here (or any subfolder not yet in DB)
└── Library/     ← files moved here on +2 promotion
```

The app moves the physical file from Inbox to Library on promotion. This makes the folder structure readable by any desktop app without needing to parse custom ID3 frames.

### 7.2 Discovery

- **On app open:** Full scan of all configured watched folders. Any MP3 file not present in the app DB is added as `inbox` with score 0. BPM detection runs in the background for newly discovered files.
- **Manual rescan:** Button in Settings; same behavior as on-open scan.
- **Live detection:** `FileObserver` (or `ContentObserver` via MediaStore) monitors watched folders for new files while the app is open or in background. New files are added to inbox immediately.

### 7.3 Watched Folders Configuration (Settings)

- User can add one or more folders from internal storage or removable SD card
- Each folder entry shows path and track count
- Removing a folder does not delete tracks from DB or disk; they become orphaned (not shown anywhere until re-added)

### 7.4 Permissions

- Android 10 and below: `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
- Android 11+: `MANAGE_EXTERNAL_STORAGE` (or scoped storage with SAF for SD card access)
- Android 13+: `READ_MEDIA_AUDIO`

---

## 8. BPM Detection

- Runs in a background WorkManager job on newly discovered inbox files
- Detected BPM is written to `TBPM` ID3 field
- Pace bucket (`TXXX:PACE`) is derived: `slow` < 90 BPM, `medium` 90–140, `fast` > 140 (thresholds TBD)
- Both BPM and Pace are shown in the Tag Sheet and are user-overridable
- If detection fails, fields are left empty; user sets manually

---

## 9. App Database (Room)

The app DB stores the following (all derivable from disk on a full rescan, so it is a cache + sort-score store):

| Table | Key fields |
|---|---|
| `tracks` | file path, status, sort score, date added to DB, date file modified |
| `presets` | preset name, serialized tag filter state |

ID3 tags are the source of truth for all metadata. The DB stores only what cannot live in the file (sort score, presets) and what is expensive to re-read (file path index for change detection).

---

## 10. Predefined Tag Lists (MVP)

### Genres (initial set, user can add freeform)
Electronic, Rock, Hip-Hop, Jazz, Classical, Ambient, Folk, Metal, R&B, Pop, Funk, Soul, Reggae, Latin, World

### Moods (initial set, user can add freeform)
Hype, Energetic, Happy, Melancholy, Chill, Focus, Dark, Romantic, Nostalgic, Aggressive

### Pace buckets (fixed, derived from BPM)
Slow, Medium, Fast

---

## 11. Out of Scope (MVP)

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

## 12. Technical Stack (Recommended)

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

## 13. Open Questions (Post-MVP)

- BPM detection library selection and accuracy threshold
- Pace bucket BPM boundaries (currently 90/140 — adjust after testing)
- SD card write access UX (SAF picker flow)
- Handling duplicate files (same content, different path)
- What happens to DB records when a file is moved outside the app
