import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import type { Database as SqlJsDatabase } from 'sql.js'
import { SettingsRepository } from '../data/settings-repository'
import { IdentityManager } from '../domain/identity-manager'
import { createTestDatabase } from './test-database'

describe('IdentityManager', () => {
  let db: SqlJsDatabase
  let settingsRepo: SettingsRepository
  let manager: IdentityManager

  beforeEach(async () => {
    db = await createTestDatabase()
    settingsRepo = new SettingsRepository(db)
    manager = new IdentityManager(settingsRepo)
  })

  afterEach(() => {
    db.close()
  })

  it('generates a UUID on first call', () => {
    const id = manager.getOrCreateLocalUserId()
    expect(id).toBeDefined()
    expect(id.length).toBeGreaterThan(0)
    // UUID v4 format
    expect(id).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i)
  })

  it('returns the same ID on subsequent calls (idempotent)', () => {
    const id1 = manager.getOrCreateLocalUserId()
    const id2 = manager.getOrCreateLocalUserId()
    expect(id1).toBe(id2)
  })

  it('persists the ID across manager instances', () => {
    const id1 = manager.getOrCreateLocalUserId()

    // Create a new manager instance with the same repo
    const manager2 = new IdentityManager(settingsRepo)
    const id2 = manager2.getOrCreateLocalUserId()
    expect(id1).toBe(id2)
  })
})
