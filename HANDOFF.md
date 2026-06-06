# wrPlayer — Session Handoff

Context dump so a fresh session can continue the autonomous build flawlessly. Pairs with the
roadmap in `C:\Users\Wr\.claude\plans\create-a-plan-to-jolly-squirrel.md`, the spec in
[design-claude/PRD.md](design-claude/PRD.md), and the project rules in [CLAUDE.md](CLAUDE.md).

## TL;DR — where we are

Building wrPlayer (local Android MP3 player, Sort + Play modes) phase-by-phase with verification
gates. **Phases 0–8 are done, committed, and verified. Phases 9 and 10 are built and verified live
on device (awaiting visual sign-off, not yet committed). Phase 11 is next.**

- **The app runs end-to-end in Sort Mode today**: empty-inbox → seed/picker → queue → playback →
  +1/−1 voting, all live on the Pixel_9 emulator.
- **66 JVM unit tests (18 classes) + 4 instrumented tests pass.** All three high-risk platform
  bridges (ID3-over-SAF, TarsosDSP BPM, Media3) are proven on the real device.
- Working tree is clean (committed). 77 Kotlin source files under `android/app/src/main`.

## Build / run / test — the workflow that works

**Use the Bash tool, not PowerShell**, for gradle/adb (the PowerShell safety classifier had an
outage mid-session; Bash is unaffected). Always `cd` first — the Bash cwd doesn't reliably persist.

```bash
cd /d/Work/wrPlayer/android
./gradlew.bat :app:assembleDebug            # build (JDK 21 pinned in gradle.properties)
./gradlew.bat :app:testDebugUnitTest        # JVM + Robolectric tests
# Instrumented (NOTE: connectedAndroidTest has no --tests; use the runner arg):
./gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.wrplayer.data.id3.SafId3GatewayTest"
```

**Emulator + adb** (`C:\SDK\Android`):
```bash
ADB="/c/SDK/Android/platform-tools/adb.exe"
# Launch if needed: C:\SDK\Android\emulator\emulator.exe -avd Pixel_9 -no-snapshot-load  (background; wait for sys.boot_completed=1)
"$ADB" install -r app/build/outputs/apk/debug/app-debug.apk
"$ADB" shell monkey -p com.wrplayer -c android.intent.category.LAUNCHER 1
"$ADB" exec-out screencap -p > /d/Work/wrPlayer/android/shot.png   # screenshot (then Read the PNG)
"$ADB" shell cmd uimode night yes|no                              # toggle dark/light
"$ADB" shell input tap X Y                                        # drive UI (device coords, 1080x2424)
```

**MSYS path gotchas (git-bash on Windows):**
- `adb exec-out screencap -p > file.png` works (binary-safe). Do **not** use PowerShell `>` (UTF-16
  BOM corrupts the PNG) and avoid `adb pull /sdcard/...` (path mangling).
- For commands with **device paths** (`/sdcard/...`): `export MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*'`.
- For `adb push`: use a **Windows-form local path** (`D:/Work/...`) and a `//`-escaped remote
  (`//sdcard/...`) so neither gets mangled.

**Test fixtures / Sort Mode demo data:**
- Real MP3s live in `d:\Work\wrPlayer\fixtures\` (3 files). One is copied to
  `app/src/test/resources/fixtures/sample.mp3` (JVM) and `app/src/androidTest/assets/fixtures/sample.mp3`
  (instrumented).
- To exercise Sort Mode live without Settings/SAF yet: push two fixtures to the app's external dir,
  then tap **"Seed demo tracks"** on the empty Sort state (temp `SortViewModel.seedDemoInbox()`):
  ```bash
  "$ADB" push "D:/Work/wrPlayer/fixtures/Eric Speed - Starfighter.mp3" "//sdcard/Android/data/com.wrplayer/files/sample1.mp3"
  "$ADB" push "D:/Work/wrPlayer/fixtures/Danny feat Therese - If Only You (Radio Version).mp3" "//sdcard/Android/data/com.wrplayer/files/sample2.mp3"
  ```
  ⚠️ `pm clear com.wrplayer` wipes that external dir — re-push afterwards.

## Architecture (as built)

Single `:app` module, package root `com.wrplayer`, light **data / domain / ui** split.

```
domain/                         pure Kotlin, JVM-tested, no Android deps
  model/  TagDimension, Pace, TrackStatus, SortOrder, ChipState, FilterState
  facet/  FacetFilter (OR-within/AND-across/AND-NOT + prospective counts), LibraryTrackTags, FacetValue
  FilenameParser, TagSuperset, TagPrefill, TagSheetValidation, PaceDeriver, PredefinedTags
  SortScore + SortReducer (voting state machine), SortQueueBuilder
