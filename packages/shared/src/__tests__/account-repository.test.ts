import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import type { Database as SqlJsDatabase } from 'sql.js'
import { AccountRepository } from '../data/account-repository'
import { createTestDatabase } from './test-database'

describe('AccountRepository', () => {
  let db: SqlJsDatabase
  let repo: AccountRepository

  beforeEach(async () => {
    db = await createTestDatabase()
    repo = new AccountRepository(db)
  })

  afterEach(() => {
    db.close()
  })

  it('returns empty list when no accounts exist', () => {
    expect(repo.getAll()).toEqual([])
    expect(repo.getCount()).toBe(0)
  })

  it('inserts and retrieves an account', () => {
    repo.insert({
      accountId: 'acc-1',
      serviceId: 'x',
      displayName: 'My X',
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })

    const account = repo.getById('acc-1')
    expect(account).toBeDefined()
    expect(account!.accountId).toBe('acc-1')
    expect(account!.serviceId).toBe('x')
    expect(account!.displayName).toBe('My X')
    expect(repo.getCount()).toBe(1)
  })

  it('getAll returns accounts with service info, ordered by service then sort_order', () => {
    repo.insert({
      accountId: 'acc-ig',
      serviceId: 'instagram',
      displayName: 'IG',
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })
    repo.insert({
      accountId: 'acc-x',
      serviceId: 'x',
      displayName: 'X1',
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })

    const all = repo.getAll()
    expect(all).toHaveLength(2)
    // X (sort_order 0) comes before Instagram (sort_order 1)
    expect(all[0].serviceId).toBe('x')
    expect(all[1].serviceId).toBe('instagram')
    expect(all[0].serviceDisplayName).toBe('X (旧Twitter)')
    expect(all[0].brandColor).toBe('#000000')
  })

  it('getByServiceId filters correctly', () => {
    repo.insert({
      accountId: 'acc-1',
      serviceId: 'x',
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })
    repo.insert({
      accountId: 'acc-2',
      serviceId: 'x',
      displayName: null,
      avatarUrl: null,
      sortOrder: 1,
      createdAt: '2026-01-01T00:00:00Z',
    })
    repo.insert({
      accountId: 'acc-3',
      serviceId: 'instagram',
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })

    const xAccounts = repo.getByServiceId('x')
    expect(xAccounts).toHaveLength(2)
    expect(repo.getCountByServiceId('x')).toBe(2)
    expect(repo.getCountByServiceId('instagram')).toBe(1)
  })

  it('getNextSortOrder returns next sequential order', () => {
    expect(repo.getNextSortOrder('x')).toBe(0)

    repo.insert({
      accountId: 'acc-1',
      serviceId: 'x',
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })

    expect(repo.getNextSortOrder('x')).toBe(1)
  })

  it('updates profile', () => {
    repo.insert({
      accountId: 'acc-1',
      serviceId: 'x',
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })

    repo.updateProfile('acc-1', 'Updated Name', 'https://example.com/avatar.png')
    const account = repo.getById('acc-1')
    expect(account!.displayName).toBe('Updated Name')
    expect(account!.avatarUrl).toBe('https://example.com/avatar.png')
  })

  it('updates sort order', () => {
    repo.insert({
      accountId: 'acc-1',
      serviceId: 'x',
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })

    repo.updateSortOrder('acc-1', 5)
    expect(repo.getById('acc-1')!.sortOrder).toBe(5)
  })

  it('deletes an account', () => {
    repo.insert({
      accountId: 'acc-1',
      serviceId: 'x',
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })

    repo.delete('acc-1')
    expect(repo.getById('acc-1')).toBeUndefined()
    expect(repo.getCount()).toBe(0)
  })

  it('getById returns undefined for non-existent account', () => {
    expect(repo.getById('non-existent')).toBeUndefined()
  })
})
