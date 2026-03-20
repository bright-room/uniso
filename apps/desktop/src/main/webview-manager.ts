import { BrowserWindow, WebContentsView, shell } from 'electron'
import {
  AccountManager,
  SessionManager,
  ServicePluginRegistry,
  LinkRouter,
  SessionRepository,
  I18nManager,
  type AccountState,
  type WebViewStateSaver,
} from '@uniso/shared'
import { getOrCreateSession, getMaskElectronJs } from './session-setup'

const SIDEBAR_WIDTH = 72
const HEADER_HEIGHT = 40

interface ManagedView {
  accountId: string
  serviceId: string
  view: WebContentsView
}

export class WebViewManager implements WebViewStateSaver {
  private views: Map<string, ManagedView> = new Map()
  private mainWindow: BrowserWindow | null = null
  private sidebarView: WebContentsView | null = null
  private maxBackgroundWebViews = 3
  private suspendTimeoutMs = 300000 // 5 minutes
  private evictionTimer: ReturnType<typeof setInterval> | null = null

  private i18nManager: I18nManager | null = null

  constructor(
    private accountManager: AccountManager,
    private sessionRepo: SessionRepository,
    private registry: ServicePluginRegistry,
    private linkRouter: LinkRouter
  ) {}

  setI18nManager(manager: I18nManager): void {
    this.i18nManager = manager
  }

  setMainWindow(win: BrowserWindow): void {
    this.mainWindow = win
    win.on('resize', () => this.layoutViews())
  }

  setSidebarView(view: WebContentsView): void {
    this.sidebarView = view
  }

  configure(maxBackground: number, suspendTimeoutMs: number): void {
    this.maxBackgroundWebViews = maxBackground
    this.suspendTimeoutMs = suspendTimeoutMs
  }

  startEvictionTimer(): void {
    this.stopEvictionTimer()
    this.evictionTimer = setInterval(() => this.evictExpired(), 60000)
  }

  stopEvictionTimer(): void {
    if (this.evictionTimer) {
      clearInterval(this.evictionTimer)
      this.evictionTimer = null
    }
  }

