import {
  type AccountListItem,
  AccountSelectDialog,
  AddAccountDialog,
  ContentPlaceholder,
  CrashRecoveryDialog,
  DeleteAccountDialog,
  type ServicePlugin,
  SettingsScreen,
  Sidebar,
  TelemetryConsentDialog,
  type ThemeMode,
  TutorialScreen,
  useTheme,
  WebViewHeaderBar,
} from '@uniso/ui'
import { useCallback, useEffect, useState } from 'react'
import { useAccounts } from './hooks/useAccounts'
import { useCurrentUrl } from './hooks/useCurrentUrl'
import { useI18n } from './hooks/useI18n'

type DialogState =
  | { type: 'none' }
  | { type: 'add-account' }
  | { type: 'delete-account'; account: AccountListItem }
  | { type: 'crash-recovery' }
  | { type: 'account-select'; url: string; serviceId: string; accountIds: string[] }
  | { type: 'settings' }
  | { type: 'tutorial' }
  | { type: 'telemetry' }

export function App() {
  const accounts = useAccounts()
  const { t, locale, setLocale } = useI18n()
  const currentUrl = useCurrentUrl()
  const { setMode } = useTheme()
  const [dialog, setDialog] = useState<DialogState>({ type: 'none' })
  const [services, setServices] = useState<ServicePlugin[]>([])
  const [telemetryEnabled, setTelemetryEnabled] = useState(false)
  const [themeMode, setThemeMode] = useState<ThemeMode>('dark')
  const [appVersion, setAppVersion] = useState('')

  const activeAccount = accounts.find((a) => a.isActive) ?? null
  const isDialogOpen = dialog.type !== 'none'

  // Fetch service plugins and app version once
  useEffect(() => {
    window.api.getServicePlugins().then(setServices)
    window.api.getAppVersion().then(setAppVersion)
  }, [])

  // Load theme mode from DB on startup
  useEffect(() => {
    window.api.getSetting('theme_mode').then((val) => {
      const mode: ThemeMode = val === 'light' ? 'light' : 'dark'
      setThemeMode(mode)
      setMode(mode)
    })
  }, [setMode])

  // First-run check: show telemetry consent if tutorial not completed
  useEffect(() => {
    window.api
      .getSetting('tutorial_completed')
      .then((val) => {
        console.log('[first-run] tutorial_completed =', JSON.stringify(val))
        if (val !== 'true') {
          setDialog({ type: 'telemetry' })
        }
      })
      .catch((err) => {
        console.error('[first-run] getSetting failed:', err)
      })
  }, [])

  // Fetch telemetry setting
  useEffect(() => {
    window.api.getSetting('telemetry_enabled').then((val) => {
      setTelemetryEnabled(val === 'true')
    })
  }, [])

  // Listen for context menu delete action from native menu
  useEffect(() => {
    return window.api.onContextMenuDelete((accountId) => {
      const account = accounts.find((a) => a.accountId === accountId)
      if (account) {
        setDialog({ type: 'delete-account', account })
      }
    })
  }, [accounts])

  // Listen for crash recovery signal from main
  useEffect(() => {
    return window.api.onShowCrashRecovery(() => {
      setDialog({ type: 'crash-recovery' })
    })
  }, [])

  // Listen for account select requests from main
  useEffect(() => {
    return window.api.onShowAccountSelect((data) => {
      setDialog({
        type: 'account-select',
        url: data.url,
        serviceId: data.serviceId,
        accountIds: data.accountIds,
      })
    })
  }, [])

  // Keyboard shortcut handlers from main process
  useEffect(() => {
    return window.api.onShortcutAddAccount(() => {
      setDialog({ type: 'add-account' })
    })
  }, [])

  useEffect(() => {
    return window.api.onShortcutDeleteAccount(() => {
      const active = accounts.find((a) => a.isActive)
      if (active) {
        setDialog({ type: 'delete-account', account: active })
      }
    })
  }, [accounts])

  useEffect(() => {
    return window.api.onShortcutSettings(() => {
      setDialog({ type: 'settings' })
    })
  }, [])

  const handleSwitch = useCallback((accountId: string) => {
    window.api.switchAccount(accountId)
  }, [])

  const handleAdd = useCallback((serviceId: string) => {
    window.api.addAccount(serviceId)
  }, [])

  const handleContextMenu = useCallback((account: AccountListItem) => {
    window.api.showContextMenu(account.accountId, account.serviceDisplayName, account.displayName)
  }, [])

  const confirmDelete = useCallback(() => {
    if (dialog.type === 'delete-account') {
      window.api.removeAccount(dialog.account.accountId)
    }
    setDialog({ type: 'none' })
  }, [dialog])

  const closeDialog = useCallback(() => {
    setDialog({ type: 'none' })
  }, [])

  const handleTelemetryAllow = useCallback(() => {
    window.api.setSetting('telemetry_enabled', 'true')
    setTelemetryEnabled(true)
    setDialog({ type: 'tutorial' })
  }, [])

  const handleTelemetryDeny = useCallback(() => {
    window.api.setSetting('telemetry_enabled', 'false')
    setTelemetryEnabled(false)
    setDialog({ type: 'tutorial' })
  }, [])

  const handleThemeModeChange = useCallback(
    (mode: ThemeMode) => {
      setThemeMode(mode)
      setMode(mode)
      window.api.setSetting('theme_mode', mode)
    },
    [setMode],
  )

  const handleTelemetryChange = useCallback((enabled: boolean) => {
    setTelemetryEnabled(enabled)
    window.api.setSetting('telemetry_enabled', String(enabled))
  }, [])

  const handleTutorialComplete = useCallback(() => {
    window.api.setSetting('tutorial_completed', 'true')
    setDialog({ type: 'none' })
  }, [])

  const handleShowTutorial = useCallback(() => {
    setDialog({ type: 'tutorial' })
  }, [])

  // Hide/show webviews when dialog state changes or no accounts exist
  const needsFullWidth = isDialogOpen || accounts.length === 0
  useEffect(() => {
    if (needsFullWidth) {
      window.api.hideWebViews()
    } else {
      window.api.showWebViews()
    }
  }, [needsFullWidth])

  return (
    <div style={{ width: '100%', height: '100%', display: 'flex', position: 'relative' }}>
      {/* Sidebar is always rendered at left */}
      <Sidebar
        accounts={accounts}
        onSwitch={handleSwitch}
        onAddClick={() => setDialog({ type: 'add-account' })}
        onContextMenu={handleContextMenu}
        onSettingsClick={() => setDialog({ type: 'settings' })}
        t={t}
      />

      {/* Header bar: only visible when dialog is open (otherwise the area is covered by SNS WebContentsView) */}
      {isDialogOpen && <WebViewHeaderBar account={activeAccount} currentUrl={currentUrl} />}

      {/* Dialogs render as full-window overlays */}
      {dialog.type === 'add-account' && (
        <AddAccountDialog services={services} onClose={closeDialog} onAdd={handleAdd} t={t} />
      )}

      {dialog.type === 'delete-account' && (
        <DeleteAccountDialog
          account={dialog.account}
          onClose={closeDialog}
          onConfirm={confirmDelete}
          t={t}
        />
      )}

      {dialog.type === 'crash-recovery' && (
        <CrashRecoveryDialog
          onRestore={() => {
            window.api.restoreSession()
            closeDialog()
          }}
          onStartFresh={() => {
            window.api.startFresh()
            closeDialog()
          }}
          t={t}
        />
      )}

      {dialog.type === 'account-select' && (
        <AccountSelectDialog
          accounts={accounts.filter((a) => dialog.accountIds.includes(a.accountId))}
          url={dialog.url}
          onSelect={(accountId) => {
            window.api.switchAccount(accountId)
            closeDialog()
          }}
          onClose={closeDialog}
          t={t}
        />
      )}

      {dialog.type === 'settings' && (
        <SettingsScreen
          locale={locale}
          telemetryEnabled={telemetryEnabled}
          themeMode={themeMode}
          appVersion={appVersion}
          onLocaleChange={setLocale}
          onTelemetryChange={handleTelemetryChange}
          onThemeModeChange={handleThemeModeChange}
          onClose={closeDialog}
          onShowTutorial={handleShowTutorial}
          t={t}
        />
      )}

      {dialog.type === 'telemetry' && (
        <TelemetryConsentDialog onAllow={handleTelemetryAllow} onDeny={handleTelemetryDeny} t={t} />
      )}

      {dialog.type === 'tutorial' && <TutorialScreen onComplete={handleTutorialComplete} t={t} />}

      {accounts.length === 0 && !isDialogOpen && <ContentPlaceholder account={null} t={t} />}
    </div>
  )
}
