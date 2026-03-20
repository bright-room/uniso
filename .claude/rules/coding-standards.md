---
paths:
  - "**/*.ts"
  - "**/*.tsx"
---

# Coding Standards

- Use TypeScript strict mode. Avoid `any` — prefer `unknown` with type narrowing.
- Formatting is handled by Biome. Do not manually adjust formatting.
  - Single quotes, trailing commas, no semicolons, 2-space indent, 100 char line width.
- Use named exports. Avoid default exports.
- All user-facing strings must go through the i18n system — never hardcode display text.
