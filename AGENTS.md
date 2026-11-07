# Agent guidelines — EasyGamepad

This file helps AI coding agents and automation work safely and consistently in
this repository.

**Canonical remote:** [https://github.com/edgardmessias/easygamepad](https://github.com/edgardmessias/easygamepad)

## Project shape

- **`app/`** — Android application (Compose UI, BLE client, preferences).
- **`protocol/`** — KRLY framing and controller configuration models (JVM tests, no Android).
- **`gradle/`**, `build.gradle.kts`, `settings.gradle.kts` — build configuration.

## Conventions

- **English** for new documentation, comments that explain non-obvious behavior,
  type names, and public API surface unless matching existing localized UI strings.
- Do **not** add secrets (keystores, API keys) or large binaries to the tree.
- **GPL-3.0-or-later**: respect [LICENSE](LICENSE), [NOTICE](NOTICE), and
  [THIRD_PARTY.md](THIRD_PARTY.md); preserve SPDX headers where present.
- **Third-party assets:** X10 images and layout JSON come from x20ctl; do not
  strip required notices when touching related files.

## Safe areas to change

- Kotlin UI and ViewModel logic under `app/src/main/kotlin/`.
- Protocol codecs and tests under `protocol/src/`.
- Gradle dependencies (coordinate with maintainers for major bumps).

## Sensitive areas (extra care)

- **`BleClient` / GATT** — threading, permissions, and connection lifecycle are
  easy to break; prefer minimal diffs and read surrounding code first.
- **`ProfileStore` / DataStore** — migration keys affect existing users.
- **Signing and `dist/`** — do not commit private keys; release signing and source
  obligations are covered in [CONTRIBUTING.md](CONTRIBUTING.md) and [LICENSE](LICENSE).

## Typical verification

```powershell
.\gradlew.bat :protocol:test :app:lintDebug :app:testDebugUnitTest
```

## Git

- **Do not commit** unless the maintainer explicitly requests it.
- Default ignore rules exclude `build/`, keystores, local SDK paths, and other
  patterns listed in `.gitignore`.

## Localization

The app supports multiple languages at runtime. New user-visible strings should
go through the project’s existing localization mechanism (do not hardcode only
one language in new UI without following established patterns).
