import { useState, useEffect } from 'react'

interface SettingsScreenProps {
  locale: string
  onLocaleChange: (locale: string) => void
  onClose: () => void
  onShowTutorial: () => void
  t: (key: string) => string
}

export function SettingsScreen({ locale, onLocaleChange, onClose, onShowTutorial, t }: SettingsScreenProps) {
  const [telemetryEnabled, setTelemetryEnabled] = useState(false)

  useEffect(() => {
    window.api.getSetting('telemetry_enabled').then((val) => {
      setTelemetryEnabled(val === 'true')
    })
  }, [])

  const handleTelemetryChange = (checked: boolean) => {
    setTelemetryEnabled(checked)
    window.api.setSetting('telemetry_enabled', String(checked))
  }

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 80,
        right: 0,
        bottom: 0,
        background: '#1a1a2e',
        zIndex: 500,
        overflow: 'auto',
        padding: '40px 32px',
      }}
    >
      {/* Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 32,
        }}
      >
        <h1 style={{ fontSize: 20, fontWeight: 600, color: '#ffffff' }}>{t('settings.title')}</h1>
        <button
          onClick={onClose}
          style={{
            padding: '6px 12px',
            borderRadius: 6,
            fontSize: 13,
            color: '#aaaaaa',
            background: '#2a2a4a',
          }}
        >
          {t('button.close')}
        </button>
      </div>

      {/* General section */}
      <Section title={t('settings.general')}>
        <SettingRow label={t('settings.language')}>
          <select
            value={locale}
            onChange={(e) => onLocaleChange(e.target.value)}
            style={{
              background: '#2a2a4a',
              color: '#ffffff',
              border: '0.5px solid #3a3a5a',
              borderRadius: 6,
              padding: '4px 8px',
              fontSize: 13,
            }}
          >
            <option value="en">English</option>
            <option value="ja">日本語</option>
          </select>
        </SettingRow>
        <SettingRow label={t('settings.show_tutorial')}>
          <button
            onClick={onShowTutorial}
            style={{
              padding: '4px 12px',
              borderRadius: 6,
              fontSize: 13,
              color: '#ffffff',
              background: '#3a3a5a',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            {t('settings.show_tutorial')}
          </button>
        </SettingRow>
      </Section>

      {/* Privacy section */}
      <Section title={t('settings.privacy')}>
        <SettingRow label={t('settings.telemetry')}>
          <ToggleSwitch checked={telemetryEnabled} onChange={handleTelemetryChange} />
        </SettingRow>
        <div style={{ padding: '6px 16px 10px', fontSize: 12, color: '#666666' }}>
          {t('settings.telemetry.description')}
        </div>
      </Section>

      {/* Keyboard Shortcuts section */}
      <Section title={t('settings.keyboard_shortcuts')}>
        <ShortcutRow label={t('settings.shortcut.next_account')} shortcut="Ctrl+Tab" />
        <ShortcutRow label={t('settings.shortcut.prev_account')} shortcut="Ctrl+Shift+Tab" />
        <ShortcutRow label={t('settings.shortcut.reload')} shortcut="Ctrl+R" />
        <ShortcutRow label={t('settings.shortcut.force_reload')} shortcut="Ctrl+Shift+R" />
        <ShortcutRow label={t('settings.shortcut.add_account')} shortcut="Ctrl+N" />
        <ShortcutRow label={t('settings.shortcut.settings')} shortcut="Ctrl+," />
      </Section>

      {/* App Info section */}
      <Section title={t('settings.app_info')}>
        <SettingRow label={t('settings.version')}>
          <span style={{ fontSize: 13, color: '#aaaaaa' }}>0.1.0</span>
        </SettingRow>
      </Section>
    </div>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 24 }}>
      <h2
        style={{
          fontSize: 13,
          fontWeight: 600,
          color: '#666666',
          textTransform: 'uppercase',
          letterSpacing: 0.5,
          marginBottom: 12,
        }}
      >
        {title}
      </h2>
      <div
        style={{
          background: '#16162a',
          borderRadius: 8,
          border: '0.5px solid #2a2a4a',
          overflow: 'hidden',
        }}
      >
        {children}
      </div>
    </div>
  )
}

function SettingRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 16px',
        borderBottom: '0.5px solid #2a2a4a',
      }}
    >
      <span style={{ fontSize: 13, color: '#ffffff' }}>{label}</span>
      {children}
    </div>
  )
}

function ShortcutRow({ label, shortcut }: { label: string; shortcut: string }) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 16px',
        borderBottom: '0.5px solid #2a2a4a',
      }}
    >
      <span style={{ fontSize: 13, color: '#ffffff' }}>{label}</span>
      <kbd
        style={{
          fontSize: 12,
          color: '#aaaaaa',
          background: '#2a2a4a',
          padding: '2px 8px',
          borderRadius: 4,
        }}
      >
        {shortcut}
      </kbd>
    </div>
  )
}

function ToggleSwitch({ checked, onChange }: { checked: boolean; onChange: (checked: boolean) => void }) {
  return (
    <button
      role="switch"
      aria-checked={checked}
      onClick={() => onChange(!checked)}
      style={{
        position: 'relative',
        width: 40,
        height: 22,
        borderRadius: 11,
        background: checked ? '#1D9E75' : '#3a3a5a',
        border: 'none',
        cursor: 'pointer',
        padding: 0,
        transition: 'background 0.2s',
      }}
    >
      <span
        style={{
          position: 'absolute',
          top: 2,
          left: checked ? 20 : 2,
          width: 18,
          height: 18,
          borderRadius: '50%',
          background: '#ffffff',
          transition: 'left 0.2s',
        }}
      />
    </button>
  )
}
