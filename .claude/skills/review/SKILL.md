---
name: review
description: コードレビューを実施する。ローカルではブランチ差分または main 全体のレビュー、CI 環境では PR レビューとして投稿する。
argument-hint: "[base-branch]"
---

# Code Review Skill

コードレビューを実施し、構造化された Markdown レポートとして出力すること。

## レビュー方針

- 行番号と**文脈情報（背景知識など）**を必ず含める
- レビュー結果はチームメンバーに共有されるため、細部まで入念に確認すること
- 書き始める前に深く考察し、すべての箇所を漏れなくチェックすること
- 事実に基づかない内容（ハルシネーション）を含めないこと

## レビューモードの解決ルール

引数と実行環境に応じて、レビューモードを以下の優先順で決定する:

| 条件 | モード | レビュー対象 | 出力先 |
|------|--------|-------------|--------|
| ローカル + `main` 以外のブランチ指定 | **ブランチ差分レビュー** | 指定ブランチとの差分 | ローカルファイル |
| ローカル + 引数なし or `main` 指定 | **コードベース全体レビュー** | main ブランチの全コード | ローカルファイル |
| `CI=true` + 引数なし | **PR レビュー** | PR の差分 | PR Review コメント |

## 手順

### 1. レビュー対象の特定

#### モード A: ブランチ差分レビュー（ローカル + main 以外のブランチ指定）

指定されたベースブランチと現在のブランチの差分をレビューする。

```bash
BASE_BRANCH="$ARGUMENTS"
git diff ${BASE_BRANCH}...HEAD --name-only
git diff ${BASE_BRANCH}...HEAD
git log ${BASE_BRANCH}...HEAD --oneline
```

#### モード B: コードベース全体レビュー（ローカル + 引数なし or main 指定）

main ブランチのコードベース全体を対象にレビューする。差分ではなく、リポジトリ全体のコード品質・設計・テストを包括的にレビューする。

- リポジトリ内のすべての TypeScript/TSX ソースファイル（`*.ts`, `*.tsx`）を読み込む
- テストファイル、設定ファイル（`package.json`、`tsconfig.*.json`、`biome.json` 等）、ドキュメントも対象とする
- `node_modules/`、`dist/`、`out/` ディレクトリはレビュー対象外とする

#### モード C: PR レビュー（CI 環境）

```bash
PR_NUMBER=$(gh pr view --json number -q .number)
BASE_BRANCH=$(gh pr view --json baseRefName -q .baseRefName)
git diff ${BASE_BRANCH}...HEAD --name-only
git diff ${BASE_BRANCH}...HEAD
git log ${BASE_BRANCH}...HEAD --oneline
```

### 2. コードの読解

レビュー対象のファイルをすべて読み込み、内容を深く理解する。対象ファイルだけでなく、関連するファイル（呼び出し元、型定義、IPC チャネル、CSS Modules など）も確認すること。

### 3. レビューの実施

以下の観点でレビューを行う:

- **アーキテクチャ**: モノレポ構成の責務分離、パッケージ間の依存関係、Electron メイン/レンダラープロセス分離
- **プロダクトコード**: ロジックの正確性、エッジケース、命名規則、TypeScript の型安全性
- **テストコード**: テストカバレッジ、テストケースの網羅性、テストの品質（Vitest / Playwright）
- **ビルド・設定**: package.json、tsconfig、Biome 設定、electron-builder 設定の正確性
- **セキュリティ**: Electron セキュリティベストプラクティス（contextIsolation、nodeIntegration）、IPC の安全性、WebView の分離、入力バリデーション
- **ドキュメント**: TSDoc コメントの整備状況、README の更新、公開 API の説明の十分さ

### 4. レビュー結果の出力

`references/review-format.md` を読み込み、記載されたフォーマットに従ってレビュー結果を生成する。

#### モード A・B: ローカル実行時

レビュー結果を `.claude/outputs/reviews/` ディレクトリにファイルとして出力する。

