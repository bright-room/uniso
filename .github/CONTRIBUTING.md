# Contributing to Uniso

Thank you for your interest in contributing to Uniso! This guide will help you
get started.

## Getting Started

### Prerequisites

- **Node.js 24+** (recommended via [mise](https://mise.jdx.dev/))
- **pnpm 9+**
- **macOS 12+** or **Windows 10+**
- **Git**

### Setting Up the Development Environment

```bash
# Fork and clone the repository
git clone https://github.com/<your-username>/uniso.git
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
```

### Useful Commands

```bash
pnpm dev           # Start Electron in development mode
pnpm build         # Build all packages
pnpm lint          # Run Biome linter
pnpm lint:fix      # Auto-fix lint issues
pnpm format        # Format code with Biome
pnpm typecheck     # Type check all packages
pnpm test          # Run all tests
pnpm test:watch    # Run tests in watch mode
pnpm storybook     # Start Storybook for UI development
pnpm package:mac   # Package macOS DMG
pnpm package:win   # Package Windows installer
```

## How to Contribute

### Reporting Bugs

1. Search [existing issues](https://github.com/bright-room/uniso/issues) to
   avoid duplicates.
2. Open a new issue using the bug report template.
3. Include steps to reproduce, expected behavior, actual behavior, and your
   environment (OS, version, etc.).

### Suggesting Features

1. Search existing issues and discussions first.
2. Open a new issue describing the feature, its use case, and why it would be
   valuable.

### Submitting Code Changes

1. **Fork** the repository and create a feature branch from `main`:

   ```bash
   git checkout -b feat/your-feature-name
   ```

2. **Make your changes.** Follow the coding conventions below.

3. **Write tests** for new functionality or bug fixes.

4. **Run checks** to make sure everything passes:

   ```bash
   pnpm lint && pnpm typecheck && pnpm test
   ```

5. **Commit** with a clear, descriptive message:

   ```bash
   git commit -m "Add feature X for Y"
   ```

6. **Push** your branch and open a **Pull Request** against `main`.

### Pull Request Guidelines

- Keep PRs focused — one feature or fix per PR.
- Provide a clear description of what the PR does and why.
- Link related issues (e.g., `Closes #42`).
- Ensure all CI checks pass before requesting review.
- Be responsive to review feedback.

## Coding Conventions

- **Language:** TypeScript (strict mode)
- **Formatting:** Biome handles formatting and linting. Run `pnpm lint:fix` before committing.
- **Naming:**
  - Components / Types / Interfaces: `PascalCase`
  - Functions / variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Styling:** CSS Modules (`.module.css`)
- **i18n:** All user-facing strings must go through the i18n system. Do not
  hardcode display text.
- **Tests:** Add tests for shared logic. Use Vitest.

## Branch Naming

| Type       | Pattern                      | Example                        |
|------------|------------------------------|--------------------------------|
| Feature    | `feat/short-description`     | `feat/sidebar-drag-drop`       |
| Bug fix    | `fix/short-description`      | `fix/session-restore-crash`    |
| Refactor   | `refactor/short-description` | `refactor/webview-lifecycle`   |
| Docs       | `docs/short-description`     | `docs/update-readme`           |

## License

By contributing to Uniso, you agree that your contributions will be licensed
under the [MIT License](../LICENSE).

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md). We are
committed to providing a welcoming and inclusive experience for everyone.
