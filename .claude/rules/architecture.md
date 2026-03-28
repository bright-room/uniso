---
paths:
  - "**/*.ts"
  - "**/*.tsx"
---

# Architecture

Unified desktop client for managing multiple SNS accounts. Built with Electron + React + TypeScript as a pnpm monorepo.

## Main Process (`apps/desktop/src/main/`)

- **index.ts**: Electron lifecycle, database init (sql.js), domain layer wiring, IPC handlers, keyboard shortcuts (Cmd+Tab, Cmd+N, Cmd+W, Cmd+,, Cmd+R)
- **webview-manager.ts**: Creates isolated `WebContentsView` per account using Electron `persist:` partitions for session isolation. Handles fingerprint masking, link routing, LRU eviction, error pages, dialog overlay management
- **ipc-handlers.ts**: Typed IPC channels between main and renderer
- **session-setup.ts**: User-Agent and fingerprint masking
- **auto-updater.ts**: electron-updater integration

## Domain Layer (`packages/shared/src/domain/`)

- **AccountManager**: Account CRUD, active account state, observer pattern for UI updates
- **SessionManager**: Periodic auto-save (30s), crash recovery, clean shutdown tracking
- **LinkRouter**: Classifies links as external/internal/same-domain, routes to correct account
- **I18nManager**: Locale detection, language switching (ja/en)
- **IdentityManager**: Local user UUID generation
- **ServicePluginRegistry**: Service metadata (brand colors, domains, icons) for 6 SNS services

## Data Layer (`packages/shared/src/data/`)

- sql.js (WASM) for local SQLite storage, persisted to `{userData}/uniso.db`
- Tables: `local_user`, `service_plugin`, `account`, `account_state`, `app_state`, `settings`
- Repositories: AccountRepository, SessionRepository, SettingsRepository, ServicePluginRepository

## UI Layer (`packages/ui/`)

- Pure React component library -- no Electron API dependency
- CSS Modules for styling
- `features/`: sidebar, dialogs (add/delete/select account, crash recovery, telemetry, tutorial), settings, onboarding, header, content placeholder
- `primitives/`: DialogOverlay, ServiceIcon, ToggleSwitch
- `theme/`: Design tokens, CSS variables, ThemeContext (light/dark)

## Renderer (`apps/desktop/src/renderer/`)

- React app consuming `@uniso/ui` components
- Hooks: `useAccounts`, `useI18n`, `useCurrentUrl`
- Dialog state management, IPC subscription via preload API

## Supported Services

X (Twitter), Instagram, Facebook, YouTube, Bluesky, Twitch
