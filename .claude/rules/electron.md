---
paths:
  - "apps/desktop/src/main/**/*.ts"
---

# Electron Main Process Rules

- IPC communication between main and renderer must use typed channels defined in `@uniso/shared`.
- Never import renderer-side code (React, DOM APIs) in the main process.
- Use `electron-log` for logging, not `console.log`.
