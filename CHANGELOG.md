# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - Unreleased

### Added

- **Multi-service dashboard**: Manage X, Instagram, Facebook, YouTube, Bluesky, and Twitch accounts in a single window
- **Session isolation**: Each account runs in an independent browser context with separate cookies and storage — no cross-account data leakage
- **Same-service multi-account**: Log in to multiple accounts on the same service simultaneously
- **Sidebar navigation**: Quick account switching via sidebar with service-grouped icons and keyboard shortcuts (Ctrl+Tab / Ctrl+Shift+Tab)
- **Session persistence**: Automatic session save/restore — close the app and resume exactly where you left off
- **Crash recovery**: Detect abnormal shutdown and offer to restore or start fresh
- **Smart memory management**: Background WebViews are kept alive for quick switching, with LRU eviction and timeout-based cleanup to limit memory usage
- **Link routing**: Internal links open in the appropriate account view; external links open in the OS default browser
- **Settings screen**: Language selection (Japanese / English), account management (reorder, rename, delete), privacy controls, and keyboard shortcut reference
- **Onboarding tutorial**: First-launch walkthrough introducing key features
- **Auto-update**: Background update checks with Sparkle (macOS) / WinSparkle (Windows)
- **Keyboard shortcuts**: Ctrl+N (add account), Ctrl+Tab (next), Ctrl+Shift+Tab (previous), Cmd/Ctrl+R (reload), Cmd/Ctrl+, (settings), Cmd/Ctrl+W (close)
- **i18n**: Full Japanese and English localization with OS locale auto-detection
- **Encrypted storage**: Local database encrypted with SQLCipher, keys stored in OS keychain (macOS Keychain / Windows Credential Manager)
- **Cross-platform packaging**: DMG (macOS) and MSI (Windows) installers with code signing and notarization
- **CI/CD**: Automated build, test, lint (ktlint/Spotless), and release pipeline via GitHub Actions
