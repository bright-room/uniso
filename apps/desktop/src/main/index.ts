import path from 'node:path'
import { app, BrowserWindow, globalShortcut, WebContentsView } from 'electron'

// Remove Chromium automation indicators before any BrowserWindow is created.
// This disables navigator.webdriver and the cdc_ DOM properties at the engine level,
// which JS-level masking cannot reliably achieve.
app.commandLine.appendSwitch('disable-blink-features', 'AutomationControlled')

import {
  AccountManager,
  AccountRepository,
  AppDatabase,
  I18nManager,
  IdentityManager,
  LinkRouter,
  ServicePluginRegistry,
  ServicePluginRepository,
  SessionManager,
  SessionRepository,
  SettingsRepository,
} from '@uniso/shared'
import { checkForUpdatesInBackground, initAutoUpdater } from './auto-updater'
import { registerIpcHandlers } from './ipc-handlers'
import { WebViewManager } from './webview-manager'

let mainWindow: BrowserWindow
let database: AppDatabase
let sessionManager: SessionManager
let webViewManager: WebViewManager

function getDbPath(): string {
  const userDataPath = app.getPath('userData')
  return path.join(userDataPath, 'uniso.db')
}

async function createWindow(): Promise<void> {
  // Initialize database (async for sql.js WASM loading)
  database = new AppDatabase(getDbPath())
  await database.initialize()

  const db = database.getDb()

  // Initialize repositories
  const accountRepo = new AccountRepository(db)
  const sessionRepo = new SessionRepository(db)
  const settingsRepo = new SettingsRepository(db)
  const servicePluginRepo = new ServicePluginRepository(db)

  // Initialize domain
  const identityManager = new IdentityManager(settingsRepo)
  identityManager.getOrCreateLocalUserId()

  const registry = new ServicePluginRegistry(servicePluginRepo)
  registry.initialize()

  const i18nManager = new I18nManager(settingsRepo)
  i18nManager.initialize(app.getLocale())

  const accountManager = new AccountManager(accountRepo, sessionRepo)
  const linkRouter = new LinkRouter(registry, accountRepo)

  webViewManager = new WebViewManager(accountManager, sessionRepo, registry, linkRouter)
  webViewManager.setI18nManager(i18nManager)

  sessionManager = new SessionManager(sessionRepo, webViewManager)

  // Configure from settings
  const maxBg = parseInt(settingsRepo.getString('max_background_webviews') ?? '3', 10)
  const timeout = parseInt(settingsRepo.getString('webview_suspend_timeout_ms') ?? '300000', 10)
  webViewManager.configure(maxBg, timeout)

  // Check for crash recovery
  const wasClean = sessionManager.isCleanShutdown()
  sessionManager.markStartup()

  // Create window
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 600,
    minHeight: 400,
    title: 'Uniso',
    backgroundColor: '#16162a',
    titleBarStyle: 'hiddenInset',
    trafficLightPosition: { x: 12, y: 18 },
  })

  // Create sidebar view (transparent so SNS content shows through dialog overlay)
  const sidebarView = new WebContentsView({
    webPreferences: {
      preload: path.join(__dirname, '../preload/index.js'),
      contextIsolation: true,
      nodeIntegration: false,
      transparent: true,
    },
  })
  sidebarView.setBackgroundColor('#00000000')
  mainWindow.contentView.addChildView(sidebarView)

  webViewManager.setMainWindow(mainWindow)
  webViewManager.setSidebarView(sidebarView)

  // Load sidebar renderer
  if (process.env.ELECTRON_RENDERER_URL) {
    sidebarView.webContents.loadURL(process.env.ELECTRON_RENDERER_URL)
    // Open DevTools in a separate window so it doesn't get hidden behind views
    sidebarView.webContents.openDevTools({ mode: 'detach' })
  } else {
    sidebarView.webContents.loadFile(path.join(__dirname, '../renderer/index.html'))
  }

  // Register IPC handlers
  registerIpcHandlers(
    accountManager,
    sessionManager,
    registry,
    i18nManager,
    webViewManager,
    settingsRepo,
  )

  // Initialize auto-updater
  initAutoUpdater(sidebarView.webContents)

  // Load accounts and restore session
  accountManager.loadAccounts()

  // Notify renderer on account changes
  accountManager.onChange(() => {
    const accounts = accountManager.getAccounts()
    const activeId = accountManager.getActiveAccountId()
    sidebarView.webContents.send(
      'accounts-changed',
      accounts.map((a) => ({
        accountId: a.accountId,
        serviceId: a.serviceId,
        displayName: a.displayName,
        brandColor: a.brandColor,
        iconResource: a.iconResource,
        serviceDisplayName: a.serviceDisplayName,
        isActive: a.accountId === activeId,
      })),
    )
  })

  // Notify renderer on locale changes
  i18nManager.onLocaleChange(() => {
    sidebarView.webContents.send('locale-changed', {
      locale: i18nManager.getCurrentLocale(),
      strings: i18nManager.getAllStrings(),
    })
  })

  // Restore existing account WebViews
  const accounts = accountManager.getAccounts()
  const activeId = accountManager.getActiveAccountId()

  if (!wasClean && accounts.length > 0) {
    // Send crash recovery signal to renderer
    sidebarView.webContents.once('did-finish-load', () => {
      sidebarView.webContents.send('show-crash-recovery')
    })
  }

  // Create views for accounts
  for (const account of accounts) {
    webViewManager.getOrCreateWebView(account.accountId)
  }
  if (activeId) {
    webViewManager.switchTo(activeId)
  }

  webViewManager.layoutViews()

  // Start periodic save & eviction (also persist DB file with sql.js)
  const originalSave = webViewManager.saveCurrentState.bind(webViewManager)
  webViewManager.saveCurrentState = () => {
    originalSave()
    database.save()
  }
  sessionManager.startPeriodicSave()
  webViewManager.startEvictionTimer()

  // Register keyboard shortcuts
  mainWindow.on('focus', () => {
    globalShortcut.register('CommandOrControl+Tab', () => {
      const nextId = accountManager.getNextAccountId()
      if (nextId) webViewManager.switchTo(nextId)
    })
    globalShortcut.register('CommandOrControl+Shift+Tab', () => {
      const prevId = accountManager.getPrevAccountId()
      if (prevId) webViewManager.switchTo(prevId)
    })
  })

  mainWindow.on('blur', () => {
    globalShortcut.unregisterAll()
  })

  // Check for updates in background after startup
  const autoUpdateEnabled = settingsRepo.getString('auto_update_check') !== 'false'
  if (autoUpdateEnabled && !process.env.ELECTRON_RENDERER_URL) {
    setTimeout(() => checkForUpdatesInBackground(), 10_000)
  }
}

app.whenReady().then(createWindow)

// Register custom protocol for OAuth callbacks (future use)
app.setAsDefaultProtocolClient('uniso')

app.on('before-quit', () => {
  sessionManager.stopPeriodicSave()
  webViewManager.stopEvictionTimer()
  sessionManager.markCleanShutdown()
  webViewManager.destroyAll()
  database.close()
})

app.on('window-all-closed', () => {
  app.quit()
})
