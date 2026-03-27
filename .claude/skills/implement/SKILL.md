---
name: implement
description: 指定された Markdown（実装プラン等）を読み込み、内容に基づいてコードを実装する。ローカルではファイルパス指定、CI 環境では Issue コメントの内容を読み取って実装する。
argument-hint: "[markdown-file-path] [--branch <branch-name>]"
---

# Implement Skill

指定されたソース（Markdown ファイルまたは GitHub Issue のコメント）を読み込み、その内容に基づいてコードを実装する。

## 前提条件

- `gh` CLI が認証済みであること
- コーディング規約 `.claude/rules/` 配下のルールに準拠すること

## 引数

```
$ARGUMENTS = [markdown-file-path] [--branch <branch-name>]
```

- `<markdown-file-path>`: 実装の元となる Markdown ファイルのパス（任意）
- `--branch <branch-name>`: 作業ブランチの指定（任意）

### ソースの解決ルール

引数と実行環境に応じて、実装の入力ソースを以下の優先順で決定する:

| 条件 | ソース | 例 |
|------|--------|-----|
| 引数がファイルパス（`/` を含む or `.md` で終わる） | 指定された Markdown ファイルを読み込む | `/implement .claude/outputs/plans/PLAN-42-xxx.md` |
| 引数なし + `CI=true` | 現在の Issue のコメント内容を読み取る | `/implement` |
| 引数なし + ローカル | エラー（ソースの指定が必要） | — |

### ブランチの動作

| 引数 | 動作 |
|------|------|
| `--branch` なし | Markdown の内容からブランチ名を自動生成し、`main` から新規ブランチを作成。実装完了後に PR を作成する |
| `--branch <existing-branch>` | 指定されたブランチにチェックアウトし、そのブランチ上で修正を実施。実装完了後に Push する（PR は作成しない） |

## 手順

### 1. 引数の解析

```
ARGUMENTS = "$ARGUMENTS"
```

- `--branch` オプションがあればブランチ名を取得する
- 残りの引数からソース種別を判定する（ファイルパス / Issue 番号 / なし）

### 2. 実装ソースの取得

ソースの解決ルールに従い、実装内容を取得する。

#### パターン A: ファイルパスが指定された場合

指定された Markdown ファイルを読み込む。ファイルが存在しない場合はエラーメッセージを出力して終了する。

#### パターン B: 引数なし + CI 環境

現在の Issue のコンテキストからソースを取得する。

```bash
# 現在の Issue 番号を取得（GitHub Actions のコンテキストから）
ISSUE_NUMBER=${GITHUB_ISSUE_NUMBER:-}

# Issue 番号が取得できない場合
if [ -z "$ISSUE_NUMBER" ]; then
  echo "Error: Issue number not found in CI context"
  exit 1
fi

# Issue 本文とコメントの取得
gh issue view ${ISSUE_NUMBER} --json body,title,comments
```

以下の優先順でソースを特定する:

1. Issue コメントの中から「実装プラン」セクション（`## 実装プラン` で始まるコメント）を検索する
2. 実装プランコメントが見つからない場合は、Issue 本文を実装ソースとして使用する
3. トリガーとなったコメント（`@claude /implement` を含むコメント）自体に実装指示が含まれている場合は、そのコメント内容も実装ソースに加味する
4. いずれも実装可能な内容を含まない場合はエラーメッセージを出力して終了する

### 3. 実装ソースの理解

取得したソースを深く理解する。

- **実装プランの場合**: Phase / Step の構成、対象ファイル、変更内容を把握する
- **レビュー指摘の場合**: 指摘事項、修正案、対象ファイル・行番号を把握する
- **Issue 本文の場合**: 要件・仕様を把握し、コードベースを調査した上で実装方針を決定する
- **その他の Markdown**: 記述された要件・仕様を把握する

### 4. ブランチの準備

#### `--branch` なしの場合（新規実装）

1. `main` ブランチの最新を取得する

```bash
git fetch origin main
git checkout main
git pull origin main
```

