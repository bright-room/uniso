# /review スキル 使い方ガイド

## 概要

PR に対してコードレビューを実施し、指摘事項をインラインコメントとして GitHub 上に投稿するスキル。コードベース全体レビューも可能（ローカル出力）。

## 使い方

```
/review 15                       # PR #15 をレビューし、インラインコメントとして投稿
/review 15 --full                # PR #15 を全カテゴリでレビュー
/review 15 --category security,code  # セキュリティとコード品質のみレビュー
/review --full-codebase          # コードベース全体レビュー（ローカル出力）
```

## レビューモード

| 条件 | モード | 出力先 |
|------|--------|--------|
| PR 番号を指定 | PR レビュー | GitHub インラインコメント |
| `--full-codebase` 指定 | コードベース全体レビュー | ローカルファイル |

## 再レビュー

同じ PR に対して再度 `/review` を実行すると、前回レビュー以降の変更分のみをレビュー対象にする。

- 前回レビュー時の commit SHA と現在の HEAD SHA を比較して差分を取得
- Resolved 済みのスレッドは除外し、同じ指摘を繰り返さない

```
/review 15        # 初回: ベースブランチからの全差分をレビュー
# → レビュー指摘に対してコード修正・Push
/review 15        # 2回目: 前回レビュー以降の変更分のみレビュー
```

## カテゴリ選択

| 条件 | カテゴリ |
|------|---------|
| `--full` 指定 | 全6カテゴリ |
| `--category` 指定 | 指定されたカテゴリのみ |
| コードベース全体レビュー | 全6カテゴリ |
| PR レビュー（指定なし） | 変更ファイルから自動選択 |

### カテゴリ一覧

| カテゴリ名 | 説明 |
|-----------|------|
| `architecture` | モノレポ構成・責務分離・Electron プロセス分離 |
| `code` | ロジック・命名・TypeScript の型安全性 |
| `test` | カバレッジ・網羅性・品質（Vitest / Playwright / Storybook） |
| `security` | Electron セキュリティ・IPC 安全性・WebView 分離・sql.js |
| `docs` | TSDoc・README・i18n 対応 |
| `build` | package.json・tsconfig・Biome・electron-builder 設定 |

## レビュー結果の投稿

PR レビューでは、指摘の深刻度に応じて自動的にレビューイベントを選択する。

| 条件 | イベント |
|------|---------|
| Critical の指摘あり | `REQUEST_CHANGES` |
| High 以下のみ | `COMMENT` |
| 指摘なし | `APPROVE` |

## 指摘の優先度

| 優先度 | バッジ | 項番例 |
|--------|-------|--------|
| 🔴 Critical | `**[Critical]**` | C-1 |
| 🟠 High | `**[High]**` | H-1 |
| 🟡 Medium | `**[Medium]**` | M-1 |
| 🟢 Low | `**[Low]**` | L-1 |

## 定義ファイル

[SKILL.md](SKILL.md)
