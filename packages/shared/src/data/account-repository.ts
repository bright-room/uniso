import type { Database as SqlJsDatabase } from 'sql.js'
import type { Account, AccountWithService } from '../types/account'

export class AccountRepository {
  constructor(private db: SqlJsDatabase) {}

  getAll(): AccountWithService[] {
    const stmt = this.db.prepare(
      `SELECT account.account_id, account.service_id, account.display_name,
              account.avatar_url, account.sort_order, account.created_at,
              service_plugin.display_name AS service_display_name,
              service_plugin.brand_color, service_plugin.icon_resource
       FROM account
       JOIN service_plugin ON account.service_id = service_plugin.service_id
       ORDER BY service_plugin.sort_order, account.sort_order`
    )
    const results: AccountWithService[] = []
    while (stmt.step()) {
      const row = stmt.getAsObject()
      results.push({
        accountId: row.account_id as string,
        serviceId: row.service_id as string,
        displayName: row.display_name as string | null,
        avatarUrl: row.avatar_url as string | null,
        sortOrder: row.sort_order as number,
        createdAt: row.created_at as string,
        serviceDisplayName: row.service_display_name as string,
        brandColor: row.brand_color as string,
        iconResource: row.icon_resource as string,
      })
    }
    stmt.free()
    return results
  }

  getById(accountId: string): Account | undefined {
    const stmt = this.db.prepare('SELECT * FROM account WHERE account_id = ?')
    stmt.bind([accountId])
    if (!stmt.step()) {
      stmt.free()
      return undefined
    }
    const row = stmt.getAsObject()
    stmt.free()
    return {
      accountId: row.account_id as string,
      serviceId: row.service_id as string,
      displayName: row.display_name as string | null,
      avatarUrl: row.avatar_url as string | null,
      sortOrder: row.sort_order as number,
      createdAt: row.created_at as string,
    }
  }

  getByServiceId(serviceId: string): Account[] {
    const stmt = this.db.prepare(
      'SELECT * FROM account WHERE service_id = ? ORDER BY sort_order'
    )
    stmt.bind([serviceId])
    const results: Account[] = []
    while (stmt.step()) {
      const row = stmt.getAsObject()
      results.push({
        accountId: row.account_id as string,
        serviceId: row.service_id as string,
        displayName: row.display_name as string | null,
        avatarUrl: row.avatar_url as string | null,
        sortOrder: row.sort_order as number,
        createdAt: row.created_at as string,
      })
    }
    stmt.free()
    return results
  }

  getCount(): number {
    const stmt = this.db.prepare('SELECT COUNT(*) AS count FROM account')
    stmt.step()
    const count = stmt.getAsObject().count as number
    stmt.free()
    return count
  }

  getCountByServiceId(serviceId: string): number {
    const stmt = this.db.prepare(
      'SELECT COUNT(*) AS count FROM account WHERE service_id = ?'
    )
    stmt.bind([serviceId])
    stmt.step()
    const count = stmt.getAsObject().count as number
    stmt.free()
    return count
  }

  getNextSortOrder(serviceId: string): number {
    const stmt = this.db.prepare(
      'SELECT COALESCE(MAX(sort_order), -1) + 1 AS next FROM account WHERE service_id = ?'
    )
    stmt.bind([serviceId])
    stmt.step()
    const next = stmt.getAsObject().next as number
    stmt.free()
    return next
  }

  insert(account: Account): void {
    this.db.run(
      `INSERT INTO account (account_id, service_id, display_name, avatar_url, sort_order, created_at)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [
        account.accountId,
        account.serviceId,
        account.displayName,
        account.avatarUrl,
        account.sortOrder,
        account.createdAt,
      ]
    )
  }

  updateProfile(accountId: string, displayName: string | null, avatarUrl: string | null): void {
    this.db.run(
      'UPDATE account SET display_name = ?, avatar_url = ? WHERE account_id = ?',
      [displayName, avatarUrl, accountId]
    )
  }

  updateSortOrder(accountId: string, sortOrder: number): void {
    this.db.run('UPDATE account SET sort_order = ? WHERE account_id = ?', [sortOrder, accountId])
  }

  delete(accountId: string): void {
    this.db.run('DELETE FROM account WHERE account_id = ?', [accountId])
  }
}
