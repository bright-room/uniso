import { contextBridge, ipcRenderer } from 'electron'

export interface AccountListItem {
  accountId: string
  serviceId: string
  displayName: string | null
  brandColor: string
  iconResource: string
  serviceDisplayName: string
  isActive: boolean
}

export interface ServicePlugin {
  serviceId: string
  displayName: string
  domainPatterns: string[]
  brandColor: string
  iconResource: string
  authType: string
  sortOrder: number
}

const api = {
  // Account management
  addAccount: (serviceId: string) => ipcRenderer.invoke('add-account', serviceId),
  switchAccount: (accountId: string) => ipcRenderer.invoke('switch-account', accountId),
  removeAccount: (accountId: string) => ipcRenderer.invoke('remove-account', accountId),
  listAccounts: () => ipcRenderer.invoke('list-accounts') as Promise<AccountListItem[]>,
  getActiveAccount: () => ipcRenderer.invoke('get-active-account') as Promise<string | null>,
  getAccountUrl: () => ipcRenderer.invoke('get-account-url') as Promise<string | null>,

  // Service plugins
  getServicePlugins: () => ipcRenderer.invoke('get-service-plugins') as Promise<ServicePlugin[]>,

  // i18n
  getI18nStrings: () => ipcRenderer.invoke('get-i18n-strings') as Promise<Record<string, string>>,
  getLocale: () => ipcRenderer.invoke('get-locale') as Promise<string>,
  setLocale: (locale: string) => ipcRenderer.invoke('set-locale', locale),

  // Settings
  getSetting: (key: string) => ipcRenderer.invoke('get-settings', key),
  setSetting: (key: string, value: string) => ipcRenderer.invoke('set-setting', key, value),

  // Session
  restoreSession: () => ipcRenderer.invoke('restore-session'),
  startFresh: () => ipcRenderer.invoke('start-fresh'),

  // WebView control
  reloadActive: () => ipcRenderer.invoke('reload-active'),
  forceReloadActive: () => ipcRenderer.invoke('force-reload-active'),
  hideWebViews: () => ipcRenderer.invoke('hide-webviews'),
  showWebViews: () => ipcRenderer.invoke('show-webviews'),

  // Auto-update
  checkForUpdates: () => ipcRenderer.invoke('check-for-updates'),
  downloadUpdate: () => ipcRenderer.invoke('download-update'),
  installUpdate: () => ipcRenderer.invoke('install-update'),
  onUpdateAvailable: (callback: (data: { version: string; releaseNotes: unknown }) => void) => {
    const handler = (
      _event: Electron.IpcRendererEvent,
      data: { version: string; releaseNotes: unknown },
    ) => callback(data)
    ipcRenderer.on('update-available', handler)
    return () => ipcRenderer.removeListener('update-available', handler)
  },
  onUpdateNotAvailable: (callback: () => void) => {
    const handler = () => callback()
    ipcRenderer.on('update-not-available', handler)
    return () => ipcRenderer.removeListener('update-not-available', handler)
  },
  onUpdateDownloaded: (callback: () => void) => {
    const handler = () => callback()
    ipcRenderer.on('update-downloaded', handler)
    return () => ipcRenderer.removeListener('update-downloaded', handler)
  },

  // Native context menu
  showContextMenu: (accountId: string, serviceDisplayName: string, displayName: string | null) =>
    ipcRenderer.invoke('show-context-menu', accountId, serviceDisplayName, displayName),
  onContextMenuDelete: (callback: (accountId: string) => void) => {
    const handler = (_event: Electron.IpcRendererEvent, accountId: string) => callback(accountId)
    ipcRenderer.on('context-menu-delete', handler)
    return () => ipcRenderer.removeListener('context-menu-delete', handler)
  },

  // Event listeners (main -> renderer)
  onAccountsChanged: (callback: (accounts: AccountListItem[]) => void) => {
    const handler = (_event: Electron.IpcRendererEvent, accounts: AccountListItem[]) =>
      callback(accounts)
    ipcRenderer.on('accounts-changed', handler)
    return () => ipcRenderer.removeListener('accounts-changed', handler)
  },
  onUrlChanged: (callback: (data: { accountId: string; url: string }) => void) => {
    const handler = (_event: Electron.IpcRendererEvent, data: { accountId: string; url: string }) =>
      callback(data)
    ipcRenderer.on('url-changed', handler)
    return () => ipcRenderer.removeListener('url-changed', handler)
  },
  onLocaleChanged: (
    callback: (data: { locale: string; strings: Record<string, string> }) => void,
  ) => {
    const handler = (
      _event: Electron.IpcRendererEvent,
      data: { locale: string; strings: Record<string, string> },
    ) => callback(data)
    ipcRenderer.on('locale-changed', handler)
    return () => ipcRenderer.removeListener('locale-changed', handler)
  },
  onShowCrashRecovery: (callback: () => void) => {
    const handler = () => callback()
    ipcRenderer.on('show-crash-recovery', handler)
    return () => ipcRenderer.removeListener('show-crash-recovery', handler)
  },
  onShowAccountSelect: (
    callback: (data: { url: string; serviceId: string; accountIds: string[] }) => void,
  ) => {
    const handler = (
      _event: Electron.IpcRendererEvent,
      data: { url: string; serviceId: string; accountIds: string[] },
    ) => callback(data)
    ipcRenderer.on('show-account-select', handler)
    return () => ipcRenderer.removeListener('show-account-select', handler)
  },

  // Keyboard shortcut events (main -> renderer)
  onShortcutAddAccount: (callback: () => void) => {
    const handler = () => callback()
    ipcRenderer.on('shortcut-add-account', handler)
    return () => ipcRenderer.removeListener('shortcut-add-account', handler)
  },
  onShortcutDeleteAccount: (callback: () => void) => {
    const handler = () => callback()
    ipcRenderer.on('shortcut-delete-account', handler)
    return () => ipcRenderer.removeListener('shortcut-delete-account', handler)
  },
  onShortcutSettings: (callback: () => void) => {
    const handler = () => callback()
    ipcRenderer.on('shortcut-settings', handler)
    return () => ipcRenderer.removeListener('shortcut-settings', handler)
  },
}

contextBridge.exposeInMainWorld('api', api)
