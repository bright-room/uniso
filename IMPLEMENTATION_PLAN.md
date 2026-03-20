# Uniso 実装計画

**最終更新**: 2026-03-21
**ブランチ**: `poc/tauri-v2`
**技術スタック**: Electron 41 + React 19 + TypeScript + sql.js + pnpm workspace

---

## 現在の進捗

### 完了済み

| カテゴリ | 内容 | 状態 |
|---|---|---|
| モノレポ構築 | pnpm workspace, tsconfig, electron-vite | ✅ |
| 型定義 | Account, AccountState, AppState, ServicePlugin, Link, Error 等 9ファイル | ✅ |
| データ層 | SQLite スキーマ (6テーブル), 4リポジトリ, sql.js 初期化 | ✅ |
| ドメイン層 | AccountManager, SessionManager, LinkRouter, I18nManager, IdentityManager, ServicePluginRegistry | ✅ |
| i18n | 日英 JSON リソース (全キー移植済み) | ✅ |
| メインプロセス | WebViewManager (セッション分離, LRU 管理, リンクルーティング連携) | ✅ |
| セッション設定 | UA 偽装, Sec-CH-UA ヘッダー書き換え, fingerprint マスク | ✅ |
| IPC | 全ハンドラー (アカウント CRUD, 設定, i18n, WebView 制御, ネイティブコンテキストメニュー) | ✅ |
| サイドバー UI | AccountItem, AddAccountButton, サービス間セパレーター, ネイティブ右クリックメニュー | ✅ |
| ヘッダーバー | サービスアイコン + アカウント名 + URL 表示 | ✅ |
| ダイアログ | アカウント追加, 削除確認, クラッシュ復元, アカウント選択 | ✅ |
| 設定画面 | 言語切替, キーボードショートカット表示, バージョン情報 | ✅ (部分的) |
| キーボードショートカット | Ctrl+Tab / Ctrl+Shift+Tab (アカウント切替) | ✅ |
| セッション永続化 | 30秒周期保存, クラッシュ検出, sql.js ファイル書き出し | ✅ |
| ダイアログ表示 | サイドバー View の z-order 制御 + 半透明オーバーレイ (SNS コンテンツ透過表示) | ✅ |

---

## 未実装タスク

### P0: MVP 必須

#### 1. Google OAuth システムブラウザ連携
**優先度**: 高 — YouTube ログインに必須、X の Google サインインにも影響

**現状の問題**:
- Google が Electron (全ての組み込みブラウザ) での OAuth を意図的にブロック
- YouTube はログイン不可、X は Google サインイン経由だと不可

**実装方針** (VS Code, Figma, Slack と同じパターン):
```
1. ユーザーが「Google でログイン」をクリック
2. アプリがシステムブラウザ (Chrome / Safari) を起動
3. ユーザーがブラウザ上で Google ログインを完了
4. カスタムプロトコル (uniso://) またはローカル HTTP サーバーでコールバック受信
5. 認証トークン/Cookie を Electron セッションに注入
```

**作業項目**:
- [x] カスタムプロトコル `uniso://` の登録 (`app.setAsDefaultProtocolClient`)
- [ ] ローカル HTTP サーバー (localhost コールバック) のフォールバック実装
- [ ] `shell.openExternal()` でシステムブラウザ起動
- [ ] コールバックから Cookie/トークンを Electron session に注入
- [x] WebView 内の Google OAuth URL を検出してインターセプト
  - MVP: 同一セッションの Popup BrowserWindow で OAuth を処理 (will-navigate でインターセプト)

#### 2. チュートリアル画面 (オンボーディング)
**優先度**: 高 — 初回起動体験

**UI モック仕様書 §6 準拠**:
- [x] TutorialScreen コンポーネント (4ステップ全画面オーバーレイ)
  - Step 1: Welcome — アプリ紹介
  - Step 2: Add Account — サイドバーの「+」ボタン説明
  - Step 3: Switch Account — アカウント切替とショートカット説明
  - Step 4: Complete — 開始ボタン
- [x] ドットインジケーター (ステップナビゲーション)
- [x] スキップ / 次へ / 戻る / はじめる ボタン
- [x] `tutorial_completed` 設定フラグとの連携
- [x] 初回起動時の自動表示

#### 3. テレメトリ同意ダイアログ
**優先度**: 中 — 初回起動フロー