data/
  db/        TrackEntity, PresetEntity, TrackTagEntity, TrackDao, PresetDao, WrDatabase, TagFormat (null-sep)
  id3/       Id3Reader, Id3Writer (JAudioTagger, File-based), SafId3Gateway (SAF↔cache bridge), Mp3TagData
  bpm/       BpmDetector, TarsosBpmDetector (MediaCodec→PCM→Tarsos), StubBpmDetector, PcmDecoder
  saf/       SafTreeManager, SafDocumentEnumerator, SafTrackFileStore, FileMover, SafMembership, FileNaming
  scan/      LibraryReconciler (orchestration), ReconcilePlanner, TrackMapping, ReconcileSeams, ReconciliationWorker
  playback/  PlaybackService (MediaSessionService), PlayerConnection (MediaController wrapper), PlaybackState, MediaItems
  prefs/     AppPreferences (SharedPreferences: lastSortOrder, queue+index+position)
  repo/      TrackRepository(+Impl), TrackFileStore (seam)
  di/        DatabaseModule, RepositoryModule, ScanModule, BpmModule
ui/
  theme/  Oklch, WrColors, Dimensions (chip colors, hues), Theme, Type (Hanken+JetBrainsMono), DimensionChip
  common/ AppMode, WrTopBar
  nav/    DefaultMode (start-destination routing)
  nowplaying/ NowPlayingScreen (shared shell), SortPanel
  tagsheet/   TagSheet, TagChips, TagSheetState, TagSheetLoader
  sort/   SortViewModel, SortScreen, SortOrderPicker
  WrApp (top-level shell, mode toggle, window insets)
