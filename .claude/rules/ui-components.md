---
paths:
  - "packages/ui/**/*.tsx"
  - "packages/ui/**/*.css"
---

# UI Component Rules

- Use CSS Modules (`.module.css`) for styling. Do not use inline styles or CSS-in-JS.
- Components are organized into `features/` (domain-specific) and `primitives/` (reusable base components).
- `@uniso/ui` has no runtime dependency on Electron APIs. It must remain a pure React component library.
