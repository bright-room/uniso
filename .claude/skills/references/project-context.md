# Project Context

## コードベース調査ガイド

### モジュール構成の把握方法

- `pnpm-workspace.yaml` でワークスペースパッケージ構成を確認する
- 3パッケージ構成:
  - `apps/desktop` — Electron アプリ（メインプロセス + React レンダラー）
  - `packages/shared` — フレームワーク非依存のビジネスロジック
  - `packages/ui` — 純粋 React コンポーネントライブラリ

### 既存パターンの調査手順

- **ドメインマネージャー**: AccountManager, SessionManager, LinkRouter, I18nManager, IdentityManager, ServicePluginRegistry のパターンを確認する
- **データ層**: AccountRepository, SessionRepository, SettingsRepository, ServicePluginRepository のパターンを確認する
- **IPC チャネル**: `apps/desktop/src/main/ipc-handlers.ts` の型付き IPC パターンを確認する
- **React コンポーネント**: `packages/ui/src/features/`、`packages/ui/src/primitives/` のパターンを確認する
- **新しい SNS サービス追加の場合**: ServicePluginRegistry のメタデータ拡張パターンを確認する

### テスト構成の確認方法

- ユニットテスト: Vitest（`pnpm test`）
- E2E テスト: Playwright（`pnpm test:e2e`）
- UI 開発: Storybook（`pnpm storybook`）
- テストヘルパー: `packages/shared/src/__tests__/test-database.ts`（インメモリ SQLite セットアップ）

## 実装ガイド

### ビルド・フォーマットコマンド

```bash
# Biome による lint + auto-fix
pnpm lint:fix

# TypeScript 型チェック（全パッケージ）
pnpm typecheck

# ユニットテスト
pnpm test
```

**注意**: `pnpm lint:fix` は必ずコミット前に実行すること。

### 言語固有の実装規約

- 新規 UI コンポーネントには **Storybook Story** (`.stories.tsx`) も作成すること。既存の Story ファイルのパターンに合わせること
- 新しい SNS サービスを追加する場合は、`ServicePluginRegistry` のパターンに従い、`packages/shared/src/domain/ServicePluginRegistry` にサービスメタデータ（ブランドカラー、ドメイン、アイコン）を追加し、関連する UI コンポーネント（`ServiceIcon` 等）を更新すること
- 新しい UI テキストがある場合は `packages/shared/src/i18n/` のリソースファイルに日本語・英語の翻訳を追加すること

### テスト配置ルール

- ユニットテスト: `packages/shared/src/__tests__/` に配置。`test-database.ts` ヘルパーでインメモリ SQLite をセットアップ
- E2E テスト: `apps/desktop/e2e/` に配置
- Storybook: `packages/ui/src/` 内の各コンポーネントと同階層に `.stories.tsx` として配置

### 実装順序

モノレポの依存関係に従い、以下の順序で実装する:

1. **ドメインロジック**: `packages/shared` の新規・修正
2. **UI コンポーネント**: `packages/ui` の新規・修正（+ Storybook Story）
3. **Electron 統合**: `apps/desktop` のメインプロセス、IPC、レンダラー
4. **テスト**: Vitest ユニットテスト、Playwright E2E テスト
5. **i18n リソース**: `packages/shared/src/i18n/` の en.json / ja.json に翻訳追加
6. **ドキュメント**: README、CLAUDE.md など

### CI に委ねてよい項目

- E2E テスト（Playwright、xvfb 環境が必要）

## レビューガイド

### ファイルパス → カテゴリマッピング

| 変更ファイルのパスパターン | 選択されるカテゴリ |
|--------------------------|-------------------|
| `apps/desktop/src/main/` | アーキテクチャ, セキュリティ, プロダクトコード |
| `apps/desktop/src/renderer/`, `apps/desktop/src/preload/` | プロダクトコード |
| `packages/shared/src/domain/` | アーキテクチャ, プロダクトコード |
| `packages/shared/src/data/` | プロダクトコード, セキュリティ |
| `packages/ui/src/` | プロダクトコード |
| `**/*.test.ts`, `**/*.spec.ts`, `**/e2e/`, `**/*.stories.tsx` | テストコード |
| `package.json`, `tsconfig*.json`, `biome.json`, `electron-builder.yml`, `vite.config.*` | ビルド・設定 |
| `*.md`, `CLAUDE.md`, `README.md`（`.claude/` 配下を除く） | ドキュメント |
| `.claude/skills/`, `.claude/rules/` | ドキュメント |

