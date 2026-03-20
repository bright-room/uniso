import type { Database as SqlJsDatabase } from 'sql.js'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { AccountRepository } from '../data/account-repository'
import { ServicePluginRepository } from '../data/service-plugin-repository'
import { LinkRouter } from '../domain/link-router'
import { ServicePluginRegistry } from '../domain/service-plugin-registry'
import { createTestDatabase } from './test-database'

describe('LinkRouter', () => {
  let db: SqlJsDatabase
  let accountRepo: AccountRepository
  let registry: ServicePluginRegistry
  let router: LinkRouter

  beforeEach(async () => {
    db = await createTestDatabase()
    accountRepo = new AccountRepository(db)
    const pluginRepo = new ServicePluginRepository(db)
    registry = new ServicePluginRegistry(pluginRepo)
    registry.initialize()
    router = new LinkRouter(registry, accountRepo)
  })

  afterEach(() => {
    db.close()
  })

  function insertAccount(id: string, serviceId: string) {
    accountRepo.insert({
      accountId: id,
      serviceId,
      displayName: null,
      avatarUrl: null,
      sortOrder: 0,
      createdAt: '2026-01-01T00:00:00Z',
    })
  }

  it('classifies external links (unknown domain)', () => {
    const result = router.classifyLink('https://example.com/page', 'x')
    expect(result).toEqual({ type: 'external', url: 'https://example.com/page' })
  })

  it('classifies invalid URLs as external', () => {
    const result = router.classifyLink('not-a-url', 'x')
    expect(result).toEqual({ type: 'external', url: 'not-a-url' })
  })

  it('classifies same-domain navigation', () => {
    const result = router.classifyLink('https://x.com/notifications', 'x')
    expect(result).toEqual({ type: 'same-domain' })
  })

  it('classifies same-domain with subdomain (mobile.x.com)', () => {
    const result = router.classifyLink('https://mobile.x.com/home', 'x')
    expect(result).toEqual({ type: 'same-domain' })
  })

  it('classifies internal link with single account', () => {
    insertAccount('acc-ig-1', 'instagram')

    const result = router.classifyLink('https://instagram.com/p/123', 'x')
    expect(result).toEqual({
      type: 'internal-single-account',
      url: 'https://instagram.com/p/123',
      accountId: 'acc-ig-1',
    })
  })

  it('classifies internal link with multiple accounts', () => {
    insertAccount('acc-ig-1', 'instagram')
    insertAccount('acc-ig-2', 'instagram')

    const result = router.classifyLink('https://instagram.com/p/123', 'x')
    expect(result.type).toBe('internal-multi-account')
    if (result.type === 'internal-multi-account') {
      expect(result.serviceId).toBe('instagram')
      expect(result.accountIds).toHaveLength(2)
    }
  })

  it('classifies internal link with no account (unlogged service)', () => {
    // No instagram accounts exist
    const result = router.classifyLink('https://instagram.com/p/123', 'x')
    expect(result).toEqual({
      type: 'internal-no-account',
      url: 'https://instagram.com/p/123',
      serviceId: 'instagram',
    })
  })

  it('handles twitter.com as alias for X', () => {
    insertAccount('acc-x-1', 'x')

    // Navigating from instagram to twitter.com
    const result = router.classifyLink('https://twitter.com/user', 'instagram')
    expect(result).toEqual({
      type: 'internal-single-account',
      url: 'https://twitter.com/user',
      accountId: 'acc-x-1',
    })
  })

  it('handles www subdomain for known services', () => {
    insertAccount('acc-yt-1', 'youtube')

    const result = router.classifyLink('https://www.youtube.com/watch?v=abc', 'x')
    expect(result).toEqual({
      type: 'internal-single-account',
      url: 'https://www.youtube.com/watch?v=abc',
      accountId: 'acc-yt-1',
    })
  })
})
