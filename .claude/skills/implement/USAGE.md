# /implement スキル 使い方ガイド

## 概要

指定されたソース（Markdown ファイルまたは GitHub Issue のコメント）を読み込み、内容に基づいてコードを実装するスキル。

## 使い方

```
/implement .claude/outputs/plans/PLAN-42-add-threads-support.md
/implement .claude/outputs/plans/PLAN-42-add-threads-support.md --branch feat/42-add-threads-support
/implement .claude/outputs/reviews/REVIEW-feat-42-add-threads-support.md --branch feat/42-add-threads-support
```

Markdown ファイルパスの指定が必須。

## ソースの解決ルール

| 条件 | ソース |
|------|--------|
| 引数がファイルパス（`/` を含む or `.md` で終わる） | 指定された Markdown ファイル |
| 引数なし | エラー（ソースの指定が必要） |

### ブランチの動作

| 引数 | 動作 |
|------|------|
| `--branch` なし | ソースの内容からブランチ名を自動生成し、`main` から新規ブランチを作成。実装完了後に PR を作成する |
| `--branch <existing-branch>` | 指定されたブランチにチェックアウトし、そのブランチ上で修正を実施。実装完了後に Push する（PR は作成しない） |

## 処理の流れ

1. **引数の解析** — ソース種別とブランチオプションを取得
2. **ソースの取得** — ファイル読み込み or Issue コメント取得
3. **ブランチの準備** — 新規作成 or 既存ブランチにチェックアウト
4. **コードの実装** — ソースの内容に基づいて段階的に実装
5. **ビルド確認** — `pnpm lint:fix` + `pnpm typecheck` + `pnpm test` の実行
6. **コミット** — 変更内容を適切なメッセージでコミット
7. **Push / PR 作成** — ブランチモードに応じて Push のみ or PR 作成

## ユースケース

| ユースケース | コマンド例 |
|-------------|-----------|
| 実装プランに基づく新規実装 | `/implement .claude/outputs/plans/PLAN-42-xxx.md` |
| レビュー指摘の修正 | `/implement .claude/outputs/reviews/REVIEW-feat-42-xxx.md --branch feat/42-xxx` |
| 任意の仕様書に基づく実装 | `/implement docs/spec.md` |

## 定義ファイル

[SKILL.md](SKILL.md)
