import type { Database as SqlJsDatabase } from 'sql.js'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AccountRepository } from '../data/account-repository'
import { SessionRepository } from '../data/session-repository'
import { AccountManager } from '../domain/account-manager'
import { createTestDatabase } from './test-database'

describe('AccountManager', () => {
  let db: SqlJsDatabase
  let accountRepo: AccountRepository
  let sessionRepo: SessionRepository
  let manager: AccountManager

  beforeEach(async () => {
    db = await createTestDatabase()
    accountRepo = new AccountRepository(db)
    sessionRepo = new SessionRepository(db)
    manager = new AccountManager(accountRepo, sessionRepo)
  })

  afterEach(() => {
    db.close()
  })

  it('starts with no accounts and no active account', () => {
    manager.loadAccounts()
    expect(manager.getAccounts()).toEqual([])
    expect(manager.getActiveAccountId()).toBeNull()
    expect(manager.getActiveAccount()).toBeUndefined()
  })

  it('adds an account and sets it as active', () => {
    manager.loadAccounts()
    const account = manager.addAccount('x')

    expect(account.serviceId).toBe('x')
    expect(account.displayName).toBe('X (旧Twitter) 1')
    expect(manager.getAccounts()).toHaveLength(1)
    expect(manager.getActiveAccountId()).toBe(account.accountId)
    expect(manager.getActiveAccount()?.accountId).toBe(account.accountId)
  })

  it('adds multiple accounts for same service with incremental names', () => {
    manager.loadAccounts()
    const acc1 = manager.addAccount('x')
    const acc2 = manager.addAccount('x')

    expect(acc1.displayName).toBe('X (旧Twitter) 1')
    expect(acc2.displayName).toBe('X (旧Twitter) 2')
    expect(manager.getAccounts()).toHaveLength(2)
    // Last added should be active
    expect(manager.getActiveAccountId()).toBe(acc2.accountId)
  })

  it('removes an account', () => {
    manager.loadAccounts()
    const acc1 = manager.addAccount('x')
    const acc2 = manager.addAccount('instagram')

    manager.removeAccount(acc2.accountId)
    expect(manager.getAccounts()).toHaveLength(1)
    expect(manager.getAccounts()[0].accountId).toBe(acc1.accountId)
  })

  it('auto-switches to first account when active is deleted', () => {
    manager.loadAccounts()
    const acc1 = manager.addAccount('x')
    const acc2 = manager.addAccount('instagram')

    // acc2 is active (last added)
    expect(manager.getActiveAccountId()).toBe(acc2.accountId)

    manager.removeAccount(acc2.accountId)
    expect(manager.getActiveAccountId()).toBe(acc1.accountId)
  })

  it('sets active account to null when last account is deleted', () => {
    manager.loadAccounts()
    const acc = manager.addAccount('x')

    manager.removeAccount(acc.accountId)
    expect(manager.getActiveAccountId()).toBeNull()
    expect(manager.getAccounts()).toHaveLength(0)
  })

  it('switches active account', () => {
    manager.loadAccounts()
    const acc1 = manager.addAccount('x')
    manager.addAccount('instagram')

    manager.setActiveAccount(acc1.accountId)
    expect(manager.getActiveAccountId()).toBe(acc1.accountId)
  })

  it('ignores switching to non-existent account', () => {
    manager.loadAccounts()
    const acc = manager.addAccount('x')

    manager.setActiveAccount('non-existent')
    expect(manager.getActiveAccountId()).toBe(acc.accountId)
  })

  it('gets next and previous account ids (circular)', () => {
    manager.loadAccounts()
    const acc1 = manager.addAccount('x')
    const acc2 = manager.addAccount('instagram')
    const acc3 = manager.addAccount('youtube')

    // acc3 is active (last added), but account order is by service sort_order: x, ig, yt
    manager.setActiveAccount(acc1.accountId)

    expect(manager.getNextAccountId()).toBe(acc2.accountId)
    expect(manager.getPrevAccountId()).toBe(acc3.accountId)

    // Wrap around
    manager.setActiveAccount(acc3.accountId)
    expect(manager.getNextAccountId()).toBe(acc1.accountId)
  })

  it('returns null for next/prev when no accounts', () => {
    manager.loadAccounts()
    expect(manager.getNextAccountId()).toBeNull()
    expect(manager.getPrevAccountId()).toBeNull()
  })

  it('notifies listeners on changes', () => {
    manager.loadAccounts()
    const listener = vi.fn()
    const unsubscribe = manager.onChange(listener)

    manager.addAccount('x')
    expect(listener).toHaveBeenCalled()

    unsubscribe()
    listener.mockClear()
    manager.addAccount('instagram')
    expect(listener).not.toHaveBeenCalled()
  })

  it('removeAccount on empty list is a no-op', () => {
    manager.loadAccounts()
    // Should not throw
    manager.removeAccount('non-existent')
    expect(manager.getAccounts()).toHaveLength(0)
  })
})
