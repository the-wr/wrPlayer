1. Ask, don't assume. If something is unclear, ask before writing a single line. Never make silent assumptions about intent, architecture, or requirements.

2. Simplest solution first. Always implement the simplest thing that could work. Do not add abstractions or flexibility that weren't explicitly requested.

3. Don't touch unrelated code. If a file or function is not directly part of the current task, do not modify it, even if you think it could be improved.

4. Flag uncertainty explicitly. If you are not confident about an approach or technical detail, say so before proceeding. Confidence without certainty causes more damage than admitting a gap.

---

## Project: wrPlayer

Local Android music player for large, growing MP3 collections. Spec: [design-claude/PRD.md](design-claude/PRD.md) (single source of truth for requirements). MVP scope; MP3 only.

### Layout
- `android/` — the Android app (Gradle project root; all build commands run from here).
- `design-claude/PRD.md` — product requirements.
- `design-codex/` — ignored at repo root (`.gitignore`), not part of the build.

### Stack
Kotlin · Jetpack Compose · Media3 (ExoPlayer) · Room · Hilt · WorkManager · Compose Navigation · JAudioTagger (ID3 read/write) · TarsosDSP `be.tarsos.dsp:core` (BPM). Dependencies are declared via the version catalog at [android/gradle/libs.versions.toml](android/gradle/libs.versions.toml) — add/update versions there, not inline in `build.gradle.kts`.

- `applicationId` / `namespace`: `com.wrplayer`
- compileSdk 35, minSdk 26, targetSdk 35
- AGP 8.7.3, Kotlin 2.0.21, Gradle 8.11.1 (wrapper)
- ID3 tags are the source of truth; the Room DB is a queryable cache that follows the file (PRD §10).

### UI design (mocks)
`mocks/wrplayer/` is a **Claude Design handoff bundle** — the visual source of truth for the UI. The prototypes are HTML/CSS/JS (`.jsx`); recreate them **pixel-perfectly in Compose**, matching the visual output, not the prototype's internal structure. Read `mocks/wrplayer/README.md` first.

- **Entry point:** `mocks/wrplayer/project/wrPlayer Mockups.html` — read it in full and follow its imports before implementing a screen. Don't render it in a browser; read the source. `screenshots/` has rendered references.
- **Design system:** `project/shared.jsx` defines the per-dimension palette (PRD §6.2) as OKLCH hues — Genre 256, Mood 305, Pace 152, Labels 58, Artist 196, Album 88 — plus `chipStyle()` (idle/included/excluded states). Use these exact hues so chip colors stay consistent across tag sheet, now-playing, and queue editor. `android-frame.jsx` is the Material 3 device frame (not app UI).
- **Screen → PRD mapping:** `now-playing.jsx` (§4.1 shared Sort/Play), `tag-sheet.jsx` (§5.3), `sort-extras.jsx` (§5.4 empty/end states), `queue-editor-*.jsx` (§6.2), `current-queue.jsx` (§6.3), `settings.jsx` (§3/§8).
- **Queue Editor variant:** build **variant H** (decided). Defined in `queue-editor-fg.jsx` as `QueueEditorH` (+ `QueueEditorHPreview` for the held/expanded preview state); reference screenshots `variant-h/-h2/-h3`. H is the refined G: modal dismissed by back-nav (no in-modal close button), no redundant exclude dash, "Enqueue" CTA label, no grabber. Ignore the other `queue-editor-a` … `-g` variants.

### Build environment (Windows)
- **JDK 21 is required and is pinned** in [android/gradle.properties](android/gradle.properties) via `org.gradle.java.home` → Android Studio's bundled JBR (`C:/Program Files/Android/Android Studio1/jbr`). The machine's default `JAVA_HOME`/`java` is JDK 25, which AGP does **not** support — do not rely on it. If that JBR path moves, update `gradle.properties`.
- Android SDK at `C:\SDK\Android` (`ANDROID_HOME`); referenced by `android/local.properties` (`sdk.dir`, git-ignored).
- `sdkmanager` / `avdmanager` live in `C:\SDK\Android\cmdline-tools\latest\bin` (not on PATH).
- `org.gradle.configuration-cache` is **off** — AGP 8.7 has a config-cache serialization bug with source-set-path tasks. Re-enable only after an AGP bump confirms it's fixed.

### Common commands (run from `android/`)
- Build debug APK: `.\gradlew.bat :app:assembleDebug`
- Install on a running device/emulator: `adb install -r app\build\outputs\apk\debug\app-debug.apk`
- Launch emulator: `C:\SDK\Android\emulator\emulator.exe -avd Pixel_9` (AVDs available: `Pixel_9`, `Medium_Tablet`)
- adb / emulator binaries: `C:\SDK\Android\platform-tools\adb.exe`, `C:\SDK\Android\emulator\emulator.exe`

### Conventions
- After editing code, check LSP diagnostics and fix type/import errors before moving on.
- Capturing a device screenshot: use `adb shell screencap -p /sdcard/x.png` then `adb pull` — piping `adb exec-out screencap` through PowerShell `>` corrupts the PNG (UTF-16 BOM).