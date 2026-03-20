import type { AccountListItem, ServicePlugin } from '@uniso/ui'

interface Api {
  addAccount(serviceId: string): Promise<AccountListItem>
  switchAccount(accountId: string): Promise<void>
  removeAccount(accountId: string): Promise<void>
  listAccounts(): Promise<AccountListItem[]>
  getActiveAccount(): Promise<string | null>
  getAccountUrl(): Promise<string | null>
  getServicePlugins(): Promise<ServicePlugin[]>
  getI18nStrings(): Promise<Record<string, string>>
  getLocale(): Promise<string>
  setLocale(locale: string): Promise<void>
  getSetting(key: string): Promise<string | null>
  setSetting(key: string, value: string): Promise<void>
  restoreSession(): Promise<{ activeAccountId: string | null }>
  startFresh(): Promise<void>
  checkForUpdates(): Promise<unknown>
  downloadUpdate(): Promise<void>
  installUpdate(): Promise<void>
  onUpdateAvailable(
    callback: (data: { version: string; releaseNotes: unknown }) => void,
  ): () => void
  onUpdateNotAvailable(callback: () => void): () => void
  onUpdateDownloaded(callback: () => void): () => void
  reloadActive(): Promise<void>
  forceReloadActive(): Promise<void>
  hideWebViews(): Promise<void>
  showWebViews(): Promise<void>
  showContextMenu(
    accountId: string,
    serviceDisplayName: string,
    displayName: string | null,
  ): Promise<void>
  onContextMenuDelete(callback: (accountId: string) => void): () => void
  onAccountsChanged(callback: (accounts: AccountListItem[]) => void): () => void
  onUrlChanged(callback: (data: { accountId: string; url: string }) => void): () => void
  onLocaleChanged(
    callback: (data: { locale: string; strings: Record<string, string> }) => void,
  ): () => void
  onShowCrashRecovery(callback: () => void): () => void
  onShowAccountSelect(
    callback: (data: { url: string; serviceId: string; accountIds: string[] }) => void,
  ): () => void
}

declare global {
  interface Window {
    api: Api
  }
}

export {}