### カテゴリ別レビュー観点

#### architecture
- モノレポ構成の責務分離（shared: フレームワーク非依存 / ui: 純粋 React / desktop: Electron 固有）
- パッケージ間の依存関係の正当性
- Electron メイン/レンダラープロセス分離
- ドメインマネージャー（AccountManager, SessionManager, LinkRouter, I18nManager, ServicePluginRegistry）の設計

#### code
- ロジックの正確性、エッジケース
- 命名規則（Biome: single quotes, no semicolons, 2-space indent）
- TypeScript の型安全性（strict モード前提）
- Observer パターン、Repository パターンの適切な使用

#### test
- テストカバレッジ、テストケースの網羅性
- Vitest ユニットテスト / Playwright E2E テスト / Storybook の品質
- テストの独立性と再現性

#### security
- Electron セキュリティ: contextIsolation、nodeIntegration の設定
- IPC セキュリティ: チャネルの型安全性、メインプロセスでのバリデーション
- WebView セッション分離: `persist:` パーティション、LRU eviction、フィンガープリントマスキング
- sql.js / WASM: データベース初期化パターン、パストラバーサル防止
- 入力バリデーション: XSS、コマンドインジェクション対策
- 機密情報管理: ハードコーディング、ログ出力

#### docs
- TSDoc: 公開 API のドキュメント
- README / CLAUDE.md の更新
- i18n: 新しい UI テキストの国際化対応

#### build
- package.json、tsconfig、Biome 設定の正確性
- electron-builder、electron-vite 設定

### セキュリティチェックリスト

| チェック項目 | 結果 | 備考 |
|-------------|:----:|------|
| contextIsolation の有効化 | ✅ / ❌ / N/A | |
| nodeIntegration の無効化 | ✅ / ❌ / N/A | |
| IPC チャネルの型安全性 | ✅ / ❌ / N/A | |
| WebView セッション分離 | ✅ / ❌ / N/A | |
| フィンガープリントマスキング | ✅ / ❌ / N/A | |
| sql.js 初期化の安全性 | ✅ / ❌ / N/A | |
| XSS 対策 | ✅ / ❌ / N/A | |
| 機密情報のハードコーディング | ✅ / ❌ / N/A | |
| ローカル DB のパストラバーサル | ✅ / ❌ / N/A | |

### テストカバレッジマトリクス

| 対象パッケージ | 関数/コンポーネント | Vitest | Playwright | Storybook | 備考 |
|--------------|-------------------|:------:|:----------:|:---------:|------|

## プランテンプレート補足

### 影響範囲テーブル

| パッケージ | 影響 | 備考 |
|-----------|------|------|
| `apps/desktop` | 新規 / 変更 / なし | <概要> |
| `packages/shared` | 新規 / 変更 / なし | <概要> |
| `packages/ui` | 新規 / 変更 / なし | <概要> |

### ファイル構成の記述例

```
apps/desktop/src/
├── main/
│   └── new-handler.ts          (new)
├── renderer/
│   └── hooks/useNewFeature.ts  (new)
packages/shared/src/
├── domain/
│   └── NewManager.ts           (new)
packages/ui/src/
├── features/
│   └── new-feature/
│       ├── NewFeature.tsx       (new)
│       └── NewFeature.module.css (new)
```

### テスト戦略テーブル

| テスト種別 | 対象 | テスト内容 |
|-----------|------|-----------|
| ユニットテスト (Vitest) | `packages/shared/src/domain/xxx` | <テスト内容> |
| E2E テスト (Playwright) | `apps/desktop` | <テスト内容> |
| Storybook | `packages/ui/src/features/xxx` | <テスト内容> |

### ドキュメント更新対象

| ドキュメント | 更新条件 |
|-------------|---------|
| `README.md` | 新機能・設定変更 |
| `CLAUDE.md` | コマンド変更・プロジェクト構成変更 |
| `.claude/rules/architecture.md` | ドメインマネージャー追加・パッケージ構成変更・IPC チャネル変更 |
| `.claude/rules/coding.md` | コーディング規約変更 |
| `.claude/skills/references/project-context.md` | パッケージ構成変更・ビルドコマンド変更・レビュー観点変更 |

## ラベル・ワークフロー規約

### Issue/PR ラベルの prefix

`Kind:` prefix を使用する（例: `Kind: Bug Fix`, `Kind: Feature`）。
