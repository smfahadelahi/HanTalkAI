# Changelog

All notable changes to HanTalk AI, newest first.
Each release also has its own notes file in this folder (`v1.1.0.md`, `v1.0.0.md`),
and CI publishes that file verbatim as the GitHub Release description.

## [Unreleased]

Nothing yet.

## [1.1.0] — 2026-11-16

Voice-first release. The app now opens in voice mode, and the entire UI was rebuilt
on a new design system.

### Added

- **Voice mode home screen** — a procedural, animated voice orb plus a Korean
  conversation loop: tap, speak, hear the answer.
- **Korean speech recognition** (`speech/VoiceRecognizer.kt`) with partial results,
  microphone level metering and readable error states.
- **Audio everywhere** — *Hear it* / *Slowly* (0.7×) / replay controls on the voice
  screen, lessons, alphabet and practice.
- **SAY THIS** card — a fresh phrase each visit with hear / slowly / practise.
- **Voice answers in practice** — speak your attempt instead of typing it.
- **Auto-speak toggle** (on by default) with a persisted preference.
- **Design system** — brand gradients, semantic jade/coral/amber tokens, warm paper
  surfaces, 8-step type scale, rounded shape tokens, full dark theme.
- **New components** — `SoftCard`, `Pill`, `GhostPill`, `StatStrip`, `ScoreBadge`,
  gradient `Bar`, redesigned `PhraseRow` / `ScreenHeader`.
- **Custom bottom bar** with a gradient pill on the selected destination.
- **Release notes** are now versioned in `releases/` and attached by CI.

### Changed

- Default tab is **Voice**; the chat tutor moved to second position as *Chat*.
- Lessons, Alphabet, Practice, Tutor and AI models screens redesigned.
- Launcher icon and edge-to-edge system bars refreshed.
- Version bumped to `1.1.0` (versionCode 2).

### Fixed

- Speech services were invisible to the app on Android 11+ (missing package
  visibility `<queries>`), which broke `isRecognitionAvailable()`.
- A model that streamed nothing left a permanently empty chat bubble.
- The first TTS utterance was dropped because the engine was still starting.
- Status/navigation bar icons were unreadable in dark mode.

## [1.0.0] — 2026-11-10

First public release.

- Offline rule-based Korean tutor, optional on-device Gemma/Qwen models.
- Six lesson sets, 37 phrases with jamo breakdowns.
- Full 한글 alphabet screen with saved progress.
- Edit-distance practice drills, Korean TTS, day streak, best score.
- On-demand, resumable model downloads; nothing leaves the device.

[1.1.0]: https://github.com/smfahadelahi/HanTalkAI/releases
[1.0.0]: https://github.com/smfahadelahi/HanTalkAI/releases
