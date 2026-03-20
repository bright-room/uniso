import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import type { Database as SqlJsDatabase } from 'sql.js'
import { SettingsRepository } from '../data/settings-repository'
import { createTestDatabase } from './test-database'

describe('SettingsRepository', () => {
  let db: SqlJsDatabase
  let repo: SettingsRepository

  beforeEach(async () => {
    db = await createTestDatabase()
    repo = new SettingsRepository(db)
  })

  afterEach(() => {
    db.close()
  })

  it('reads seeded settings', () => {
    expect(repo.getString('telemetry_enabled')).toBe('false')
    expect(repo.getString('tutorial_completed')).toBe('false')
  })

  it('returns undefined for non-existent key', () => {
    expect(repo.getString('nonexistent')).toBeUndefined()
  })

  it('sets and gets string value', () => {
    repo.setString('locale', 'ja')
    expect(repo.getString('locale')).toBe('ja')
  })

  it('upserts existing key', () => {
    repo.setString('locale', 'ja')
    repo.setString('locale', 'en')
    expect(repo.getString('locale')).toBe('en')
  })

  it('gets and sets boolean values', () => {
    expect(repo.getBoolean('telemetry_enabled')).toBe(false)

    repo.setBoolean('telemetry_enabled', true)
    expect(repo.getBoolean('telemetry_enabled')).toBe(true)
  })

  it('getBoolean returns undefined for non-existent key', () => {
    expect(repo.getBoolean('nonexistent')).toBeUndefined()
  })

  it('getAll returns all settings', () => {
    const all = repo.getAll()
    expect(all.length).toBeGreaterThanOrEqual(5)
    expect(all.find((s) => s.key === 'locale')).toBeDefined()
  })

  it('deletes a setting', () => {
    repo.setString('custom_key', 'value')
    repo.delete('custom_key')
    expect(repo.getString('custom_key')).toBeUndefined()
  })

  it('manages local user', () => {
    expect(repo.getLocalUser()).toBeUndefined()

    repo.insertLocalUser('user-1', '2026-01-01T00:00:00Z')
    const user = repo.getLocalUser()
    expect(user).toBeDefined()
    expect(user!.id).toBe('user-1')
    expect(user!.createdAt).toBe('2026-01-01T00:00:00Z')
  })
})
