# Contributing to Uniso

Thank you for your interest in contributing to Uniso! This guide will help you
get started.

## Getting Started

### Prerequisites

- **JDK 25** (Amazon Corretto recommended)
- **macOS 12+** or **Windows 10+**
- **Git**

### Setting Up the Development Environment

```bash
# Fork and clone the repository
git clone https://github.com/<your-username>/uniso.git
cd uniso

# Run the application
./gradlew run

# Run tests
./gradlew check
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

4. **Run tests** to make sure everything passes:

   ```bash
   ./gradlew check
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

- **Language:** Kotlin
- **Formatting:** Follow the project's existing code style. Use the default
  Kotlin coding conventions.
- **Naming:**
  - Classes: `PascalCase`
  - Functions/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Architecture:** Follow the existing 3-layer architecture (UI, Domain, Data).
  See `shared/` module structure.
- **i18n:** All user-facing strings must go through the i18n system. Do not
  hardcode display text.
- **Tests:** Add unit tests for domain logic. Use JUnit 5 + Kotlin Test.

## Branch Naming

| Type       | Pattern                      | Example                        |
|------------|------------------------------|--------------------------------|
| Feature    | `feat/short-description`     | `feat/sidebar-drag-drop`       |
| Bug fix    | `fix/short-description`      | `fix/session-restore-crash`    |
| Refactor   | `refactor/short-description` | `refactor/webview-lifecycle`   |
| Docs       | `docs/short-description`     | `docs/update-readme`           |

## License

By contributing to Uniso, you agree that your contributions will be licensed
under the [GNU General Public License v3.0](LICENSE).

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md). We are
committed to providing a welcoming and inclusive experience for everyone.
