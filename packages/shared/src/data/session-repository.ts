import type { Database as SqlJsDatabase } from 'sql.js'
import type { AccountState, AccountStateWithInfo } from '../types/account-state'

export class SessionRepository {
  constructor(private db: SqlJsDatabase) {}

  getAppState(): {
    id: number
    activeAccountId: string | null
    cleanShutdown: boolean
    lastSavedAt: string
  } {
    const stmt = this.db.prepare('SELECT * FROM app_state WHERE id = 1')
    stmt.step()
    const row = stmt.getAsObject()
    stmt.free()
    return {
      id: row.id as number,
      activeAccountId: row.active_account_id as string | null,
      cleanShutdown: (row.clean_shutdown as number) === 1,
      lastSavedAt: row.last_saved_at as string,
    }
  }

  updateActiveAccount(accountId: string | null, lastSavedAt: string): void {
    this.db.run('UPDATE app_state SET active_account_id = ?, last_saved_at = ? WHERE id = 1', [
      accountId,
      lastSavedAt,
    ])
  }

  updateCleanShutdown(cleanShutdown: boolean, lastSavedAt: string): void {
    this.db.run('UPDATE app_state SET clean_shutdown = ?, last_saved_at = ? WHERE id = 1', [
      cleanShutdown ? 1 : 0,
      lastSavedAt,
    ])
  }

  updateLastSavedAt(lastSavedAt: string): void {
    this.db.run('UPDATE app_state SET last_saved_at = ? WHERE id = 1', [lastSavedAt])
  }

  getAccountState(accountId: string): AccountState | undefined {
    const stmt = this.db.prepare('SELECT * FROM account_state WHERE account_id = ?')
    stmt.bind([accountId])
    if (!stmt.step()) {
      stmt.free()
      return undefined
    }
    const row = stmt.getAsObject()
    stmt.free()
    return mapAccountState(row)
  }

  getAllAccountStates(): AccountStateWithInfo[] {
    const stmt = this.db.prepare(
      `SELECT account_state.*, account.service_id, account.display_name AS account_display_name
       FROM account_state
       JOIN account ON account_state.account_id = account.account_id`,
    )
    const results: AccountStateWithInfo[] = []
    while (stmt.step()) {
      const row = stmt.getAsObject()
      results.push({
        ...mapAccountState(row),
        serviceId: row.service_id as string,
        displayName: row.account_display_name as string | null,
      })
    }
    stmt.free()
    return results
  }

  upsertAccountState(state: AccountState): void {
    this.db.run(
      `INSERT INTO account_state (account_id, last_url, scroll_position_y, webview_status, last_accessed_at)
       VALUES (?, ?, ?, ?, ?)
       ON CONFLICT(account_id) DO UPDATE SET
           last_url = excluded.last_url,
           scroll_position_y = excluded.scroll_position_y,
           webview_status = excluded.webview_status,
           last_accessed_at = excluded.last_accessed_at`,
      [
        state.accountId,
        state.lastUrl,
        state.scrollPositionY,
        state.webviewStatus,
        state.lastAccessedAt,
      ],
    )
  }

  updateWebViewStatus(accountId: string, status: string, lastAccessedAt: string): void {
    this.db.run(
      'UPDATE account_state SET webview_status = ?, last_accessed_at = ? WHERE account_id = ?',
      [status, lastAccessedAt, accountId],
    )
  }

  updateLastUrl(accountId: string, lastUrl: string | null, scrollPositionY: number): void {
    this.db.run(
      'UPDATE account_state SET last_url = ?, scroll_position_y = ? WHERE account_id = ?',
      [lastUrl, scrollPositionY, accountId],
    )
  }

  markAllAsDestroyed(): void {
    this.db.run("UPDATE account_state SET webview_status = 'destroyed'")
  }

  deleteAccountState(accountId: string): void {
    this.db.run('DELETE FROM account_state WHERE account_id = ?', [accountId])
  }

  getBackgroundWebViews(): AccountState[] {
    const stmt = this.db.prepare(
      "SELECT * FROM account_state WHERE webview_status = 'background' ORDER BY last_accessed_at ASC",
    )
    const results: AccountState[] = []
    while (stmt.step()) {
      results.push(mapAccountState(stmt.getAsObject()))
    }
    stmt.free()
    return results
  }

  getBackgroundWebViewCount(): number {
    const stmt = this.db.prepare(
      "SELECT COUNT(*) AS count FROM account_state WHERE webview_status = 'background'",
    )
    stmt.step()
    const count = stmt.getAsObject().count as number
    stmt.free()
    return count
  }

  getOldestBackground(): AccountState | undefined {
    const stmt = this.db.prepare(
      "SELECT * FROM account_state WHERE webview_status = 'background' ORDER BY last_accessed_at ASC LIMIT 1",
    )
    if (!stmt.step()) {
      stmt.free()
      return undefined
    }
    const result = mapAccountState(stmt.getAsObject())
    stmt.free()
    return result
  }

  getExpiredBackground(cutoffTime: string): AccountState[] {
    const stmt = this.db.prepare(
      "SELECT * FROM account_state WHERE webview_status = 'background' AND last_accessed_at < ? ORDER BY last_accessed_at ASC",
    )
    stmt.bind([cutoffTime])
    const results: AccountState[] = []
    while (stmt.step()) {
      results.push(mapAccountState(stmt.getAsObject()))
    }
    stmt.free()
    return results
  }
}

function mapAccountState(row: Record<string, unknown>): AccountState {
  return {
    accountId: row.account_id as string,
    lastUrl: row.last_url as string | null,
    scrollPositionY: row.scroll_position_y as number,
    webviewStatus: row.webview_status as AccountState['webviewStatus'],
    lastAccessedAt: row.last_accessed_at as string,
  }
}
