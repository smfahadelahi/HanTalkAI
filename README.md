# HanTalk AI (한톡 AI)

A Korean language learning app for Android with a **built-in, fully local AI tutor**.
No account, no server, no network required once installed.

[![Build HanTalk AI APK](https://github.com/smfahadelahi/HanTalkAI/actions/workflows/build-apk.yml/badge.svg)](https://github.com/smfahadelahi/HanTalkAI/actions/workflows/build-apk.yml)

---

## Download the APK

1. Open the [**Actions**](../../actions/workflows/build-apk.yml) tab and pick the latest green run.
2. Scroll to **Artifacts** and download **`HanTalkAI-release-apk`**.
3. Unzip it, copy `app-release.apk` to your phone, and install it
   (you will need to allow "install unknown apps" for your browser or file manager).

| Artifact | Use it for |
|---|---|
| `HanTalkAI-release-apk` | **Normal installation.** Minified, signed, 43 MB. |
| `HanTalkAI-debug-apk` | Debugging with logs attached. Installs side by side. |

> Requires Android 8.0 (API 26) or newer.

CI signs the release APK with a throwaway key generated per build, so builds from
different runs are not upgrade-compatible with each other — uninstall before
installing a newer one. For a real release, add the four `KEYSTORE_*` /
`KEY_*` secrets listed in the workflow and the same key is reused.

---

## What it does

### 1. AI Tutor
A chat tutor that teaches Korean. It works in two modes and switches automatically:

- **Offline tutor (default, no download).** A rule-based engine covering the
  alphabet, 받침, numbers, particles, politeness, romanisation and a jamo
  breakdown of any Korean you type. Works on a brand-new install, offline.
- **On-device LLM (optional).** Install a Gemma or Qwen model from the
  **AI models** screen and get free-form conversation and translation, still
  100 % offline on the phone. If the model fails to load, the app silently
  falls back to the offline tutor instead of breaking.

### 2. Lessons
Six themed sets — greetings, introductions, café, directions, numbers, small talk —
with 37 phrases. Each phrase opens a sheet with a syllable-by-syllable **jamo
breakdown**, its romanisation, and one-tap **Practice** / **Explain**.

### 3. 한글 Alphabet
All 19 consonants and 15 vowels, each with its name, sound, an example word and a
live demo syllable (ㄱ → 가). Progress is saved so you can see how many letters
you have marked as learned.

### 4. Practice
Translation drills scored by edit distance, plus Korean **text-to-speech**
playback, a day streak and a best-score record.

---

## How the Korean engine works

`domain/hangul/Hangul.kt` is pure Kotlin with no Android dependencies, so it is
fully unit-tested on the JVM.

It decomposes pre-composed syllables into 초성/중성/종성 and romanises them with
real sound rules rather than a lookup table:

| Input | Output | Rule |
|---|---|---|
| `한국어` | `hangugeo` | liaison — 받침 moves into a following ㅇ syllable |
| `감사합니다` | `gamsahamnida` | nasalisation — ㅂ becomes ㅁ before ㄴ |
| `저는 학생이에요` | `jeoneun haksaengieyo` | final ㅇ never moves; it stays *ng* |
| `좋아` | `joa` | ㅎ drops before a vowel |

Curated romanisations override the engine where Korean is irregular
(loanwords such as *americano*, tensification in `맛있어요`).

---

## Build it yourself

```bash
./gradlew :app:testDebugUnitTest   # 31 unit tests
./gradlew :app:assembleDebug       # debug APK
./gradlew :app:assembleRelease     # release APK (needs a keystore, see below)
```

Release signing reads its configuration from the environment, so **no secrets
live in the repository**:

```bash
export HANTALK_KEYSTORE_PATH=/path/to/release.jks
export HANTALK_STORE_PASSWORD=...
export HANTALK_KEY_ALIAS=...
export HANTALK_KEY_PASSWORD=...
./gradlew :app:assembleRelease
```

Without those variables the release build simply produces an unsigned APK.

A structural check that catches unbalanced brackets and stray string
delimiters before CI does:

```bash
python3 scripts/check-kotlin-structure.py
```

## Tech stack

Kotlin 2.0 · Jetpack Compose (Material 3) · MediaPipe `tasks-genai` for on-device
inference · Android `TextToSpeech` for Korean audio · AGP 8.7 / Gradle 8.11 / JDK 17.

## Project layout

```
app/src/main/java/com/hantalk/koreanai/
├── domain/          Hangul engine, curriculum data, similarity scoring (pure Kotlin)
├── llm/             MediaPipe wrapper, model registry, offline rule-based tutor
├── data/            Preferences, model storage, resumable downloader
├── speech/          Korean text-to-speech
└── ui/              Compose screens, theme, ViewModel
```

## Models are not bundled

A 1–3 GB model inside an APK would be a terrible install experience, so models
are downloaded on demand into app storage, with **resumable** downloads.
Nothing is uploaded anywhere — inference happens on the phone.

## Licence

Model weights you download (Gemma, Qwen) are governed by their own licences and
are not distributed by this project.