  getOrCreateWebView(accountId: string): WebContentsView {
    const existing = this.views.get(accountId)
    if (existing) return existing.view

    const account = this.accountManager
      .getAccounts()
      .find((a) => a.accountId === accountId)
    if (!account) throw new Error(`Account ${accountId} not found`)

    const ses = getOrCreateSession(accountId)
    const view = new WebContentsView({
      webPreferences: {
        session: ses,
        contextIsolation: true,
        nodeIntegration: false,
      },
    })

    // Inject fingerprint masking as early as possible (before page scripts run)
    view.webContents.on('did-start-navigation', () => {
      view.webContents
        .executeJavaScript(getMaskElectronJs())
        .catch(() => {})
    })
    // Re-inject on dom-ready as a safety net (some SPAs reset properties)
    view.webContents.on('dom-ready', () => {
      view.webContents.executeJavaScript(getMaskElectronJs()).catch(() => {})
    })

    // Inject login guidance for X (email/password login is blocked by Arkose Labs)
    // X is a SPA, so we must detect login pages via both full navigation and in-page navigation
    if (account.serviceId === 'x') {
      const injectXLoginHint = (url: string): void => {
        if (!url.includes('/login') && !url.includes('/i/flow/login')) return
        const hintText = this.i18nManager?.getString('error.x_login_hint') ??
          'X does not support email/password login in this app. Please use "Sign in with Google" or "Sign in with Apple" instead.'
        const safeText = JSON.stringify(hintText)
        view.webContents
          .executeJavaScript(
            `(function() {
              if (document.getElementById('uniso-login-hint')) return;
              var banner = document.createElement('div');
              banner.id = 'uniso-login-hint';
              banner.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:99999;background:#1d4ed8;color:#fff;padding:10px 16px;font-size:13px;font-family:-apple-system,system-ui,sans-serif;text-align:center;';
              banner.textContent = ${safeText};
              var close = document.createElement('button');
              close.textContent = '\\u2715';
              close.style.cssText = 'background:none;border:none;color:#fff;font-size:16px;cursor:pointer;margin-left:12px;';
              close.onclick = function() { banner.remove(); };
              banner.appendChild(close);
              document.body.prepend(banner);
            })()`
          )
          .catch(() => {})
      }
      view.webContents.on('did-navigate', (_ev, url) => injectXLoginHint(url))
      view.webContents.on('did-navigate-in-page', (_ev, url) => injectXLoginHint(url))
    }

    // Track URL changes
    view.webContents.on('did-navigate', (_event, url) => {
      this.sessionRepo.updateLastUrl(accountId, url, 0)
      this.notifySidebar('url-changed', {
        accountId,
        url,
      })
    })

    view.webContents.on('did-navigate-in-page', (_event, url) => {
      this.sessionRepo.updateLastUrl(accountId, url, 0)
      this.notifySidebar('url-changed', {
        accountId,
        url,
      })
    })

    // Handle popups
    view.webContents.setWindowOpenHandler(({ url: popupUrl }) => {
      if (
        popupUrl.includes('accounts.google.com') ||
        popupUrl.includes('accounts.youtube.com') ||
        popupUrl.includes('appleid.apple.com')
      ) {
        return {
          action: 'allow',
          overrideBrowserWindowOptions: {
            width: 500,
            height: 700,
            webPreferences: {
              session: ses,
              contextIsolation: true,
              nodeIntegration: false,
            },
          },
        }
      }

      // Link routing for new windows
      const classification = this.linkRouter.classifyLink(popupUrl, account.serviceId)
      switch (classification.type) {
        case 'external':
        case 'internal-no-account':
          shell.openExternal(popupUrl)
          return { action: 'deny' as const }
        case 'same-domain':
          view.webContents.loadURL(popupUrl)
          return { action: 'deny' as const }
        case 'internal-single-account':
          this.accountManager.setActiveAccount(classification.accountId)
          this.switchTo(classification.accountId, popupUrl)
          return { action: 'deny' as const }
        case 'internal-multi-account':
          this.notifySidebar('show-account-select', {
            url: popupUrl,
            serviceId: classification.serviceId,
            accountIds: classification.accountIds,
          })
          return { action: 'deny' as const }
      }
    })

    // Handle navigation
    view.webContents.on('will-navigate', (event, url) => {
      // Intercept Google OAuth navigations and open in a popup window
      // Google blocks embedded WebViews but allows Electron popup windows with the same session
      if (
        url.includes('accounts.google.com/o/oauth2') ||
        url.includes('accounts.google.com/signin') ||
        url.includes('accounts.google.com/ServiceLogin')
      ) {
        event.preventDefault()
        this.openOAuthPopup(accountId, url, ses)
        return
      }

      const classification = this.linkRouter.classifyLink(url, account.serviceId)
      if (classification.type === 'external' || classification.type === 'internal-no-account') {
        event.preventDefault()
        shell.openExternal(url)
      } else if (classification.type === 'internal-single-account') {
        event.preventDefault()
        this.accountManager.setActiveAccount(classification.accountId)
        this.switchTo(classification.accountId, url)
      } else if (classification.type === 'internal-multi-account') {
        event.preventDefault()
        this.notifySidebar('show-account-select', {
          url,
          serviceId: classification.serviceId,
          accountIds: classification.accountIds,
        })
      }
    })

    // Handle load failures
    view.webContents.on('did-fail-load', (_event, errorCode, errorDescription, validatedURL) => {
      // Ignore cancelled loads (-3) and aborted requests (-1)
      if (errorCode === -3 || errorCode === -1) return

      const plugin = this.registry.getById(account.serviceId)
      const brandColor = plugin?.brandColor ?? '#3a3a5a'
      const displayName = plugin?.displayName ?? account.serviceId
      const iconLetter = displayName.charAt(0).toUpperCase()

      // Sanitize values for safe HTML embedding
      const safeUrl = validatedURL
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
      const safeErrorDesc = (errorDescription || 'Connection failed')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
      const safeBrandColor = brandColor.replace(/[^#a-fA-F0-9]/g, '')

      const errorHtml = `<!DOCTYPE html>
<html><head><meta charset="utf-8"><style>
  body { margin:0; background:#1a1a2e; color:#fff; font-family:-apple-system,system-ui,sans-serif;
         display:flex; align-items:center; justify-content:center; height:100vh; }
  .container { text-align:center; max-width:400px; padding:32px; }
  .icon { width:64px; height:64px; border-radius:16px; background:${safeBrandColor};
          display:inline-flex; align-items:center; justify-content:center;
          font-size:28px; font-weight:600; color:#fff; margin-bottom:16px; }
  h2 { font-size:16px; font-weight:500; margin:0 0 8px; }
  p { font-size:13px; color:#aaa; margin:0 0 24px; }
  .url { font-size:12px; color:#666; background:#16162a; border-radius:8px;
         padding:12px 16px; margin-bottom:24px; word-break:break-all; }
  .actions { display:flex; gap:8px; justify-content:center; }
  button, .open-link { padding:8px 20px; border-radius:6px; font-size:13px;
                       cursor:pointer; text-decoration:none; }
  .retry { background:#3a3a5a; color:#fff; border:none; }
  .retry:hover { background:#4a4a6a; }
  .open-link { background:transparent; color:#aaa; border:0.5px solid #3a3a5a; display:inline-block; }
  .open-link:hover { background:#2a2a4a; }
</style></head><body>
<div class="container">
  <div class="icon">${iconLetter}</div>
  <h2>${safeErrorDesc}</h2>
  <p>Could not load the page (error ${errorCode})</p>
  <div class="url">${safeUrl}</div>
  <div class="actions">
    <button class="retry" id="retryBtn">Retry</button>
    <a class="open-link" id="openBtn" href="#">Open in browser</a>
  </div>
</div>
<script>
  var failedUrl = ${JSON.stringify(validatedURL)};
  document.getElementById('retryBtn').addEventListener('click', function() {
    window.location.href = failedUrl;
  });
  document.getElementById('openBtn').addEventListener('click', function(e) {
    e.preventDefault();
    window.open(failedUrl, '_blank');
  });
</script>
</body></html>`

      view.webContents.loadURL(
        `data:text/html;charset=utf-8,${encodeURIComponent(errorHtml)}`
      )
    })

    this.mainWindow!.contentView.addChildView(view)

    // Load URL - either restored or default
    const state = this.sessionRepo.getAccountState(accountId)
    const defaultUrl = this.registry.getDefaultUrl(account.serviceId)
    const urlToLoad = state?.lastUrl || defaultUrl || `https://${account.serviceId}.com`
    view.webContents.loadURL(urlToLoad)

    this.views.set(accountId, { accountId, serviceId: account.serviceId, view })

    // Update state
    const now = new Date().toISOString()
    this.sessionRepo.upsertAccountState({
      accountId,
      lastUrl: urlToLoad,
      scrollPositionY: 0,
      webviewStatus: 'active',
      lastAccessedAt: now,
    })

    return view
  }

  switchTo(accountId: string, url?: string): void {
    const now = new Date().toISOString()
    const currentActiveId = this.accountManager.getActiveAccountId()

    // Move current active to background
    if (currentActiveId && currentActiveId !== accountId) {
      this.sessionRepo.updateWebViewStatus(currentActiveId, 'background', now)
    }

    // Get or create the target view
    const view = this.getOrCreateWebView(accountId)

    // If a specific URL is provided, navigate to it
    if (url) {
      view.webContents.loadURL(url)
    }

    this.sessionRepo.updateWebViewStatus(accountId, 'active', now)
    this.accountManager.setActiveAccount(accountId)
    this.layoutViews()

    // Evict if too many background views
    this.evictOverflow()
  }

  destroyWebView(accountId: string): void {
    const managed = this.views.get(accountId)
    if (!managed) return

    this.mainWindow?.contentView.removeChildView(managed.view)
    managed.view.webContents.close()
    this.views.delete(accountId)

    this.sessionRepo.updateWebViewStatus(accountId, 'destroyed', new Date().toISOString())
  }

  destroyAll(): void {
    for (const [accountId] of this.views) {
      this.destroyWebView(accountId)
    }
  }

  removeAccountViews(accountId: string): void {
    const managed = this.views.get(accountId)
    if (managed) {
      this.mainWindow?.contentView.removeChildView(managed.view)
      const ses = managed.view.webContents.session
      ses.clearStorageData()
      managed.view.webContents.close()
      this.views.delete(accountId)
    }
  }

  layoutViews(): void {
    if (!this.mainWindow || !this.sidebarView) return
    const [width, height] = this.mainWindow.getContentSize()

    this.sidebarView.setBounds({ x: 0, y: 0, width: SIDEBAR_WIDTH, height })

    const activeId = this.accountManager.getActiveAccountId()
    for (const [id, managed] of this.views) {
      if (id === activeId) {
        managed.view.setBounds({
          x: SIDEBAR_WIDTH,
          y: HEADER_HEIGHT,
          width: width - SIDEBAR_WIDTH,
          height: height - HEADER_HEIGHT,
        })
      } else {
        managed.view.setBounds({ x: -10000, y: -10000, width: 0, height: 0 })
      }
    }
  }

  getCurrentUrl(accountId: string): string | undefined {
    const managed = this.views.get(accountId)
    if (!managed) return undefined
    return managed.view.webContents.getURL()
  }

  reloadActiveView(): void {
    const activeId = this.accountManager.getActiveAccountId()
    if (!activeId) return
    const managed = this.views.get(activeId)
    managed?.view.webContents.reload()
  }

  forceReloadActiveView(): void {
    const activeId = this.accountManager.getActiveAccountId()
    if (!activeId) return
    const managed = this.views.get(activeId)
    managed?.view.webContents.reloadIgnoringCache()
  }

  // WebViewStateSaver implementation
  saveCurrentState(): void {
    const now = new Date().toISOString()
    for (const [accountId, managed] of this.views) {
      const url = managed.view.webContents.getURL()
      this.sessionRepo.updateLastUrl(accountId, url, 0)
    }
    this.sessionRepo.updateLastSavedAt(now)
  }

  hideAllViews(): void {
    if (!this.mainWindow || !this.sidebarView) return
    const [width, height] = this.mainWindow.getContentSize()

    // Raise sidebar view above SNS WebViews by re-adding it (last child = topmost)
    this.mainWindow.contentView.removeChildView(this.sidebarView)
    this.mainWindow.contentView.addChildView(this.sidebarView)

    // Expand sidebar view to full window so dialogs render without clipping
    // SNS WebViews stay in place — visible behind the semi-transparent dialog overlay
    this.sidebarView.setBounds({ x: 0, y: 0, width, height })
  }

  showActiveView(): void {
    this.layoutViews()
  }

  private openOAuthPopup(accountId: string, url: string, ses: Electron.Session): void {
    const popup = new BrowserWindow({
      width: 500,
      height: 700,
      parent: this.mainWindow!,
      modal: false,
      webPreferences: {
        session: ses,
        contextIsolation: true,
        nodeIntegration: false,
      },
    })
    popup.loadURL(url)

    // When the OAuth flow completes, the popup will redirect back to the service.
    // Detect when the popup navigates away from Google and close it.
    popup.webContents.on('will-navigate', (_event, navUrl) => {
      if (
        !navUrl.includes('accounts.google.com') &&
        !navUrl.includes('myaccount.google.com')
      ) {
        popup.close()
        const managed = this.views.get(accountId)
        if (managed) {
          managed.view.webContents.reload()
        }
      }
    })
  }

  private evictOverflow(): void {
    const bgCount = this.sessionRepo.getBackgroundWebViewCount()
    if (bgCount <= this.maxBackgroundWebViews) return

    const oldest = this.sessionRepo.getOldestBackground()
    if (oldest) {
      this.destroyWebView(oldest.accountId)
    }
  }

  private evictExpired(): void {
    const cutoff = new Date(Date.now() - this.suspendTimeoutMs).toISOString()
    const expired = this.sessionRepo.getExpiredBackground(cutoff)
    for (const state of expired) {
      this.destroyWebView(state.accountId)
    }
  }

  private notifySidebar(channel: string, data: unknown): void {
    this.sidebarView?.webContents.send(channel, data)
  }
}
