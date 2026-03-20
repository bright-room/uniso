import type { Database as SqlJsDatabase } from 'sql.js'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { ServicePluginRepository } from '../data/service-plugin-repository'
import { ServicePluginRegistry } from '../domain/service-plugin-registry'
import { createTestDatabase } from './test-database'

describe('ServicePluginRegistry', () => {
  let db: SqlJsDatabase
  let registry: ServicePluginRegistry

  beforeEach(async () => {
    db = await createTestDatabase()
    const repo = new ServicePluginRepository(db)
    registry = new ServicePluginRegistry(repo)
    registry.initialize()
  })

  afterEach(() => {
    db.close()
  })

  it('loads all seeded service plugins', () => {
    const plugins = registry.getAll()
    expect(plugins).toHaveLength(6)
    expect(plugins.map((p) => p.serviceId)).toEqual([
      'x',
      'instagram',
      'facebook',
      'youtube',
      'bluesky',
      'twitch',
    ])
  })

  it('gets plugin by id', () => {
    const plugin = registry.getById('x')
    expect(plugin).toBeDefined()
    expect(plugin?.displayName).toBe('X (旧Twitter)')
    expect(plugin?.domainPatterns).toEqual(['x.com', 'twitter.com'])
  })

  it('returns undefined for non-existent id', () => {
    expect(registry.getById('nonexistent')).toBeUndefined()
  })

  it('finds service by direct domain match', () => {
    expect(registry.findByDomain('x.com')).toBe('x')
    expect(registry.findByDomain('twitter.com')).toBe('x')
    expect(registry.findByDomain('instagram.com')).toBe('instagram')
    expect(registry.findByDomain('youtube.com')).toBe('youtube')
    expect(registry.findByDomain('bsky.app')).toBe('bluesky')
    expect(registry.findByDomain('twitch.tv')).toBe('twitch')
  })

  it('finds service by subdomain match', () => {
    expect(registry.findByDomain('www.x.com')).toBe('x')
    expect(registry.findByDomain('mobile.x.com')).toBe('x')
    expect(registry.findByDomain('www.instagram.com')).toBe('instagram')
    expect(registry.findByDomain('www.youtube.com')).toBe('youtube')
    expect(registry.findByDomain('m.twitch.tv')).toBe('twitch')
  })

  it('returns undefined for unknown domain', () => {
    expect(registry.findByDomain('example.com')).toBeUndefined()
    expect(registry.findByDomain('google.com')).toBeUndefined()
  })

  it('gets default URL for a service', () => {
    expect(registry.getDefaultUrl('x')).toBe('https://x.com')
    expect(registry.getDefaultUrl('instagram')).toBe('https://instagram.com')
    expect(registry.getDefaultUrl('bluesky')).toBe('https://bsky.app')
  })

  it('returns undefined for default URL of non-existent service', () => {
    expect(registry.getDefaultUrl('nonexistent')).toBeUndefined()
  })
})
