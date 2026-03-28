---
name: review
description: PR 番号を指定してコードレビューを実施し、インラインコメントとして投稿する。コードベース全体のレビューも可能（ローカル出力）。
argument-hint: "<pr-number> [--category <category>] [--full]"
---

# Code Review Skill

PR に対してコードレビューを実施し、指摘事項をインラインコメントとして GitHub 上に投稿する。コードベース全体レビューの場合はローカルファイルとして出力する。

## レビュー方針

- 行番号と**文脈情報（背景知識など）**を必ず含める
- レビュー結果はチームメンバーに共有されるため、細部まで入念に確認すること
- 書き始める前に深く考察し、すべての箇所を漏れなくチェックすること
- 事実に基づかない内容（ハルシネーション）を含めないこと

## 前提条件

- `gh` CLI が認証済みであること

## レビューモードの解決ルール

引数に応じて、レビューモードを以下の優先順で決定する:

| 条件 | モード | レビュー対象 | 出力先 |
|------|--------|-------------|--------|
| PR 番号を指定（数値のみ） | **PR レビュー** | PR の差分 | GitHub インラインコメント |
| `--full-codebase` 指定 | **コードベース全体レビュー** | main ブランチの全コード | ローカルファイル |

PR 番号が指定された場合は必ず PR レビューモードとなる。

## カテゴリ選択ルール

レビューするカテゴリを以下の優先順で決定する:

| 条件 | カテゴリ |
|------|---------|
| `--full` 指定 | 全6カテゴリ |
| `--category` 指定 | 指定されたカテゴリのみ |
| コードベース全体レビュー | 全6カテゴリ |
| PR レビュー（カテゴリ指定なし） | **変更ファイルから自動選択**（後述） |

### 変更ファイルからのカテゴリ自動選択（PR レビュー時のデフォルト）

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
| `*.md`, `CLAUDE.md`, `README.md`（`.claude/` 配下を除く） | ドキュメント |
| `.claude/skills/`, `.claude/rules/` | ドキュメント |

- 1つのファイルが複数カテゴリにマッチする場合はすべて選択する
- **プロダクトコード**は PR レビュー時に常に含める（最低1カテゴリ）

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

#### モード A: PR レビュー

引数に PR 番号が指定された場合。

##### 1-1. PR 情報の取得

```bash
# PR のベースブランチと最新コミット SHA を取得
gh pr view <pr-number> --json baseRefName,headRefOid,headRefName

# PR で変更されたファイル一覧を取得
gh api repos/{owner}/{repo}/pulls/<pr-number>/files --jq '.[].filename'
```

##### 1-2. レビュー済み判定と差分取得

前回レビュー以降の変更分のみをレビュー対象にする。

```bash
# 過去のレビュー一覧を取得
gh api repos/{owner}/{repo}/pulls/<pr-number>/reviews
```

- 自身（claude）の最新レビューの `commit_id`（レビュー時点の HEAD SHA）を特定する
- 自身のレビューが見つかった場合: `git diff <前回レビューSHA>..<現在のHEAD SHA>` で新規変更分のみを差分として取得する
- 自身のレビューが見つからない場合（初回レビュー）: ベースブランチからの全差分を対象とする

##### 1-3. Resolved 済みスレッドの除外

GraphQL API で Resolved 済みのレビュースレッドを取得し、既に解決済みの指摘を再度行わないようにする。

```bash
gh api graphql -f query='
{
  repository(owner: "{owner}", name: "{repo}") {
    pullRequest(number: <pr-number>) {
      reviewThreads(first: 100) {
        nodes {
          isResolved
          comments(first: 10) {
            nodes { body, path, line }
          }
        }
      }
    }
  }
}'
```

- `isResolved: true` のスレッドに含まれる指摘は、同じ内容を再度指摘しない
- bot コメント（CI 等）も除外する

#### モード B: コードベース全体レビュー（`--full-codebase` 指定）

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

#### モード A: PR レビュー — GitHub インラインコメントとして投稿

指摘事項を GitHub PR レビューとして1回の API コールで投稿する。

```bash
gh api repos/{owner}/{repo}/pulls/<pr-number>/reviews \
  --method POST \
  --input /tmp/review-payload.json
```

レビューペイロードの構造:

```json
{
  "commit_id": "<PR の最新コミット SHA>",
  "body": "<!-- claude:review -->\n<レビュー総括コメント>",
  "event": "COMMENT",
  "comments": [
    {
      "path": "packages/shared/src/domain/AccountManager.ts",
      "line": 42,
      "side": "RIGHT",
      "body": "**[Critical]** <指摘内容>\n\n**背景**: <なぜ問題なのか>\n\n**修正案**:\n```typescript\n// 修正コード\n```"
    }
  ]
}
```

##### event の選択基準

| 条件 | event |
|------|-------|
| Critical の指摘がある | `REQUEST_CHANGES` |
| High 以下の指摘のみ | `COMMENT` |
| 指摘なし | `APPROVE` |

##### 総括コメントのフォーマット

```markdown
<!-- claude:review -->
## Code Review 総括

> レビュー日: YYYY-MM-DD
> レビューカテゴリ: <選択されたカテゴリ一覧>

### 総合評価

| 観点 | 評価 |
|------|------|
| <カテゴリ名> | ⭐⭐⭐⭐☆ |

### 指摘サマリ

| # | 優先度 | カテゴリ | 概要 |
|---|--------|----------|------|
| 1 | 🔴 Critical | <カテゴリ> | <概要> |
| 2 | 🟠 High | <カテゴリ> | <概要> |

---
🤖 *Reviewed by Claude Code*
```

##### インラインコメントの記述ルール

- 各コメントの先頭に優先度バッジを付ける: `**[Critical]**`, `**[High]**`, `**[Medium]**`, `**[Low]**`
- 背景（なぜ問題なのか）を必ず含める
- 修正案がある場合はコード例を含める
- `commit_id` には `gh pr view <pr-number> --json headRefOid --jq .headRefOid` で取得した最新コミット SHA を指定すること（行番号のズレを防ぐため）

##### API エラー時のフォールバック

API 呼び出しが失敗した場合は、ローカルファイルにフォールバック出力する。

- 出力先: `.claude/outputs/reviews/REVIEW-PR-<pr-number>.md`
- ユーザーに API エラーが発生した旨を報告する

#### モード B: コードベース全体レビュー — ローカルファイルとして出力

レビュー結果を `.claude/outputs/reviews/` ディレクトリにファイルとして出力する。

- ディレクトリが存在しない場合は作成すること
- ファイル名: `REVIEW-main-YYYY-MM-DD.md`

以下のフォーマットに従ってレビュー結果を生成する。**選択されたカテゴリのセクションのみ出力する。**

````markdown
# Code Review: main

> レビュー日: YYYY-MM-DD
> レビューカテゴリ: <選択されたカテゴリ一覧>

## 総合評価

| 観点 | 評価 |
|------|------|
| <カテゴリ名> | ⭐⭐⭐⭐☆ |

---

## <カテゴリ名>レビュー

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

- 推測ではな��、実際にコードを読んで確認した事実のみを記載すること
- 良い点（Good practices）があれば、総括コメントで簡潔に言及すること
- セキュリティレビューでは、推測による脆弱性指摘は行わず、コード上で確認できる事実のみを記載すること
- ドキュメントレビューでは、ドキュメントの「有無」だけでなく「内容の正確性」も確認すること
- Resolved 済みのスレッドと同じ内容の指摘は行わないこと
