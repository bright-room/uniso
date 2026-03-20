import type { SessionRepository } from '../data/session-repository'

export interface WebViewStateSaver {
  saveCurrentState(): void
}

export class SessionManager {
  private intervalId: ReturnType<typeof setInterval> | null = null

  constructor(
    private sessionRepo: SessionRepository,
    private stateSaver?: WebViewStateSaver,
  ) {}

  isCleanShutdown(): boolean {
    return this.sessionRepo.getAppState().cleanShutdown
  }

  markStartup(): void {
    this.sessionRepo.updateCleanShutdown(false, new Date().toISOString())
  }

  markCleanShutdown(): void {
    this.stateSaver?.saveCurrentState()
    this.sessionRepo.updateCleanShutdown(true, new Date().toISOString())
  }

  startPeriodicSave(intervalMs: number = 30000): void {
    this.stopPeriodicSave()
    this.intervalId = setInterval(() => {
      this.saveImmediate()
    }, intervalMs)
  }

  stopPeriodicSave(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId)
      this.intervalId = null
    }
  }

  saveImmediate(): void {
    this.stateSaver?.saveCurrentState()
    this.sessionRepo.updateLastSavedAt(new Date().toISOString())
  }

  restoreSession(): { activeAccountId: string | null } {
    const appState = this.sessionRepo.getAppState()
    return { activeAccountId: appState.activeAccountId }
  }

  startFresh(): void {
    this.sessionRepo.markAllAsDestroyed()
    this.sessionRepo.updateActiveAccount(null, new Date().toISOString())
  }
}