MainActivity (@AndroidEntryPoint → WrApp), WrPlayerApp (@HiltAndroidApp + WorkManager Configuration.Provider)
```

**Boundaries:** UI/ViewModels touch only repositories/DAOs + interfaces; SAF is isolated behind
seams (`WatchedTreeSource`, `DocumentEnumerator`, `TrackTagSource`, `TrackFileStore`) so logic is
unit-tested with fakes. `TrackRepository` enforces **file-first** writes (write ID3 → move/delete →
DB, with `track_tags` rebuilt in the same transaction). Hilt throughout; `@HiltViewModel` VMs.

## Phases done (key files + how verified)

- **0 Test harness** — JUnit/Truth/Robolectric/Hilt-testing/Compose-test; `HiltTestRunner`. Both pipelines green.
- **1 Room + FacetFilter** — schema (`tracks`/`presets`/`track_tags`), `track_tags` rebuilt in-txn; `FacetFilter` exhaustive truth tables. JVM/Robolectric.
- **2 ID3 bridge** — JAudioTagger read/write ID3v2.4 incl. null-sep multi-value + `TXXX` (STATUS/PACE/LABELS). Verified JVM (`Id3ReadWriteTest`) + on-device (`SafId3GatewayTest`); copy-edit-copyback round-trip preserves the file.
- **3 BPM** — `TarsosBpmDetector` (MediaCodec decode → manual PCM feed → onset histogram). **GO: detected 136 BPM on-device.** `StubBpmDetector` fallback wired via `BpmModule`.
- **4 SAF + reconciliation** — `LibraryReconciler` (insert/update-preserving-score+bpm/remove, unmounted-folder guard) unit-tested with fakes; SAF impls + WorkManager wiring compile + app launches. *Real-SAF enumeration/move verified later (can't grant SAF headlessly).*
- **5 Media3** — `PlaybackService` + `PlayerConnection`; **on-device: playback + media notification with album art + lock-screen session.**
- **6 Design system** 🔔 **approved** — OKLCH engine, `DimensionChip` (solid scheme), `WrTopBar`, bundled **Hanken Grotesk + JetBrains Mono** variable fonts. Light/dark signed off.
- **7 Tag Sheet + write paths** — `FilenameParser`/`TagSuperset`/`TagPrefill`/`TagSheetValidation` + `TrackRepository.promote/editTags/delete` with DB re-key (Robolectric). `TagSheet` composable verified on-device.
- **8 Sort Mode** — `SortReducer`/`SortQueueBuilder` (12 tests) + `SortViewModel` + `SortScreen` + picker + end state + real nav shell. **Live end-to-end on device.**
- **9 Queue Editor (variant H) + Play panel** 🔔 *awaiting visual sign-off* — built and **verified live on device**: promoted a track → Queue Editor shows faceted sections (Genre/Mood/Pace/Artist) with prospective counts → three-state chip toggle + active-filter chip → **Shuffle & Play** replaced the queue and started playback → Play panel shows the current track's read-only color chips + Edit + Queue Editor/Current Queue. 77 unit tests pass (added projection, preset JSON, staleness, CTA-at-0). See **Phase 9 notes** below.

## What remains

### Phase 9 notes (built — pending visual sign-off)
- `domain/facet/FacetFilter.staleSelections()` (per-value, independent of rest of filter); `data/db/TrackTagsProjection.kt` (`List<TrackTagEntity>.toLibraryTrackTags()`).
- `data/repo/FilterStateJson` (org.json; tested under Robolectric) + `PresetRepository`(+Impl) over `PresetDao`, bound in `RepositoryModule`.
- `ui/queue/QueueEditorStateHolder` (@Singleton session filter), `QueueEditorViewModel` (combines library projection × filter × presets × search; CTAs shuffle matching URIs), `QueueEditorScreen` (variant H: back-nav dismiss, no close, "Enqueue", no grabber, hold-to-preview overlay, preset save/rename/delete dialogs, stale toast).
- `PlayerConnection.addToQueue/playNext/queueMediaIds/currentIndex`; queue persisted to `AppPreferences` after each CTA. `TrackDao.getByUris`.
- `ui/nowplaying/PlayPanel`, `ui/play/PlayViewModel` + `PlayScreen` (Now Playing + panel + QueueEditor/CurrentQueue overlays + Edit→TagSheet via `repo.editTags`); `WrApp` PLAY branch → `PlayScreen`. CurrentQueue is a Phase-10 placeholder.
- **Open visual nits for sign-off:** Pace chip renders the stored key lowercase ("medium") vs mock "Medium" — decide whether to title-case pace display. Hold-to-preview overlay lists matching tracks (cover placeholder + title/artist), no per-row duration (we don't cache duration).

### Phase 10 notes (built — pending visual sign-off)
**Verified live on device:** first-launch routing → Settings; SAF folder picker → add `Music` → "1 active"; live scan spinner ("Scanning…"); scan populated the inbox (Sort picker read "3 tracks" from the SAF folder); queue persistence restored on launch (paused); Current Queue renders to the mock (now-playing highlight + grip + mini footer). **Real SAF is now exercised end-to-end** (add folder → scan → inbox), so the seed hack is only a fallback.
- `data/playback`: `PlaybackState` gained `currentIndex` + `queue: List<QueueTrack>`; `PlayerConnection` gained `restoreQueue`/`moveItem`/`removeItem`/`seekToItem` and emits the queue list in `pushState`.
- `ui/play/CurrentQueueScreen` (`current-queue.jsx`): tap-to-jump, grip drag-reorder (live single-step `moveItem`), swipe-to-remove (Animatable offset), now-playing highlight + mini footer. Wired into `PlayScreen` (replaces the placeholder).
- `ui/play/PlayViewModel`: persists queue+index on each player event and position every ~3s; restores the persisted queue once on first connect when the player is empty (paused). Exposes queue/currentIndex.
- `ui/settings/SettingsScreen` + `SettingsViewModel` (`settings.jsx`): persisted-tree list with name/path/INTERNAL|SD badge/availability, SAF picker via `rememberLauncherForActivityResult(OpenDocumentTree)` → `SafTreeManager.persist` + rescan, per-folder remove (`release` + rescan), manual "Rescan now". **Theme/accent customization deliberately omitted** — not in the mock or PRD; `settings.jsx` is Watched-Folders-only. `WrPlayerTheme` still reads the hardcoded accent (fine; revisit only if a theme picker is ever specced).
- `data/scan/ScanStatus` (@Singleton, WorkManager `getWorkInfosForUniqueWorkFlow`) drives the top-bar scan spinner in Sort + Play.
- `ui/AppShellViewModel` + rewritten `WrApp`: resolves the launch route via `DefaultMode` (no folders → Settings overlay; else Play), kicks the on-open reconcile walk, hosts the Settings overlay (gear / back-dismiss).
- **Open visual nits for sign-off:** drag-reorder is a live single-step move (finger doesn't pixel-track the row); no per-row duration in the queue rows (we don't cache duration); removing a watched folder leaves its tracks' rows until they're confirmed-absent under a *reachable* tree (orphan rows possible — acceptable per §8.2 guard, revisit in Phase 11 if undesired).

### Phase 11 — Integration hardening
- Full loop on device; edge cases: unmounted/empty-folder guard, external-edit preserved across scan, queued-track-removed-mid-play skip (§6.1). `assembleRelease` builds.
- **Remove temp code:** `SortViewModel.seedDemoInbox()` + the "Seed demo tracks" empty-state button → real "add a watched folder" guidance.

## Key decisions & deviations (so they aren't "fixed" by mistake)

- **Faceted counts computed in-memory** in `FacetFilter` over the `track_tags` projection (refreshed reactively), not per-toggle SQL `GROUP BY`. Honors §10.1 (queries hit `track_tags`, never ID3 on disk); revisit only if huge libraries prove slow.
- **Solid chip scheme** + **bundled fonts** were explicit user choices (Phase 6 sign-off).
- **Sort Mode plays one track at a time** (the VM owns advancement) to avoid playlist auto-advance/commit races. Play Mode uses the player's real playlist.
- **SAF behind seams**; real-SAF enumeration/move/delete is the main thing not yet exercised on device.
- **Config cache OFF** (AGP 8.7 bug) — don't re-enable without testing. JDK 21 pinned (system default is JDK 25, unsupported).

## Known gaps / deferred
- In-app **album art not loaded** (placeholder music-note); the media *notification* shows real art. Add Coil loading from the document URI for the in-app cover.
- Audio-focus auto-resume nuance (Media3 default may resume after transient loss; PRD §6.1 says "Android default", so acceptable).
- Queued-track-removed-mid-play skip (§6.1) to implement in Play Mode.

## Pointers
- Roadmap/plan: `C:\Users\Wr\.claude\plans\create-a-plan-to-jolly-squirrel.md`
- Spec: [design-claude/PRD.md](design-claude/PRD.md) (cite § numbers)
- Visual source of truth: [mocks/wrplayer/project/](mocks/wrplayer/project/) — `shared.jsx`, `now-playing.jsx`, `tag-sheet.jsx`, `queue-editor-fg.jsx` (variant H), `current-queue.jsx`, `settings.jsx`; rendered refs in `screenshots/`
- Project rules + build env + variant-H note: [CLAUDE.md](CLAUDE.md)
