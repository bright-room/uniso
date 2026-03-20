import type { Database as SqlJsDatabase } from 'sql.js'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { SessionRepository } from '../data/session-repository'
import { SessionManager, type WebViewStateSaver } from '../domain/session-manager'
import { createTestDatabase } from './test-database'

describe('SessionManager', () => {
  let db: SqlJsDatabase
  let sessionRepo: SessionRepository
  let stateSaver: WebViewStateSaver
  let manager: SessionManager

  beforeEach(async () => {
    db = await createTestDatabase()
    sessionRepo = new SessionRepository(db)
    stateSaver = { saveCurrentState: vi.fn() }
    manager = new SessionManager(sessionRepo, stateSaver)
  })

  afterEach(() => {
    manager.stopPeriodicSave()
    db.close()
  })

  it('detects non-clean shutdown on startup', () => {
    // Default app_state has clean_shutdown = 0
    expect(manager.isCleanShutdown()).toBe(false)
  })

  it('marks startup (sets clean_shutdown to false)', () => {
    sessionRepo.updateCleanShutdown(true, new Date().toISOString())
    expect(manager.isCleanShutdown()).toBe(true)

    manager.markStartup()
    expect(manager.isCleanShutdown()).toBe(false)
  })

  it('marks clean shutdown and saves state', () => {
    manager.markCleanShutdown()
    expect(manager.isCleanShutdown()).toBe(true)
    expect(stateSaver.saveCurrentState).toHaveBeenCalledOnce()
  })

  it('starts and stops periodic save', () => {
    vi.useFakeTimers()

    manager.startPeriodicSave(100)

    vi.advanceTimersByTime(350)
    expect(stateSaver.saveCurrentState).toHaveBeenCalledTimes(3)

    manager.stopPeriodicSave()

    vi.advanceTimersByTime(200)
    // No additional calls
    expect(stateSaver.saveCurrentState).toHaveBeenCalledTimes(3)

    vi.useRealTimers()
  })

  it('saveImmediate triggers state save and updates timestamp', () => {
    manager.saveImmediate()
    expect(stateSaver.saveCurrentState).toHaveBeenCalledOnce()

    const state = sessionRepo.getAppState()
    expect(state.lastSavedAt).not.toBe('')
  })

  it('restores session returning active account id', () => {
    const result = manager.restoreSession()
    expect(result).toEqual({ activeAccountId: null })
  })

  it('startFresh marks all destroyed and clears active account', () => {
    manager.startFresh()
    const state = sessionRepo.getAppState()
    expect(state.activeAccountId).toBeNull()
  })

  it('works without stateSaver', () => {
    const managerNoSaver = new SessionManager(sessionRepo)
    // Should not throw
    managerNoSaver.markCleanShutdown()
    managerNoSaver.saveImmediate()
  })

  it('restartPeriodicSave replaces the previous timer', () => {
    vi.useFakeTimers()

    manager.startPeriodicSave(100)
    vi.advanceTimersByTime(50)

    // Restart with different interval
    manager.startPeriodicSave(200)
    vi.advanceTimersByTime(150)
    // Only the restart happened, 150ms < 200ms so no tick yet
    expect(stateSaver.saveCurrentState).toHaveBeenCalledTimes(0)

    vi.advanceTimersByTime(100)
    // Now at 250ms from restart, 1 tick
    expect(stateSaver.saveCurrentState).toHaveBeenCalledTimes(1)

    manager.stopPeriodicSave()
    vi.useRealTimers()
  })
})
