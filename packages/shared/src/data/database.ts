import initSqlJs, { type Database as SqlJsDatabase } from 'sql.js'
import fs from 'node:fs'
import path from 'node:path'

const DDL = `
CREATE TABLE IF NOT EXISTS local_user (
    id          TEXT    NOT NULL PRIMARY KEY,
    created_at  TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS service_plugin (
    service_id    TEXT    NOT NULL PRIMARY KEY,
    display_name  TEXT    NOT NULL,
    domain_patterns TEXT  NOT NULL,
    brand_color   TEXT    NOT NULL,
    icon_resource TEXT    NOT NULL,
    auth_type     TEXT    NOT NULL DEFAULT 'cookie',
    sort_order    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS account (
    account_id   TEXT    NOT NULL PRIMARY KEY,
    service_id   TEXT    NOT NULL,
    display_name TEXT,
    avatar_url   TEXT,
    sort_order   INTEGER NOT NULL DEFAULT 0,
    created_at   TEXT    NOT NULL,
    FOREIGN KEY (service_id) REFERENCES service_plugin(service_id)
);

CREATE INDEX IF NOT EXISTS idx_account_sort ON account(sort_order);
CREATE INDEX IF NOT EXISTS idx_account_service ON account(service_id);

CREATE TABLE IF NOT EXISTS account_state (
    account_id       TEXT    NOT NULL PRIMARY KEY,
    last_url         TEXT,
    scroll_position_y INTEGER DEFAULT 0,
    webview_status   TEXT    NOT NULL DEFAULT 'destroyed',
    last_accessed_at TEXT    NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_account_state_lru ON account_state(webview_status, last_accessed_at);

CREATE TABLE IF NOT EXISTS app_state (
    id                 INTEGER NOT NULL PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    active_account_id  TEXT,
    clean_shutdown     INTEGER NOT NULL DEFAULT 0,
    last_saved_at      TEXT    NOT NULL,
    FOREIGN KEY (active_account_id) REFERENCES account(account_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS settings (
    key   TEXT NOT NULL PRIMARY KEY,
    value TEXT NOT NULL
);
`

const SEED_SERVICE_PLUGINS = `
INSERT OR IGNORE INTO service_plugin (service_id, display_name, domain_patterns, brand_color, icon_resource, auth_type, sort_order)
VALUES
    ('x',         'X (旧Twitter)', '["x.com","twitter.com"]',  '#000000', 'icons/x.svg',         'cookie', 0),
    ('instagram', 'Instagram',     '["instagram.com"]',        '#E1306C', 'icons/instagram.svg',  'cookie', 1),
    ('facebook',  'Facebook',      '["facebook.com"]',         '#1877F2', 'icons/facebook.svg',   'cookie', 2),
    ('youtube',   'YouTube',       '["youtube.com"]',          '#FF0000', 'icons/youtube.svg',    'cookie', 3),
    ('bluesky',   'Bluesky',       '["bsky.app"]',             '#0085FF', 'icons/bluesky.svg',    'cookie', 4),
    ('twitch',    'Twitch',        '["twitch.tv"]',            '#9146FF', 'icons/twitch.svg',     'cookie', 5);
`

const SEED_APP_STATE = `
INSERT OR IGNORE INTO app_state (id, active_account_id, clean_shutdown, last_saved_at)
VALUES (1, NULL, 0, '');
`

const SEED_SETTINGS = `
INSERT OR IGNORE INTO settings (key, value) VALUES ('locale', '');
INSERT OR IGNORE INTO settings (key, value) VALUES ('telemetry_enabled', 'false');
INSERT OR IGNORE INTO settings (key, value) VALUES ('tutorial_completed', 'false');
INSERT OR IGNORE INTO settings (key, value) VALUES ('webview_suspend_timeout_ms', '300000');
INSERT OR IGNORE INTO settings (key, value) VALUES ('max_background_webviews', '3');
`

export class AppDatabase {
  private db!: SqlJsDatabase
  private filePath: string

  constructor(filePath: string) {
    this.filePath = filePath
  }

  async initialize(): Promise<void> {
    const SQL = await initSqlJs()

    // Load existing database file if it exists
    let data: Buffer | undefined
    try {
      data = fs.readFileSync(this.filePath)
    } catch {
      // File doesn't exist yet, create new database
    }

    this.db = data ? new SQL.Database(data) : new SQL.Database()

    // Enable foreign keys
    this.db.run('PRAGMA foreign_keys = ON')

    this.db.run(DDL)
    this.db.run(SEED_SERVICE_PLUGINS)
    this.db.run(SEED_APP_STATE)
    this.db.run(SEED_SETTINGS)

    this.save()
  }

  getDb(): SqlJsDatabase {
    return this.db
  }

  save(): void {
    const data = this.db.export()
    const dir = path.dirname(this.filePath)
    fs.mkdirSync(dir, { recursive: true })
    fs.writeFileSync(this.filePath, Buffer.from(data))
  }

  close(): void {
    this.save()
    this.db.close()
  }
}