- ディレクトリが存在しない場合は作成すること
- ファイル名:
  - モード A: `REVIEW-<現在のブランチ名（スラッシュをハイフンに変換）>.md`
    - 例: ブランチ `feat/42-add-threads-support` → `REVIEW-feat-42-add-threads-support.md`
  - モード B: `REVIEW-main-YYYY-MM-DD.md`
    - 例: `REVIEW-main-2026-03-28.md`

#### モード C: GitHub Actions 実行時 (`CI=true`)

レビュー結果を GitHub Pull Request Review として投稿する。サマリはレビュー本文に、各指摘はコードの該当行にインラインコメントとして紐付ける。

##### 事前準備

```bash
OWNER=$(gh repo view --json owner -q .owner.login)
REPO=$(gh repo view --json name -q .name)
PR_NUMBER=$(gh pr view --json number -q .number)
LATEST_COMMIT=$(gh pr view --json headRefOid -q .headRefOid)
```

`PR_NUMBER` が取得できない場合はエラーメッセージを出力して終了すること。

##### レビューの構成

レビューは **1回の API コール** で投稿する:

1. **レビュー本文 (`body`)**: 総合評価 + レビューサマリ（指摘事項一覧表 + 対応チェックリスト）+ カバレッジマトリクス
2. **インラインコメント (`comments`)**: 各指摘を該当ファイル・該当行に紐付け

##### インラインコメントのフォーマット

各指摘を以下の形式でインラインコメントとして記述する:

```
🔴 **C-1: <問題のタイトル>**

**問題点**
<具体的な問題の説明>

**背景**
<なぜこれが問題なのか、技術的な文脈情報>

**修正案**
\`\`\`typescript
// 修正後のコード例
\`\`\`
```

- 1つの指摘に関連する修正詳細（C-1-1, C-1-2 等）がある場合は、同じインラインコメント内にまとめること
- 優先度に応じた絵文字プレフィックス: 🔴 Critical / 🟠 High / 🟡 Medium / 🟢 Low

##### API コール

JSON をファイルに書き出してから `gh api` に渡す。コメント本文に特殊文字（バッククォート、ダブルクォート等）が含まれても安全に処理するため、インラインでの JSON 構築は避けること。

手順:

1. レビュー本文とインラインコメントの JSON を一時ファイルに書き出す
2. `gh api` の `--input` オプションでファイルを渡す

```bash
# 1. JSON ファイルを作成（Python/Node.js 等でエスケープを確実に行う）
python3 -c "
import json, sys
review = {
    'commit_id': '${LATEST_COMMIT}',
    'body': '''<レビュー本文>''',
    'event': 'COMMENT',
    'comments': [
        {
            'path': 'packages/shared/src/domain/example.ts',
            'line': 42,
            'body': '<コメント本文>'
        }
    ]
}
json.dump(review, sys.stdout, ensure_ascii=False)
" > /tmp/review-payload.json

# 2. API コール
gh api repos/${OWNER}/${REPO}/pulls/${PR_NUMBER}/reviews \
  --method POST \
  --input /tmp/review-payload.json

# 3. 一時ファイルの削除
rm -f /tmp/review-payload.json
```

##### 行番号の特定方法

インラインコメントの `line` は **変更後ファイルの実際の行番号** を使用する。

1. `git diff ${BASE_BRANCH}...HEAD` の出力から、各ファイルの diff ヘッダー (`@@ -a,b +c,d @@`) を確認する
2. 指摘対象のコードが変更後ファイルの何行目にあるかを特定する
3. その行番号を `line` フィールドに設定する

diff に含まれない行（未変更行）にはコメントできないため、その場合はレビュー本文の末尾に「その他の指摘」セクションとして記載すること。

##### 注意事項

- レビュー本文にはインラインコメントで投稿する個別指摘の詳細を重複して書かないこと。サマリの一覧表で項番・タイトル・概要のみ記載する
- API コールが失敗した場合はエラー内容を出力すること
