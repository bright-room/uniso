import type { Database as SqlJsDatabase } from 'sql.js'
import type { SettingsEntry } from '../types/settings'
import type { LocalUser } from '../types/local-user'

export class SettingsRepository {
  constructor(private db: SqlJsDatabase) {}

  getString(key: string): string | undefined {
    const stmt = this.db.prepare('SELECT value FROM settings WHERE key = ?')
    stmt.bind([key])
    if (!stmt.step()) {
      stmt.free()
      return undefined
    }
    const value = stmt.getAsObject().value as string
    stmt.free()
    return value
  }

  setString(key: string, value: string): void {
    this.db.run(
      'INSERT INTO settings (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value',
      [key, value]
    )
  }

  getBoolean(key: string): boolean | undefined {
    const value = this.getString(key)
    if (value === undefined) return undefined
    return value === 'true'
  }

  setBoolean(key: string, value: boolean): void {
    this.setString(key, value ? 'true' : 'false')
  }

  getAll(): SettingsEntry[] {
    const stmt = this.db.prepare('SELECT * FROM settings')
    const results: SettingsEntry[] = []
    while (stmt.step()) {
      const row = stmt.getAsObject()
      results.push({ key: row.key as string, value: row.value as string })
    }
    stmt.free()
    return results
  }

  delete(key: string): void {
    this.db.run('DELETE FROM settings WHERE key = ?', [key])
  }

  getLocalUser(): LocalUser | undefined {
    const stmt = this.db.prepare('SELECT * FROM local_user LIMIT 1')
    if (!stmt.step()) {
      stmt.free()
      return undefined
    }
    const row = stmt.getAsObject()
    stmt.free()
    return { id: row.id as string, createdAt: row.created_at as string }
  }

  insertLocalUser(id: string, createdAt: string): void {
    this.db.run('INSERT INTO local_user (id, created_at) VALUES (?, ?)', [id, createdAt])
  }
}
