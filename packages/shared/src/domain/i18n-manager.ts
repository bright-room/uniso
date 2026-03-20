import type { SettingsRepository } from '../data'
import en from '../i18n/en.json'
import ja from '../i18n/ja.json'
import type { Locale } from '../types'

type Listener = (locale: Locale) => void

const RESOURCES: Record<Locale, Record<string, string>> = { en, ja }

export class I18nManager {
  private currentLocale: Locale = 'en'
  private listeners: Set<Listener> = new Set()

  constructor(private settingsRepo: SettingsRepository) {}

  initialize(osLocale?: string): void {
    const saved = this.settingsRepo.getString('locale')
    if (saved && (saved === 'en' || saved === 'ja')) {
      this.currentLocale = saved
    } else if (osLocale) {
      this.currentLocale = osLocale.startsWith('ja') ? 'ja' : 'en'
    }
  }

  getCurrentLocale(): Locale {
    return this.currentLocale
  }

  setLocale(locale: Locale): void {
    this.currentLocale = locale
    this.settingsRepo.setString('locale', locale)
    for (const listener of this.listeners) {
      listener(locale)
    }
  }

  getString(key: string): string {
    return RESOURCES[this.currentLocale][key] ?? RESOURCES.en[key] ?? key
  }

  getAllStrings(): Record<string, string> {
    return { ...RESOURCES.en, ...RESOURCES[this.currentLocale] }
  }

  onLocaleChange(listener: Listener): () => void {
    this.listeners.add(listener)
    return () => this.listeners.delete(listener)
  }
}
