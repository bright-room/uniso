export type SettingsKey =
  | 'locale'
  | 'telemetry_enabled'
  | 'tutorial_completed'
  | 'webview_suspend_timeout_ms'
  | 'max_background_webviews'

export interface SettingsEntry {
  key: string
  value: string
}
