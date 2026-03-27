---
name: review
description: コードレビューを実施する。ブランチ差分または main 全体のレビューを行い、ローカルファイルとして出力する。
argument-hint: "[base-branch] [--category <category>] [--full] [--full-codebase]"
---

# Code Review Skill

コードレビューを実施し、構造化された Markdown レポートとして出力すること。

## レビュー方針

- 行番号と**文脈情報（背景知識など）**を必ず含める
- レビュー結果はチームメンバーに共有されるため、細部まで入念に確認すること
- 書き始める前に深く考察し、すべての箇所を漏れなくチェックすること
- 事実に基づかない内容（ハルシネーション）を含めないこと

## レビューモードの解決ルール

現在のブランチと引数に応じて、レビューモードを以下の優先順で決定する:

| 条件 | モード | レビュー対象 |
|------|--------|-------------|
| `--full-codebase` 指定 | **コードベース全体レビュー** | main ブランチの全コード |
| 現在のブランチが `main` 以外 + 引数なし | **main との差分レビュー** | `main` との差分 |
| 現在のブランチが `main` 以外 + ブランチ指定 | **指定ブランチとの差分レビュー** | 指定ブランチとの差分 |
| 現在のブランチが `main` + 引数なし | **コードベース全体レビュー** | main ブランチの全コード |

## カテゴリ選択ルール

レビューするカテゴリを以下の優先順で決定する:

| 条件 | カテゴリ |
|------|---------|
| `--full` 指定 | 全6カテゴリ |
| `--category` 指定 | 指定されたカテゴリのみ |
| コードベース全体レビュー | 全6カテゴリ |
| 差分レビュー（カテゴリ指定なし） | **変更ファイルから自動選択**（後述） |

### 変更ファイルからのカテゴリ自動選択（差分レビュー時のデフォルト）

変更されたファイルのパスに基づいて、レビューするカテゴリを自動で絞り込む。

| 変更ファイルのパスパターン | 選択されるカテゴリ |
|--------------------------|-------------------|
| `apps/desktop/src/main/` | アーキテクチャ, セキュリティ, プロダクトコード |
| `apps/desktop/src/renderer/`, `apps/desktop/src/preload/` | プロダクトコード |
| `packages/shared/src/domain/` | アーキテクチャ, プロダクトコード |
| `packages/shared/src/data/` | プロダクトコード, セキュリティ |
| `packages/ui/src/` | プロダクトコード |
| `**/*.test.ts`, `**/*.spec.ts`, `**/e2e/`, `**/*.stories.tsx` | テストコード |
| `package.json`, `tsconfig*.json`, `biome.json`, `electron-builder.yml`, `vite.config.*` | ビルド・設定 |
| `*.md`, `CLAUDE.md`, `README.md` | ドキュメント |

- 1つのファイルが複数カテゴリにマッチする場合はすべて選択する
- **プロダクトコード**は差分レビュー時に常に含める（最低1カテゴリ）

### 使用可能なカテゴリ名

`--category` で指定可能な値（複数指定はカンマ区切り: `--category security,code`）:

| カテゴリ名 | 説明 |
|-----------|------|
| `architecture` | モノレポ構成の責務分離、パッケージ間の依存関係、Electron メイン/レンダラープロセス分離 |
| `code` | ロジックの正確性、エッジケース、命名規則、TypeScript の型安全性 |
| `test` | テストカバレッジ、テストケースの網羅性、テストの品質（Vitest / Playwright / Storybook） |
| `security` | Electron セキュリティ、IPC 安全性、WebView 分離、sql.js データ保護 |
| `docs` | TSDoc、README、i18n 対応 |
| `build` | package.json、tsconfig、Biome、electron-builder 設定 |

## 手順

### 1. レビュー対象の特定

まず現在のブランチを確認する:
```bash
git branch --show-current
```

#### モード A: 差分レビュー

現在のブランチが `main` 以外の場合のデフォルト動作。ベースブランチは引数で指定するか、未指定なら `main` を使用する。

```bash
BASE_BRANCH="${ARGUMENTS:-main}"  # 引数なしなら main
git diff ${BASE_BRANCH}...HEAD --name-only
git diff ${BASE_BRANCH}...HEAD
git log ${BASE_BRANCH}...HEAD --oneline
```

#### モード B: コードベース全体レビュー（`--full-codebase` 指定、または main ブランチ上で引数なし）

- リポジトリ内のすべての TypeScript/TSX ソースファイル（`*.ts`, `*.tsx`）を読み込む
- テストファイル、設定ファイル（`package.json`、`tsconfig.*.json`、`biome.json` 等）、ドキュメントも対象とする
- `node_modules/`、`dist/`、`out/` ディレクトリはレビュー対象外とする

### 2. カテゴリの決定

引数とモードに基づいて、レビューするカテゴリを決定する（「カテゴリ選択ルール」参照）。

### 3. コードの読解

レビュー対象のファイルをすべて読み込み、内容を深く理解する。対象ファイルだけでなく、関連するファイル（呼び出し元、型定義、IPC チャネル、CSS Modules など）も確認すること。

### 4. レビューの実施

選択されたカテゴリについてのみレビューを行う。各カテゴリの観点:

#### architecture（アーキテクチャ）
- モノレポ構成の責務分離（shared: フレームワーク非依存 / ui: 純粋 React / desktop: Electron 固有）
- パッケージ間の依存関係の正当性
- Electron メイン/レンダラープロセス分離
- ドメインマネージャー（AccountManager, SessionManager, LinkRouter, I18nManager, ServicePluginRegistry）の設計

#### code（プロダクトコード）
- ロジックの正確性、エッジケース
- 命名規則（Biome: single quotes, no semicolons, 2-space indent）
- TypeScript の型安全性（strict モード前提）
- Observer パターン、Repository パターンの適切な使用

#### test（テストコード）
- テストカバレッジ、テストケースの網羅性
- Vitest ユニットテスト / Playwright E2E テスト / Storybook の品質
- テストの独立性と再現性

#### security（セキュリティ）
- Electron セキュリティ: contextIsolation、nodeIntegration の設定
- IPC セキュリティ: チャネルの型安全性、メインプロセスでのバリデーション
- WebView セッション分離: `persist:` パーティション、LRU eviction、フィンガープリントマスキング
- sql.js / WASM: データベース初期化パターン、パストラバーサル防止
- 入力バリデーション: XSS、コマンドインジェクション対策
- 機密情報管理: ハードコーディング、ログ出力

#### docs（ドキュメント）
- TSDoc: 公開 API のドキュメント
- README / CLAUDE.md の更新
- i18n: 新しい UI テキストの国際化対応

#### build（ビルド・設定）
- package.json、tsconfig、Biome 設定の正確性
- electron-builder、electron-vite 設定

### 5. レビュー結果の出力

以下のフォーマットに従ってレビュー結果を生成する。**選択されたカテゴリのセクションのみ出力する。**

レビュー結果を `.claude/outputs/reviews/` ディレクトリにファイルとして出力する。

- ディレクトリが存在しない場合は作成すること
- ファイル名:
  - モード A: `REVIEW-<現在のブランチ名（スラッシュをハイフンに変換）>.md`
    - 例: ブランチ `feat/42-add-threads-support` → `REVIEW-feat-42-add-threads-support.md`
  - モード B: `REVIEW-main-YYYY-MM-DD.md`
    - 例: `REVIEW-main-2026-03-28.md`

## レビュー結果フォーマット

````markdown
# Code Review: <ブランチ名>

> レビュー日: YYYY-MM-DD
> ベースブランチ: `<base-branch>`
> 対象ブランチ: `<current-branch>`
> レビュー対象コミット: <コミットハッシュ一覧>
> レビューカテゴリ: <選択されたカテゴリ一覧>

## 総合評価

<!-- 選択されたカテゴリのみ評価行を出力 -->

| 観点 | 評価 |
|------|------|
| <カテゴリ名> | ⭐⭐⭐⭐☆ |

---

## レビューサマリ

### 指摘事項一覧

| # | 優先度 | カテゴリ | タイトル | 概要 |
|---|--------|----------|----------|------|
| C-1 | 🔴 Critical | <カテゴリ> | <タイトル> | <概要> |
| H-1 | 🟠 High | <カテゴリ> | <タイトル> | <概要> |

### 対応チェックリスト

- [ ] C-1: <タイトル>
- [ ] H-1: <タイトル>

---

## <カテゴリ名>レビュー

<!-- 選択されたカテゴリのセクションのみ出力。指摘がないセクションは「指摘なし」と記載 -->

### 🔴 Critical

#### C-1: <問題のタイトル>

**問題点**

> 📍 [`path/to/file.ts:42`](path/to/file.ts#L42)
> ```typescript
> // 該当コードの引用
> ```

**背景**

<!-- なぜこれが問題なのか、技術的な文脈情報 -->

**修正案**

```typescript
// 修正後のコード例
```

##### C-1-1: <関連する修正すべき詳細>

> 📍 [`path/to/file.ts:55`](path/to/file.ts#L55)

---

### 🟠 High
### 🟡 Medium
### 🟢 Low
````

### セキュリティチェックリスト（security カテゴリ選択時のみ出力）

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

### テストカバレッジマトリクス（test カテゴリ選択時のみ出力）

| 対象パッケージ | 関数/コンポーネント | Vitest | Playwright | Storybook | 備考 |
|--------------|-------------------|:------:|:----------:|:---------:|------|
| `packages/shared` | `ClassName.method()` | ✅ | ✅ | N/A | |
| `packages/ui` | `ComponentName` | ✅ | ❌ | ✅ | |

## 指摘の記述ルール

- 指摘がない優先度セクションは「指摘なし」と記載してスキップすること
- 項番はレビュー全体で一意になるよう、優先度プレフィックス（C/H/M/L）ごとにグローバル通し番号を付与すること（カテゴリをまたいでも重複させない）
- コード参照は相対パスで記載し、行番号を含めること
- 推測ではなく、実際にコードを読んで確認した事実のみを記載すること
- 良い点（Good practices）があれば、各セクションの冒頭で簡潔に言及すること
- セキュリティレビューでは、推測による脆弱性指摘は行わず、コード上で確認できる事実のみを記載すること
- ドキュメントレビューでは、ドキュメントの「有無」だけでなく「内容の正確性」も確認すること