2. ソースの内容からブランチ名を自動生成する
   - 実装プランの場合: `feat/<issue-number>-<概要のケバブケース>` の形式
     - 例: Issue #42 "Add Threads support" → `feat/42-add-threads-support`
   - レビュー指摘修正の場合: `fix/<issue-number>-<概要のケバブケース>` の形式
   - その他: `feat/<概要のケバブケース>` の形式

3. ブランチを作成する

```bash
git checkout -b <branch-name>
```

#### `--branch` ありの場合（既存ブランチでの修正）

1. 指定されたブランチにチェックアウトする

```bash
git checkout <branch-name>
git pull origin <branch-name>
```

### 5. コードの実装

ソースの内容に基づいてコードを実装する。

#### 実装時の注意事項

- `.claude/rules/` 配下のルールファイルに準拠すること
  - `coding-standards.md`: TypeScript strict モード、Biome フォーマット、named export、i18n
  - `electron.md`: IPC 型付け、レンダラーからメインプロセスのインポート禁止、electron-log 使用
  - `shared-package.md`: フレームワーク非依存、domain/data/types 構成
  - `ui-components.md`: CSS Modules、features/primitives 構成、純粋 React
- 既存のコードベースのパターン・命名規則に従うこと
- `CLAUDE.md` に記載されたアーキテクチャとパッケージ構成を遵守すること
- 実装プランがある場合は Phase / Step の順序に従って段階的に実装すること
- 各ステップの実装後、型エラーがないことを確認すること
- モノレポの各パッケージの責務を守ること
  - `packages/shared`: Electron/React に依存しないビジネスロジック
  - `packages/ui`: Electron API に依存しない純粋 React コンポーネント
  - `apps/desktop`: Electron 固有のコード

#### 実装の進め方

1. **ドメインロジックの実装**: `packages/shared` の新規・修正
2. **UI コンポーネントの実装**: `packages/ui` の新規・修正
3. **Electron 統合の実装**: `apps/desktop` のメインプロセス、IPC、レンダラー
4. **テストコードの実装**: Vitest ユニットテスト、Playwright E2E テスト
5. **ドキュメントの更新**: README、CLAUDE.md など（ソースに記載がある場合）

### 6. ビルドとフォーマットの確認

実装完了後、以下を実行する:

```bash
# フォーマットとリントの適用
pnpm lint:fix

# 型チェック
pnpm typecheck

# ユニットテストの実行
pnpm test
```

- `pnpm lint:fix` は必ずコミット前に実行すること
- 型チェックやテストが失敗した場合は原因を特定し修正すること。修正後に再度実行し、成功するまで繰り返す

### 7. コミット

変更内容をコミットする。

- コミットメッセージは変更内容を適切に要約すること
- 実装プランの場合は Issue 番号をコミットメッセージに含めること
  - 例: `Close #42: Add Threads support`
- レビュー指摘修正の場合は修正内容を簡潔に記載すること
  - 例: `Fix review comments: improve error handling and add missing tests`
- 複数の論理的なまとまりがある場合は、適切にコミットを分割すること

```bash
git add <files>
git commit -m "$(cat <<'EOF'
<commit message>

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

### 8. Push と PR 作成

#### `--branch` なしの場合（新規実装）

1. リモートに Push する

```bash
git push -u origin <branch-name>
```

2. PR を作成する

```bash
gh pr create --title "<PR title>" --body "$(cat <<'EOF'
## Summary
<変更内容の箇条書き>

## Test plan
<テスト方針のチェックリスト>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

- PR タイトルは Issue を閉じる場合 `Close #<issue-number>: <概要>` の形式にする
- PR タイトルは 70 文字以内に収めること

3. PR の URL をユーザーに返すこと

#### `--branch` ありの場合（既存ブランチでの修正）

1. リモートに Push する

```bash
git push origin <branch-name>
```

2. Push が完了した旨をユーザーに報告すること

## 注意事項

- ソースの内容を正確に理解し、過不足のない実装を行うこと
- 推測ではなく、実際のコードを読んで確認した事実に基づいて実装すること
- 実装中に不明点や判断が必要な事項があればユーザーに確認すること
- ビルドが通らない状態でコミット・Push しないこと
- `pnpm lint:fix` を忘れずに実行すること
- CI 環境で Issue 本文から実装する場合は、要件が曖昧な場合に Issue にコメントで確認してから実装を進めること
