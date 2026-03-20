import { useCallback, useEffect, useState } from 'react'
import { ContentPlaceholder } from './components/content/ContentPlaceholder'
import { AccountSelectDialog } from './components/dialogs/AccountSelectDialog'
import { AddAccountDialog } from './components/dialogs/AddAccountDialog'
import { CrashRecoveryDialog } from './components/dialogs/CrashRecoveryDialog'
import { DeleteAccountDialog } from './components/dialogs/DeleteAccountDialog'
import { TelemetryConsentDialog } from './components/dialogs/TelemetryConsentDialog'
import { WebViewHeaderBar } from './components/header/WebViewHeaderBar'
import { TutorialScreen } from './components/onboarding/TutorialScreen'
import { SettingsScreen } from './components/settings/SettingsScreen'
import { Sidebar } from './components/sidebar/Sidebar'
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
  const [dialog, setDialog] = useState<DialogState>({ type: 'none' })

  const activeAccount = accounts.find((a) => a.isActive) ?? null
  const isDialogOpen = dialog.type !== 'none'

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

  const handleSwitch = useCallback((accountId: string) => {
    window.api.switchAccount(accountId)
  }, [])

  const handleAdd = useCallback((serviceId: string) => {
    window.api.addAccount(serviceId)
  }, [])

  const handleDelete = useCallback(
    (accountId: string) => {
      const account = accounts.find((a) => a.accountId === accountId)
      if (account) {
        setDialog({ type: 'delete-account', account })
      }
    },
    [accounts],
  )

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
    setDialog({ type: 'tutorial' })
  }, [])

  const handleTelemetryDeny = useCallback(() => {
    window.api.setSetting('telemetry_enabled', 'false')
    setDialog({ type: 'tutorial' })
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
        onDelete={handleDelete}
        onSettingsClick={() => setDialog({ type: 'settings' })}
        t={t}
      />

      {/* Header bar: only visible when dialog is open (otherwise the area is covered by SNS WebContentsView) */}
      {isDialogOpen && <WebViewHeaderBar account={activeAccount} currentUrl={currentUrl} />}

      {/* Dialogs render as full-window overlays */}
      {dialog.type === 'add-account' && (
        <AddAccountDialog onClose={closeDialog} onAdd={handleAdd} t={t} />
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
          onLocaleChange={setLocale}
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
