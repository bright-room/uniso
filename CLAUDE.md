# Uniso

Unified desktop client for managing multiple SNS accounts. Built with Electron + React + TypeScript as a pnpm monorepo.

## Project Structure

```
apps/desktop/        → Electron app (main process + React renderer) (@uniso/desktop)
packages/shared/     → Shared business logic, repositories, types (@uniso/shared)
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
pnpm test             # Run unit tests (Vitest)
pnpm test:e2e         # Build + run E2E tests (Playwright)
pnpm storybook        # UI component development (port 6006)
pnpm package:mac      # Build macOS DMG
pnpm package:win      # Build Windows installer
pnpm security:check   # Dependency audit + Glassworm detection
```

## Architecture

For main process, domain layer, data layer, UI layer, and renderer details, see @.claude/rules/architecture.md

## Coding Standards

For TypeScript conventions, Electron rules, shared package rules, and UI component rules, see @.claude/rules/coding.md

## CI/CD

- **PR checks** (`ci.yml`): Biome lint → TypeScript typecheck → Vitest unit tests → Playwright E2E (xvfb on Linux)
- **Security** (`security-check.yml`): pnpm audit, anti-trojan-source (Glassworm detection), Aikido Safe Chain
- **Release** (`release.yml`): Tag push (`v*`) → parallel macOS + Windows builds → GitHub Release
- **Manual release** (`release-dispatch.yml`): Workflow dispatch with semver validation

## Tech Stack

| Category | Technology |
|---|---|
| Runtime | Electron 41 |
| UI | React 19 + CSS Modules |
| Language | TypeScript 5.9 (strict) |
| Build | Vite 7 + electron-vite 5 |
| Database | sql.js (WASM SQLite) |
| Linting | Biome (single quotes, no semicolons, 2-space indent, 100 char width) |
| Unit tests | Vitest |
| E2E tests | Playwright |
| UI dev | Storybook 10 |
| Packaging | electron-builder (DMG, NSIS) |
| Auto-update | electron-updater |

## Design Documents

Design documents are located in `.claude/inputs/`:

- `sns_dashboard_requirements.md` — Application requirements (v1.1)
- `sns_dashboard_requirements_definition.md` — Requirements definition (v1.6)
- `SNS統合ダッシュボード_基本設計書_v1.0.md` — Basic design
- `SNS統合ダッシュボード_詳細設計書_v1_0.md` — Detailed design
- `ui_mock_specification.md` — UI mock specification
- `テスト・セキュリティ設計書_v1.0.md` — Test & security design

> **Note**: Requirements definition, basic design, and detailed design documents were written for Kotlin Compose Multiplatform + KCEF. The actual implementation uses Electron + React + TypeScript. Refer to these documents for functional requirements, domain logic, and data design (which remain valid), but not for technology-specific details.
