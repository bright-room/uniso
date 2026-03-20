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
- **Encrypted local storage** — SQLite database with OS keychain key management
- **Auto-update** — background update checks via electron-updater

## Requirements

- **macOS** 12+ or **Windows** 10+
- **Node.js** 24+ and **pnpm** 9+ (for building from source)

## Building from Source

```bash
# Clone the repository
git clone https://github.com/bright-room/uniso.git
cd uniso

# Install dependencies
pnpm install

# Run the application in development mode
pnpm dev

# Run linter
pnpm lint

# Run type checking
pnpm typecheck

# Run tests
pnpm test

# Package for macOS (DMG)
pnpm package:mac

# Package for Windows (NSIS installer)
pnpm package:win
```

## Project Structure

```
uniso/
├── apps/
│   └── desktop/              # Electron main app
│       ├── src/main/          #   Main process (IPC, session, webview)
│       └── src/renderer/      #   Renderer process (React)
├── packages/
│   ├── shared/                # Shared types and business logic
│   │   └── src/types/         #   Account, settings, i18n types
│   └── ui/                    # React component library
│       └── src/               #   Sidebar, settings, theme
├── .github/
│   ├── workflows/             # CI/CD pipelines
│   └── actions/               # Reusable composite actions
├── biome.json                 # Linter & formatter config
├── pnpm-workspace.yaml        # Monorepo workspace definition
└── tsconfig.base.json         # Shared TypeScript config
```

## Tech Stack

- **Electron** 41 — desktop runtime
- **React** 19 — UI framework
- **TypeScript** 5.9 — language
- **Vite** 8 + **electron-vite** — build tooling
- **sql.js** — SQLite in the renderer process
- **Biome** — linter and formatter
- **Storybook** 10 — UI component development
- **electron-builder** — native packaging (DMG, NSIS)
- **electron-updater** — auto-update

## Contributing

We welcome contributions! Please read our [Contributing Guide](.github/CONTRIBUTING.md)
to get started.

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](.github/CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code.

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE)
file for details.
