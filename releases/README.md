# Releases

Notes for every HanTalk AI build. Each file here is published verbatim as the
GitHub Release description by the CI workflow
(`.github/workflows/build-apk.yml` → *Compose release notes*), with a short build
footer appended (size, commit, SHA-256).

| Version | Date | Notes | Highlights |
|---|---|---|---|
| **1.1.0** | 2026-11-16 | [v1.1.0.md](v1.1.0.md) | Voice-first home screen, new design system, redesigned screens, release tooling |
| 1.0.0 | 2026-11-10 | [v1.0.0.md](v1.0.0.md) | First public release: offline tutor, lessons, 한글, practice |

Full history: [CHANGELOG.md](CHANGELOG.md).

## Download

Newest build: [github.com/smfahadelahi/HanTalkAI/releases/latest](https://github.com/smfahadelahi/HanTalkAI/releases/latest)

Each release attaches:

- `HanTalkAI-<version>-release.apk` — installable, signed, minified release build
- `HanTalkAI-<version>-release.apk.sha256` — checksum for verifying the download

Verify a download with:

```bash
sha256sum -c HanTalkAI-1.1.0-release.apk.sha256
```

## Adding a new release

1. Bump `versionCode` / `versionName` in `app/build.gradle.kts`.
2. Add `releases/v<version>.md` with the user-facing notes.
3. Add a row to the table above and an entry to `CHANGELOG.md`.
4. Merge to `main` — CI builds, verifies, and publishes the release with those notes.

If a notes file is missing, CI warns and falls back to a short generated body, so a
release can never fail because of documentation.
