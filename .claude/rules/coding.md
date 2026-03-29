---
paths:
  - "**/*.ts"
  - "**/*.tsx"
  - "**/*.css"
---

# Coding Standards

- Use TypeScript strict mode. Avoid `any` -- prefer `unknown` with type narrowing.
- Formatting is handled by Biome. Do not manually adjust formatting.
  - Single quotes, trailing commas, no semicolons, 2-space indent, 100 char line width.
- Use named exports. Avoid default exports.
- All user-facing strings must go through the i18n system -- never hardcode display text.

## Electron Main Process (`apps/desktop/src/main/`)

- IPC communication between main and renderer must use typed channels defined in `@uniso/shared`.
- Never import renderer-side code (React, DOM APIs) in the main process.
- Use `electron-log` for logging, not `console.log`.

## Shared Package (`packages/shared/`)

- `@uniso/shared` has no runtime dependency on Electron or React. It must remain framework-agnostic.
- Domain logic lives in `src/domain/`, data access in `src/data/`, types in `src/types/`.
- Unit tests use Vitest and live in `src/__tests__/`. Use `test-database.ts` helper for in-memory SQLite setup.
- i18n string resources are JSON files in `src/i18n/` (en.json, ja.json). All i18n keys must exist in both files.

## UI Components (`packages/ui/`)

- Use CSS Modules (`.module.css`) for styling. Do not use inline styles or CSS-in-JS.
- Components are organized into `features/` (domain-specific) and `primitives/` (reusable base components).
- `@uniso/ui` has no runtime dependency on Electron APIs. It must remain a pure React component library.
