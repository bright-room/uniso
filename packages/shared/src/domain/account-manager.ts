import { AccountRepository } from '../data/account-repository'
import { SessionRepository } from '../data/session-repository'
import type { Account, AccountWithService } from '../types'

type Listener = () => void

export class AccountManager {
  private accounts: AccountWithService[] = []
  private activeAccountId: string | null = null
  private listeners: Set<Listener> = new Set()

  constructor(
    private accountRepo: AccountRepository,
    private sessionRepo: SessionRepository
  ) {}

  loadAccounts(): void {
    this.accounts = this.accountRepo.getAll()
    const appState = this.sessionRepo.getAppState()
    this.activeAccountId = appState.activeAccountId
    this.notify()
  }

  getAccounts(): AccountWithService[] {
    return this.accounts
  }

  getActiveAccountId(): string | null {
    return this.activeAccountId
  }

  getActiveAccount(): AccountWithService | undefined {
    if (!this.activeAccountId) return undefined
    return this.accounts.find((a) => a.accountId === this.activeAccountId)
  }

  addAccount(serviceId: string): AccountWithService {
    const now = new Date().toISOString()
    const accountId = crypto.randomUUID()
    const sortOrder = this.accountRepo.getNextSortOrder(serviceId)
    const count = this.accountRepo.getCountByServiceId(serviceId)

    const account: Account = {
      accountId,
      serviceId,
      displayName: null,
      avatarUrl: null,
      sortOrder,
      createdAt: now,
    }

    this.accountRepo.insert(account)

    // Initialize account state
    this.sessionRepo.upsertAccountState({
      accountId,
      lastUrl: null,
      scrollPositionY: 0,
      webviewStatus: 'active',
      lastAccessedAt: now,
    })

    // Set as active
    this.activeAccountId = accountId
    this.sessionRepo.updateActiveAccount(accountId, now)

    this.accounts = this.accountRepo.getAll()
    this.notify()

    // Return the newly created account with service info
    const withService = this.accounts.find((a) => a.accountId === accountId)!
    // Set default display name based on count
    if (!withService.displayName) {
      const displayName = `${withService.serviceDisplayName} ${count + 1}`
      this.accountRepo.updateProfile(accountId, displayName, null)
      withService.displayName = displayName
    }

    return withService
  }

  removeAccount(accountId: string): void {
    const now = new Date().toISOString()

    this.sessionRepo.deleteAccountState(accountId)
    this.accountRepo.delete(accountId)

    this.accounts = this.accountRepo.getAll()

    if (this.activeAccountId === accountId) {
      this.activeAccountId = this.accounts.length > 0 ? this.accounts[0].accountId : null
      this.sessionRepo.updateActiveAccount(this.activeAccountId, now)
    }

    this.notify()
  }

  setActiveAccount(accountId: string): void {
    const account = this.accounts.find((a) => a.accountId === accountId)
    if (!account) return

    const now = new Date().toISOString()
    this.activeAccountId = accountId
    this.sessionRepo.updateActiveAccount(accountId, now)
    this.notify()
  }

  getNextAccountId(): string | null {
    if (this.accounts.length === 0 || !this.activeAccountId) return null
    const idx = this.accounts.findIndex((a) => a.accountId === this.activeAccountId)
    const nextIdx = (idx + 1) % this.accounts.length
    return this.accounts[nextIdx].accountId
  }

  getPrevAccountId(): string | null {
    if (this.accounts.length === 0 || !this.activeAccountId) return null
    const idx = this.accounts.findIndex((a) => a.accountId === this.activeAccountId)
    const prevIdx = (idx - 1 + this.accounts.length) % this.accounts.length
    return this.accounts[prevIdx].accountId
  }

  onChange(listener: Listener): () => void {
    this.listeners.add(listener)
    return () => this.listeners.delete(listener)
  }

  private notify(): void {
    for (const listener of this.listeners) {
      listener()
    }
  }
}