- [x] TelemetryConsentDialog コンポーネント
- [x] 「許可する」→ `telemetry_enabled = true`
- [x] 「許可しない」→ `telemetry_enabled = false` (デフォルト)
- [x] 初回起動時、チュートリアルの前に表示 (テレメトリ → チュートリアル → メイン画面)

#### 4. 設定画面の完成
**優先度**: 中

**不足している項目**:
- [ ] アカウント管理セクション (並び替え、表示名編集、削除)
- [x] テレメトリ収集トグル
- [x] チュートリアル再表示ボタン
- [ ] ユーザーエージェント設定 (詳細設定)

#### 5. WebView エラーページ
**優先度**: 中 — ネットワーク切断時の UX

- [x] `did-fail-load` イベントのハンドリング
- [x] カスタムエラー HTML (サービスアイコン + エラーメッセージ + 再試行ボタン + 「ブラウザで開く」リンク)
- [x] リトライ機能

#### 6. コンテンツプレースホルダー
**優先度**: 低

- [x] WebView 未ロード / 再生成中に表示するプレースホルダー
- [x] サービスアイコン + 「読み込み中」テキスト

---

### P1: リリース準備

#### 7. electron-builder パッケージング
**優先度**: 高

- [x] `electron-builder.yml` 設定
- [x] macOS: DMG 生成, コード署名, 公証 (notarization)
- [x] Windows: NSIS / MSI インストーラー
- [ ] アプリアイコン設定 (`.claude/inputs/logo/` の SVG から生成)
- [x] `pnpm run package` スクリプト

#### 8. 自動アップデート
**優先度**: 中

- [x] `electron-updater` 統合
- [x] GitHub Releases をアップデートソースとして設定
- [x] バックグラウンドバージョンチェック
- [x] UpdateDialog との連携
- [x] ダウンロード + インストールフロー

#### 9. CI/CD (GitHub Actions)
**優先度**: 中

- [x] PR 時: lint, typecheck, test, build
- [x] タグ push 時: build + package + GitHub Release 作成
- [x] macOS / Windows マトリクスビルド

#### 10. ユニットテスト
**優先度**: 中

**packages/shared のテスト (vitest)**:
- [x] AccountRepository: CRUD 操作 (in-memory SQLite)
- [x] SessionRepository: 状態保存/復元
- [x] AccountManager: 追加/削除/切替/順序
- [x] SessionManager: 周期保存/クラッシュ検出
- [x] LinkRouter: 5分類パス全てのテスト
- [x] I18nManager: ロケール切替/フォールバック
- [x] ServicePluginRegistry: ドメインマッチング

---

### P2: ポストリリース

#### 11. キーボードショートカット拡充
- [ ] Ctrl+N / Cmd+N: アカウント追加ダイアログ
- [ ] Ctrl+, / Cmd+,: 設定画面
- [ ] Ctrl+R / Cmd+R: リロード
- [ ] Ctrl+Shift+R / Cmd+Shift+R: 強制リロード

#### 12. モバイル対応 (Capacitor)
- [ ] `apps/mobile/` ディレクトリ構成
- [ ] Capacitor 初期設定
- [ ] iOS (WKWebView) / Android (WebView) ネイティブレイヤー
- [ ] 共通 React コンポーネントの流用

#### 13. X.com メール/パスワードログイン検証
- [ ] Google サインイン以外のログインフロー確認
- [ ] 問題があればシステムブラウザ方式で対応

---

## 既知の問題

| 問題 | 影響範囲 | 対応方針 | 対応タスク |
|---|---|---|---|
| Google OAuth ブロック | YouTube (ログイン不可), X (Google サインイン不可) | システムブラウザ OAuth | #1 |
| メモリ消費 | アカウント数増加時 | LRU 方式遅延破棄 (実装済み: 上限3, 5分タイムアウト) | — |
| sql.js WASM | 起動時の非同期ロード | 初期化完了まで待機 (実装済み) | — |
| better-sqlite3 非互換 | Electron 41 の V8 API 変更 | sql.js に移行済み | — |

---

## 技術メモ

- **sql.js**: Electron 41 で `better-sqlite3` のネイティブモジュールがコンパイル不可のため WASM 版を使用。インメモリ DB → 周期的にファイル書き出し。
- **WebContentsView z-order**: ダイアログ表示時にサイドバー View を `removeChildView` → `addChildView` で最前面に移動。半透明オーバーレイ越しに SNS コンテンツが透過表示される。
- **ネイティブコンテキストメニュー**: サイドバー View が 72px 幅のため HTML メニューはクリップされる → `Menu.popup()` を使用。
