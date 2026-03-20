export const colors = {
  bgPrimary: '#ffffff',
  bgSecondary: '#f5f5f7',
  bgTertiary: '#e8e8ed',
  textPrimary: '#1d1d1f',
  textSecondary: '#86868b',
  textTertiary: '#aeaeb2',
  borderSecondary: '#d2d2d7',
  borderTertiary: '#e5e5ea',
  danger: '#ff3b30',

  // Dark theme
  dark: {
    bgPrimary: '#1a1a2e',
    bgSecondary: '#16162a',
    bgTertiary: '#2a2a4a',
    textPrimary: '#ffffff',
    textSecondary: '#aaaaaa',
    textTertiary: '#666666',
    borderSecondary: '#3a3a5a',
    borderTertiary: '#2a2a4a',
    danger: '#ff4444',
  },
} as const

export const dimensions = {
  sidebarWidth: 72,
  headerHeight: 40,
  accountItemSize: 48,
  serviceIconSize: 24,
  serviceIconRadius: 6,
  avatarSize: 14,
  addButtonSize: 40,
  activeIndicatorWidth: 3,
} as const

export const serviceIcons: Record<string, string> = {
  x: '𝕏',
  instagram: '📷',
  facebook: 'f',
  youtube: '▶',
  bluesky: '🦋',
  twitch: '📺',
}
