import type { ReactNode } from 'react'
import type { ThemeMode } from '../../theme/ThemeContext'
import { ToggleSwitch } from '../../primitives/ToggleSwitch'
import styles from './SettingsScreen.module.css'

interface SettingsScreenProps {
  locale: string
  telemetryEnabled: boolean
  themeMode: ThemeMode
  appVersion: string
  onLocaleChange: (locale: string) => void
  onTelemetryChange: (enabled: boolean) => void
  onThemeModeChange: (mode: ThemeMode) => void
  onClose: () => void
  onShowTutorial: () => void
  t: (key: string) => string
}

export function SettingsScreen({
  locale,
  telemetryEnabled,
  themeMode,
  appVersion,
  onLocaleChange,
  onTelemetryChange,
  onThemeModeChange,
  onClose,
  onShowTutorial,
  t,
}: SettingsScreenProps) {
  return (
    <div className={styles.screen}>
      <div className={styles.header}>
        <h1 className={styles.headerTitle}>{t('settings.title')}</h1>
        <button className={styles.closeButton} onClick={onClose}>
          {t('button.close')}
        </button>
      </div>

      <Section title={t('settings.general')}>
        <Row label={t('settings.language')}>
          <select
            value={locale}
            onChange={(e) => onLocaleChange(e.target.value)}
            className={styles.select}
          >
            <option value="en">English</option>
            <option value="ja">日本語</option>
          </select>
        </Row>
        <Row label={t('settings.show_tutorial')}>
          <button className={styles.showTutorialButton} onClick={onShowTutorial}>
            {t('settings.show_tutorial')}
          </button>
        </Row>
      </Section>

      <Section title={t('settings.appearance')}>
        <Row label={t('settings.theme')}>
          <div className={styles.themeToggle}>
            <span className={styles.themeLabel}>
              {themeMode === 'light' ? t('settings.theme.light') : t('settings.theme.dark')}
            </span>
            <ToggleSwitch
              checked={themeMode === 'light'}
              onChange={(checked) => onThemeModeChange(checked ? 'light' : 'dark')}
            />
          </div>
        </Row>
      </Section>

      <Section title={t('settings.privacy')}>
        <Row label={t('settings.telemetry')}>
          <ToggleSwitch checked={telemetryEnabled} onChange={onTelemetryChange} />
        </Row>
        <div className={styles.telemetryDescription}>{t('settings.telemetry.description')}</div>
      </Section>

      <Section title={t('settings.keyboard_shortcuts')}>
        <ShortcutRow label={t('settings.shortcut.next_account')} shortcut="Ctrl+Tab" />
        <ShortcutRow label={t('settings.shortcut.prev_account')} shortcut="Ctrl+Shift+Tab" />
        <ShortcutRow label={t('settings.shortcut.reload')} shortcut="Ctrl+R" />
        <ShortcutRow label={t('settings.shortcut.force_reload')} shortcut="Ctrl+Shift+R" />
        <ShortcutRow label={t('settings.shortcut.add_account')} shortcut="Ctrl+N" />
        <ShortcutRow label={t('settings.shortcut.settings')} shortcut="Ctrl+," />
      </Section>

      <Section title={t('settings.app_info')}>
        <Row label={t('settings.version')}>
          <span className={styles.versionText}>{appVersion}</span>
        </Row>
      </Section>
    </div>
  )
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className={styles.section}>
      <h2 className={styles.sectionTitle}>{title}</h2>
      <div className={styles.sectionContent}>{children}</div>
    </div>
  )
}

function Row({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className={styles.row}>
      <span className={styles.rowLabel}>{label}</span>
      {children}
    </div>
  )
}

function ShortcutRow({ label, shortcut }: { label: string; shortcut: string }) {
  return (
    <div className={styles.row}>
      <span className={styles.rowLabel}>{label}</span>
      <kbd className={styles.kbd}>{shortcut}</kbd>
    </div>
  )
}
