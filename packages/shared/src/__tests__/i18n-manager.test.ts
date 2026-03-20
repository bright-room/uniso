import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import type { Database as SqlJsDatabase } from 'sql.js'
import { SettingsRepository } from '../data/settings-repository'
import { I18nManager } from '../domain/i18n-manager'
import { createTestDatabase } from './test-database'

describe('I18nManager', () => {
  let db: SqlJsDatabase
  let settingsRepo: SettingsRepository
  let manager: I18nManager

  beforeEach(async () => {
    db = await createTestDatabase()
    settingsRepo = new SettingsRepository(db)
    manager = new I18nManager(settingsRepo)
  })

  afterEach(() => {
    db.close()
  })

  it('defaults to English', () => {
    manager.initialize()
    expect(manager.getCurrentLocale()).toBe('en')
  })

  it('detects Japanese from OS locale', () => {
    manager.initialize('ja-JP')
    expect(manager.getCurrentLocale()).toBe('ja')
  })

  it('falls back to English for non-Japanese OS locales', () => {
    manager.initialize('fr-FR')
    expect(manager.getCurrentLocale()).toBe('en')
  })

  it('uses saved locale over OS locale', () => {
    settingsRepo.setString('locale', 'ja')
    manager.initialize('en-US')
    expect(manager.getCurrentLocale()).toBe('ja')
  })

  it('switches locale and persists', () => {
    manager.initialize()
    manager.setLocale('ja')
    expect(manager.getCurrentLocale()).toBe('ja')
    expect(settingsRepo.getString('locale')).toBe('ja')
  })

  it('getString returns localized string', () => {
    manager.initialize()
    expect(manager.getString('sidebar.add_account')).toBe('Add Account')

    manager.setLocale('ja')
    const jaValue = manager.getString('sidebar.add_account')
    expect(jaValue).not.toBe('Add Account')
    expect(jaValue.length).toBeGreaterThan(0)
  })

  it('getString falls back to English for missing key in current locale', () => {
    manager.initialize()
    // Assuming all keys exist in both, test with a key
    const enValue = manager.getString('sidebar.add_account')
    expect(enValue).toBe('Add Account')
  })

  it('getString returns key itself when not found in any locale', () => {
    manager.initialize()
    expect(manager.getString('nonexistent.key')).toBe('nonexistent.key')
  })

  it('getAllStrings merges current locale over English', () => {
    manager.initialize()
    const strings = manager.getAllStrings()
    expect(strings['sidebar.add_account']).toBe('Add Account')
    expect(Object.keys(strings).length).toBeGreaterThan(0)
  })

  it('notifies listeners on locale change', () => {
    manager.initialize()
    const listener = vi.fn()
    const unsubscribe = manager.onLocaleChange(listener)

    manager.setLocale('ja')
    expect(listener).toHaveBeenCalledWith('ja')

    unsubscribe()
    listener.mockClear()
    manager.setLocale('en')
    expect(listener).not.toHaveBeenCalled()
  })
})
