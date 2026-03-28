---
name: implement
description: Issue 上の実装プラン、PR のレビュー指摘、またはローカル Markdown を元にコードを実装する。Issue 番号、PR 番号、またはファイルパスを指定する。
argument-hint: "<issue-number> | <pr-number> --pr | <markdown-file-path> [--branch <branch-name>]"
---

# Implement Skill

以下の3つの入力ソースに基づいてコードを実装する:
- **Issue 番号**: Issue コメント上の実装プラン（`<!-- claude:plan -->` マーカー）を読み取って実装する
- **PR 番号 + `--pr`**: PR のレビュー指摘（インラインコメント）に対して修正を行う
- **Markdown ファイルパス**: ローカルの Markdown ファイルを読み込んで実装する

## 前提条件

- `gh` CLI が認証済みであること
- コーディング規約 `.claude/rules/` 配下のルールに準拠すること

## 引数

```
$ARGUMENTS = <issue-number> | <pr-number> --pr | <markdown-file-path> [--branch <branch-name>]
```

### 入力モードの判定

| 引数パターン | モード | 動作 |
|-------------|--------|------|
| 数値のみ（例: `42`） | **Issue プラン実装** | Issue コメントからプランを読み取り実装 |
| 数値 + `--pr`（例: `15 --pr`） | **PR レビュー指摘対応** | PR のレビュー指摘を読み取り修正 |
| ファイルパス [+ `--branch`]（例: `plan.md`） | **Markdown 実装** | ローカル Markdown を読み込んで実装 |

引数なしの場合はエラーとする。

## 手順

### 1. 入力ソースの取得

#### モード A: Issue プラン実装

Issue コメントから `<!-- claude:plan -->` マーカー付きのプランを抽出する。

```bash
# Issue のコメント一覧からプランコメントを取得
gh api repos/{owner}/{repo}/issues/<issue-number>/comments \
  --jq '.[] | select(.body | contains("<!-- claude:plan -->"))'
```

- プランコメントが見つからない場合はエラーメッセージを出力して終了する
- 複数のプランコメントが見つかった場合は、最新（最後に投稿された）コメントを採用する
- プランの Phase / Step の構成、対象ファイル、変更内容を把握する

#### モード B: PR レビュー指摘対応

PR のレビュー指摘（未解決のもの）を取得する。

##### B-1. 未解決のレビュースレッドを取得

```bash
gh api graphql -f query='
{
  repository(owner: "{owner}", name: "{repo}") {
    pullRequest(number: <pr-number>) {
      reviewThreads(first: 100) {
        nodes {
          isResolved
          comments(first: 10) {
            nodes {
              body
              path
              line
              diffHunk
              author { login }
            }
          }
        }
      }
    }
  }
}'
```

- `isResolved: false` のスレッドのみを対象とする
- bot コメント（CI 等）は除外する
- 各指摘の `path`, `line`, `diffHunk` から修正箇所を特定する
- 解釈不能な指摘はスキップし、対応できなかった旨をユーザーに報告する

##### B-2. PR のブランチ情報を取得

```bash
gh pr view <pr-number> --json headRefName,baseRefName
```

#### モード C: Markdown 実装

指定された Markdown ファイルを読み込み、内容を深く理解する。

- **実装プランの場合**: Phase / Step の構成、対象ファイル、変更内容を把握する
- **レビュー指摘の場合**: 指摘事項、修正案、対象ファイル・行番号を把握する
- **その他の Markdown**: 記述された要件・仕様を把握する

### 2. ブランチの準備

#### モード A（Issue プラン実装）: 新規ブランチを作成

1. `main` ブランチの最新を取得し、そこから新規ブランチを作成する
2. ブランチ名: `feat/<issue-number>-<概要のケバブケース>`
   - 例: `feat/42-add-threads-support`

#### モード B（PR レビュー指摘対応）: PR のブランチにチェックアウト

PR の `headRefName` にチェックアウトし、最新を pull する。

#### モード C（Markdown 実装）: 引数に依存

| 引数 | 動作 |
|------|------|
| `--branch` なし | Markdown の内容からブランチ名を自動生成し、`main` から新規ブランチを作成 |
| `--branch <existing-branch>` | 指定されたブランチにチェックアウトし、最新を pull する |

ブランチ名の自動生成ルール:
- 実装プランの場合: `feat/<issue-number>-<概要のケバブケース>`
- レビュー指摘修正の場合: `fix/<issue-number>-<概要のケバブケース>`
- その他: `feat/<概要のケバブケース>`

