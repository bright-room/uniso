# Uniso

Unified desktop client for managing multiple SNS accounts. Built with Electron + React + TypeScript as a pnpm monorepo.

## Project Structure

```
apps/desktop/        → Electron app (main process + React renderer)
packages/shared/     → Shared types and business logic (@uniso/shared)
packages/ui/         → React component library with Storybook (@uniso/ui)
```

## Getting Started

```bash
pnpm install          # Install dependencies
pnpm dev              # Start Electron dev mode
```

## Key Commands

```bash
pnpm lint             # Biome linter
pnpm lint:fix         # Auto-fix lint issues
pnpm typecheck        # TypeScript type checking (all packages)
pnpm test             # Run tests (Vitest)
pnpm storybook        # UI component development (port 6006)
pnpm package:mac      # Build macOS DMG
pnpm package:win      # Build Windows installer
```

## Architecture

- **Main process** (`apps/desktop/src/main/`): Electron lifecycle, IPC handlers, session management, webview management
- **Renderer** (`apps/desktop/src/renderer/`): React app with hooks for accounts, i18n, URL handling
- **Shared** (`packages/shared/`): Type definitions (accounts, settings, i18n, app state), no runtime dependencies
- **UI** (`packages/ui/`): Presentational React components using CSS Modules, organized into `features/` and `primitives/`

## CI/CD

- PR checks: Biome lint → TypeScript type check (GitHub Actions)
- Release: Tag push (`v*`) triggers parallel macOS + Windows builds with code signing
- Manual release: Workflow dispatch with semver validation
