---
paths:
  - "packages/shared/**/*.ts"
---

# Shared Package Rules

- `@uniso/shared` has no runtime dependency on Electron or React. It must remain framework-agnostic.
- Domain logic lives in `src/domain/`, data access in `src/data/`, types in `src/types/`.
- Unit tests use Vitest and live in `src/__tests__/`. Use `test-database.ts` helper for in-memory SQLite setup.
- i18n string resources are JSON files in `src/i18n/` (en.json, ja.json). All i18n keys must exist in both files.
