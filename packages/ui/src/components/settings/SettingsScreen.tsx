import type React from 'react'
import { useTheme } from '../../theme/ThemeContext'
import { d } from '../../theme/tokens'

interface SettingsScreenProps {
  locale: string
  telemetryEnabled: boolean
  onLocaleChange: (locale: string) => void
  onTelemetryChange: (enabled: boolean) => void
  onClose: () => void
  onShowTutorial: () => void
  t: (key: string) => string
}

export function SettingsScreen({
  locale,
  telemetryEnabled,
  onLocaleChange,
  onTelemetryChange,
  onClose,
  onShowTutorial,
  t,
}: SettingsScreenProps) {
  const { c } = useTheme()

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: d.sidebarWidth,
        right: 0,
        bottom: 0,
        background: c.bgPrimary,
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
        <h1 style={{ fontSize: d.fontSize.xxl, fontWeight: 600, color: c.textPrimary }}>
          {t('settings.title')}
        </h1>
        <button
          onClick={onClose}
          style={{
            padding: '6px 12px',
            borderRadius: d.borderRadius.sm,
            fontSize: d.fontSize.md,
            color: c.textSecondary,
            background: c.bgTertiary,
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
              background: c.bgTertiary,
              color: c.textPrimary,
              border: `0.5px solid ${c.borderPrimary}`,
              borderRadius: d.borderRadius.sm,
              padding: '4px 8px',
              fontSize: d.fontSize.md,
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
              borderRadius: d.borderRadius.sm,
              fontSize: d.fontSize.md,
              color: c.textPrimary,
              background: c.borderPrimary,
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
          <ToggleSwitch checked={telemetryEnabled} onChange={onTelemetryChange} />
        </SettingRow>
        <div style={{ padding: '6px 16px 10px', fontSize: d.fontSize.sm, color: c.textTertiary }}>
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
          <span style={{ fontSize: d.fontSize.md, color: c.textSecondary }}>0.1.0</span>
        </SettingRow>
      </Section>
    </div>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  const { c } = useTheme()

  return (
    <div style={{ marginBottom: 24 }}>
      <h2
        style={{
          fontSize: d.fontSize.md,
          fontWeight: 600,
          color: c.textTertiary,
          textTransform: 'uppercase',
          letterSpacing: 0.5,
          marginBottom: 12,
        }}
      >
        {title}
      </h2>
      <div
        style={{
          background: c.bgSecondary,
          borderRadius: d.borderRadius.md,
          border: `0.5px solid ${c.borderSecondary}`,
          overflow: 'hidden',
        }}
      >
        {children}
      </div>
    </div>
  )
}

function SettingRow({ label, children }: { label: string; children: React.ReactNode }) {
  const { c } = useTheme()

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 16px',
        borderBottom: `0.5px solid ${c.borderSecondary}`,
      }}
    >
      <span style={{ fontSize: d.fontSize.md, color: c.textPrimary }}>{label}</span>
      {children}
    </div>
  )
}

function ShortcutRow({ label, shortcut }: { label: string; shortcut: string }) {
  const { c } = useTheme()

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 16px',
        borderBottom: `0.5px solid ${c.borderSecondary}`,
      }}
    >
      <span style={{ fontSize: d.fontSize.md, color: c.textPrimary }}>{label}</span>
      <kbd
        style={{
          fontSize: d.fontSize.sm,
          color: c.textSecondary,
          background: c.bgTertiary,
          padding: '2px 8px',
          borderRadius: d.borderRadius.xs,
        }}
      >
        {shortcut}
      </kbd>
    </div>
  )
}

function ToggleSwitch({
  checked,
  onChange,
}: {
  checked: boolean
  onChange: (checked: boolean) => void
}) {
  const { c } = useTheme()

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
        background: checked ? c.success : c.borderPrimary,
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
          background: c.textPrimary,
          transition: 'left 0.2s',
        }}
      />
    </button>
  )
}
