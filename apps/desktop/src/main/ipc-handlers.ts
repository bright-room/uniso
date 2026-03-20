import type {
  AccountManager,
  I18nManager,
  ServicePluginRegistry,
  SessionManager,
  SettingsRepository,
} from '@uniso/shared'
import { BrowserWindow, ipcMain, Menu } from 'electron'
import type { WebViewManager } from './webview-manager'

export function registerIpcHandlers(
  accountManager: AccountManager,
  sessionManager: SessionManager,
  registry: ServicePluginRegistry,
  i18nManager: I18nManager,
  webViewManager: WebViewManager,
  settingsRepo: SettingsRepository,
): void {
  ipcMain.handle('add-account', (_event, serviceId: string) => {
    const account = accountManager.addAccount(serviceId)
    webViewManager.getOrCreateWebView(account.accountId)
    webViewManager.layoutViews()
    return {
      accountId: account.accountId,
      serviceId: account.serviceId,
      displayName: account.displayName,
      brandColor: account.brandColor,
      iconResource: account.iconResource,
      serviceDisplayName: account.serviceDisplayName,
    }
  })

  ipcMain.handle('switch-account', (_event, accountId: string) => {
    webViewManager.switchTo(accountId)
  })

  ipcMain.handle('remove-account', (_event, accountId: string) => {
    webViewManager.removeAccountViews(accountId)
    accountManager.removeAccount(accountId)
    webViewManager.layoutViews()
  })

  ipcMain.handle('list-accounts', () => {
    const accounts = accountManager.getAccounts()
    const activeId = accountManager.getActiveAccountId()
    return accounts.map((a) => ({
      accountId: a.accountId,
      serviceId: a.serviceId,
      displayName: a.displayName,
      brandColor: a.brandColor,
      iconResource: a.iconResource,
      serviceDisplayName: a.serviceDisplayName,
      isActive: a.accountId === activeId,
    }))
  })

  ipcMain.handle('get-active-account', () => {
    return accountManager.getActiveAccountId()
  })

  ipcMain.handle('get-account-url', () => {
    const activeId = accountManager.getActiveAccountId()
    if (!activeId) return null
    return webViewManager.getCurrentUrl(activeId) ?? null
  })

  ipcMain.handle('get-service-plugins', () => {
    return registry.getAll()
  })

  ipcMain.handle('get-settings', (_event, key: string) => {
    return settingsRepo.getString(key) ?? null
  })

  ipcMain.handle('set-setting', (_event, key: string, value: string) => {
    settingsRepo.setString(key, value)
    if (key === 'locale') {
      i18nManager.setLocale(value as 'en' | 'ja')
    }
  })

  ipcMain.handle('get-i18n-strings', () => {
    return i18nManager.getAllStrings()
  })

  ipcMain.handle('get-locale', () => {
    return i18nManager.getCurrentLocale()
  })

  ipcMain.handle('set-locale', (_event, locale: string) => {
    i18nManager.setLocale(locale as 'en' | 'ja')
  })

  ipcMain.handle('restore-session', () => {
    const result = sessionManager.restoreSession()
    if (result.activeAccountId) {
      webViewManager.switchTo(result.activeAccountId)
    }
    return result
  })

  ipcMain.handle('start-fresh', () => {
    sessionManager.startFresh()
    webViewManager.destroyAll()
  })

  ipcMain.handle('reload-active', () => {
    webViewManager.reloadActiveView()
  })

  ipcMain.handle('force-reload-active', () => {
    webViewManager.forceReloadActiveView()
  })

  ipcMain.handle('hide-webviews', () => {
    webViewManager.hideAllViews()
  })

  ipcMain.handle('show-webviews', () => {
    webViewManager.showActiveView()
  })

  ipcMain.handle(
    'show-context-menu',
    (_event, accountId: string, serviceDisplayName: string, displayName: string | null) => {
      const label = `${serviceDisplayName} — ${displayName ?? ''}`
      const win = BrowserWindow.fromWebContents(_event.sender)
      if (!win) return

      const menu = Menu.buildFromTemplate([
        { label, enabled: false },
        { type: 'separator' },
        {
          label: i18nManager.getString('dialog.delete.title'),
          click: () => {
            _event.sender.send('context-menu-delete', accountId)
          },
        },
      ])
      menu.popup({ window: win })
    },
  )
}