### 3. コードの実装

入力ソースの内容に基づいてコードを実装する。

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

#### パッケージの責務

| パッケージ | 責務 | 依存禁止 |
|-----------|------|---------|
| `packages/shared` | ビジネスロジック（ドメインマネージャー、リポジトリ、型定義） | Electron, React |
| `packages/ui` | 純粋 React コンポーネント（CSS Modules でスタイリング） | Electron API |
| `apps/desktop` | Electron 固有コード（メインプロセス、IPC、プリロード） | - |

#### 実装の進め方

1. **ドメインロジックの実装**: `packages/shared` の新規・修正
2. **UI コンポーネントの実装**: `packages/ui` の新規・修正
   - 新規コンポーネントには **Storybook Story** (`.stories.tsx`) も作成すること
   - 既存の Story ファイルのパターンに合わせること
3. **Electron 統合の実装**: `apps/desktop` のメインプロセス、IPC、レンダラー
4. **テストコードの実装**: Vitest ユニットテスト、Playwright E2E テスト
5. **i18n リソースの更新**: 新しい UI テキストがある場合は `packages/shared/src/i18n/` のリソースファイルに日本語・英語の翻訳を追加すること
6. **ドキュメントの更新**: README、CLAUDE.md など（ソースに記載がある場合）

#### サービスプラグインの追加時

新しい SNS サービスを追加する場合は、`ServicePluginRegistry` のパターンに従うこと:
- `packages/shared/src/domain/ServicePluginRegistry` にサービスメタデータ（ブランドカラー、ドメイン、アイコン）を追加
- 関連する UI コンポーネント（`ServiceIcon` 等）を更新

### 4. ビルドとフォーマットの確認

実装完了後、以下を実行する:

```bash
pnpm lint:fix
pnpm typecheck
pnpm test
```

- `pnpm lint:fix` は必ずコミット前に実行すること
- 型チェックやテストが失敗した場合は原因を特定し修正すること。修正後に再度実行し、成功するまで繰り返す

### 5. コミット

変更内容をコミットする。

- コミットメッセージは変更内容を適切に要約すること
- 実装プランの場合は Issue 番号をコミットメッセージに含めること
  - 例: `feat: add Threads support (#42)`
- レビュー指摘修正の場合は修正内容を簡潔に記載すること
  - 例: `fix: improve error handling and add missing tests`
- 複数の論理的なまとまりがある場合は、適切にコミットを分割すること
- Co-Authored-By には実行時のモデル情報を使用すること

```bash
git add <files>
git commit -m "$(cat <<'EOF'
<commit message>

Co-Authored-By: <実行中のモデル名> <noreply@anthropic.com>
EOF
)"
```

### 6. Push と PR 作成

#### モード A（Issue プラン実装）: Push して PR 作成

1. リモートに Push する

```bash
git push -u origin <branch-name>
```

2. PR を作成する。Issue を紐づけるため **PR 本文** に `Closes #<issue-number>` を記載する。

```bash
gh pr create --title "<PR title>" --body "$(cat <<'EOF'
## Summary
<変更内容の箇条書き>

Closes #<issue-number>

## Test plan
<テスト方針のチェックリスト>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

- PR タイトルは 70 文字以内に収めること

3. Issue に `Type: *` ラベルが付与されている場合、同じラベルを PR にも付与する

```bash
LABELS=$(gh issue view <issue-number> --json labels --jq '[.labels[].name | select(startswith("Type: "))] | join(",")')
if [ -n "$LABELS" ]; then
  gh pr edit --add-label "$LABELS"
fi
```

- PR の URL をユーザーに返すこと

#### モード B（PR レビュー指摘対応）: Push のみ

1. リモートに Push する
2. 対応した指摘と対応できなかった指摘をユーザーに報告すること

#### モード C（Markdown 実装）: 引数に依存

**`--branch` なしの場合（新規実装）:**
- Push して PR を作成する（モード A と同様のフロー）

**`--branch` ありの場合（既存ブランチでの修正）:**
- Push のみ行い、完了をユーザーに報告する

## 注意事項

- 入力ソースの内容を正確に理解し、過不足のない実装を行うこと
- 推測ではなく、実際のコードを読んで確認した事実に基づいて実装すること
- 実装中に不明点や判断が必要な事項があればユーザーに確認すること
- ビルドが通らない状態でコミット・Push しないこと
- `pnpm lint:fix` を忘れずに実行すること
