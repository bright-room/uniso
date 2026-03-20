# Uniso

A unified desktop client for managing multiple SNS accounts in one place.

Uniso lets you log in to X, Instagram, Facebook, YouTube, Bluesky, and Twitch simultaneously — each in its own isolated browser session — and switch between them with a single click or keyboard shortcut.

## Features

- **Multi-service, multi-account** — manage accounts across 6 services in one window
- **Session isolation** — each account has its own cookies and storage; no cross-account leakage
- **Persistent sessions** — close and reopen without re-logging in
- **Crash recovery** — automatic save with restore-on-crash prompt
- **Smart memory management** — LRU-based WebView lifecycle with configurable background limits
- **Link routing** — internal links open in the right account; external links go to your browser
- **Keyboard shortcuts** — fast account switching, reload, settings access
- **i18n** — Japanese and English with OS locale detection
- **Encrypted local storage** — SQLCipher database with OS keychain key management
- **Auto-update** — background update checks via Sparkle / WinSparkle

## Requirements

- **macOS** 12+ or **Windows** 10+
- **JDK 25** (for building from source)

## Building from Source

```bash
# Clone the repository
git clone https://github.com/bright-room/uniso.git
cd uniso

# Run the application
./gradlew run

# Run tests
./gradlew check

# Package for macOS (DMG)
./gradlew packageDmg

# Package for Windows (MSI)
./gradlew packageMsi
```

## Project Structure

```
uniso/
├── shared/                  # Kotlin Multiplatform shared module
│   ├── commonMain/          #   Platform-independent code
│   │   ├── data/            #     Data models, repositories, DB schema
│   │   ├── domain/          #     Business logic (accounts, sessions, links, settings)
│   │   └── ui/              #     Compose UI (sidebar, dialogs, settings, onboarding)
│   ├── jvmMain/             #   JVM/Desktop specific code
│   │   ├── domain/          #     SessionManager, AppInitializer
│   │   └── ui/webview/      #     WebView lifecycle management
│   ├── commonTest/          #   Platform-independent tests
│   └── jvmTest/             #   JVM tests (unit, integration, performance)
├── desktopApp/              # Compose Desktop entry point & packaging config
├── gradle/                  # Version catalog & build scripts
└── .github/workflows/       # CI/CD pipelines
```

## Tech Stack

- **Kotlin** 2.3 + **Compose Multiplatform** 1.10
- **KCEF** (compose-webview-multiplatform) for embedded browser
- **SQLDelight** + **SQLCipher** for encrypted local persistence
- **Sparkle** / **WinSparkle** for auto-updates

## Contributing

We welcome contributions! Please read our [Contributing Guide](CONTRIBUTING.md)
to get started.

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code.

## License

This project is licensed under the **GNU General Public License v3.0**. See the
[LICENSE](LICENSE) file for details.
