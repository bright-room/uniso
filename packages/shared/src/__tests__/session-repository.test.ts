import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import type { Database as SqlJsDatabase } from 'sql.js'
import { AccountRepository } from '../data/account-repository'
import { SessionRepository } from '../data/session-repository'
import { createTestDatabase } from './test-database'

describe('SessionRepository', () => {
  let db: SqlJsDatabase
  let repo: SessionRepository
  let accountRepo: AccountRepository

  beforeEach(async () => {
    db = await createTestDatabase()
    repo = new SessionRepository(db)
    accountRepo = new AccountRepository(db)
  })

  afterEach(() => {
    db.close()
  })

  function insertAccount(id: string, serviceId = 'x') {
    accountRepo.insert({
      accountId: id,
      serviceId,
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })
  }

  describe('app_state', () => {
    it('returns initial app state', () => {
      const state = repo.getAppState()
      expect(state.id).toBe(1)
      expect(state.activeAccountId).toBeNull()
      expect(state.cleanShutdown).toBe(false)
      expect(state.lastSavedAt).toBe('')
    })

    it('updates active account', () => {
      insertAccount('acc-1')
      repo.updateActiveAccount('acc-1', '2026-01-01T12:00:00Z')
      const state = repo.getAppState()
      expect(state.activeAccountId).toBe('acc-1')
      expect(state.lastSavedAt).toBe('2026-01-01T12:00:00Z')
    })

    it('updates clean shutdown flag', () => {
      repo.updateCleanShutdown(true, '2026-01-01T12:00:00Z')
      expect(repo.getAppState().cleanShutdown).toBe(true)

      repo.updateCleanShutdown(false, '2026-01-01T13:00:00Z')
      expect(repo.getAppState().cleanShutdown).toBe(false)
    })

    it('updates last saved at', () => {
      repo.updateLastSavedAt('2026-03-21T00:00:00Z')
      expect(repo.getAppState().lastSavedAt).toBe('2026-03-21T00:00:00Z')
    })
  })

  describe('account_state', () => {
    it('returns undefined for non-existent account state', () => {
      expect(repo.getAccountState('non-existent')).toBeUndefined()
    })

    it('upserts and retrieves account state', () => {
      insertAccount('acc-1')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: 'https://x.com/home',
        scrollPositionY: 100,
        webviewStatus: 'active',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })

      const state = repo.getAccountState('acc-1')
      expect(state).toBeDefined()
      expect(state!.lastUrl).toBe('https://x.com/home')
      expect(state!.scrollPositionY).toBe(100)
      expect(state!.webviewStatus).toBe('active')
    })

    it('upsert updates existing state', () => {
      insertAccount('acc-1')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: 'https://x.com/home',
        scrollPositionY: 0,
        webviewStatus: 'active',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: 'https://x.com/notifications',
        scrollPositionY: 200,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T13:00:00Z',
      })

      const state = repo.getAccountState('acc-1')
      expect(state!.lastUrl).toBe('https://x.com/notifications')
      expect(state!.webviewStatus).toBe('background')
    })

    it('updates webview status', () => {
      insertAccount('acc-1')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'active',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })

      repo.updateWebViewStatus('acc-1', 'background', '2026-01-01T13:00:00Z')
      expect(repo.getAccountState('acc-1')!.webviewStatus).toBe('background')
    })

    it('updates last URL and scroll position', () => {
      insertAccount('acc-1')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'active',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })

      repo.updateLastUrl('acc-1', 'https://x.com/explore', 500)
      const state = repo.getAccountState('acc-1')
      expect(state!.lastUrl).toBe('https://x.com/explore')
      expect(state!.scrollPositionY).toBe(500)
    })

    it('marks all as destroyed', () => {
      insertAccount('acc-1')
      insertAccount('acc-2', 'instagram')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'active',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })
      repo.upsertAccountState({
        accountId: 'acc-2',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })

      repo.markAllAsDestroyed()
      expect(repo.getAccountState('acc-1')!.webviewStatus).toBe('destroyed')
      expect(repo.getAccountState('acc-2')!.webviewStatus).toBe('destroyed')
    })

    it('gets background webviews ordered by last accessed', () => {
      insertAccount('acc-1')
      insertAccount('acc-2', 'instagram')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T13:00:00Z',
      })
      repo.upsertAccountState({
        accountId: 'acc-2',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })

      const bgs = repo.getBackgroundWebViews()
      expect(bgs).toHaveLength(2)
      // Oldest first
      expect(bgs[0].accountId).toBe('acc-2')
      expect(repo.getBackgroundWebViewCount()).toBe(2)
    })

    it('gets oldest background', () => {
      insertAccount('acc-1')
      insertAccount('acc-2', 'instagram')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T13:00:00Z',
      })
      repo.upsertAccountState({
        accountId: 'acc-2',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })

      const oldest = repo.getOldestBackground()
      expect(oldest!.accountId).toBe('acc-2')
    })

    it('gets expired background webviews', () => {
      insertAccount('acc-1')
      insertAccount('acc-2', 'instagram')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })
      repo.upsertAccountState({
        accountId: 'acc-2',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'background',
        lastAccessedAt: '2026-01-01T14:00:00Z',
      })

      const expired = repo.getExpiredBackground('2026-01-01T13:00:00Z')
      expect(expired).toHaveLength(1)
      expect(expired[0].accountId).toBe('acc-1')
    })

    it('deletes account state', () => {
      insertAccount('acc-1')
      repo.upsertAccountState({
        accountId: 'acc-1',
        lastUrl: null,
        scrollPositionY: 0,
        webviewStatus: 'active',
        lastAccessedAt: '2026-01-01T12:00:00Z',
      })

      repo.deleteAccountState('acc-1')
      expect(repo.getAccountState('acc-1')).toBeUndefined()
    })
  })
})
