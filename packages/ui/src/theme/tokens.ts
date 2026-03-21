// ─── Color Tokens ───────────────────────────────────────────────────────────

export const colors = {
  light: {
    bgPrimary: '#ffffff',
    bgSecondary: '#f5f5f7',
    bgTertiary: '#e8e8ed',
    textPrimary: '#1d1d1f',
    textSecondary: '#86868b',
    textTertiary: '#aeaeb2',
    borderPrimary: '#d2d2d7',
    borderSecondary: '#e5e5ea',
    danger: '#ff3b30',
    accent: '#0085FF',
    success: '#34C759',
    overlay: 'rgba(0,0,0,0.3)',
    hover: 'rgba(0,0,0,0.04)',
  },
  dark: {
    bgPrimary: '#1a1a2e',
    bgSecondary: '#16162a',
    bgTertiary: '#2a2a4a',
    textPrimary: '#ffffff',
    textSecondary: '#aaaaaa',
    textTertiary: '#666666',
    borderPrimary: '#3a3a5a',
    borderSecondary: '#2a2a4a',
    danger: '#ff4444',
    accent: '#0085FF',
    success: '#1D9E75',
    overlay: 'rgba(0,0,0,0.5)',
    hover: 'rgba(255,255,255,0.08)',
  },
} as const

// ─── Dimension Tokens ───────────────────────────────────────────────────────

export const dimensions = {
  sidebarWidth: 80,
  sidebarTitleBarHeight: 48,
  headerHeight: 40,
  accountItemSize: 48,
  serviceIconSize: 24,
  serviceIconRadius: 6,
  avatarSize: 14,
  addButtonSize: 40,
  activeIndicatorWidth: 3,
  dialogWidth: 320,
  dialogWidthWide: 380,
  dialogBorderRadius: 12,
  dialogPadding: 24,
  borderRadius: {
    xs: 4,
    sm: 6,
    md: 8,
    lg: 10,
    xl: 12,
  },
  fontSize: {
    xs: 7,
    sm: 12,
    md: 13,
    lg: 14,
    xl: 16,
    xxl: 20,
  },
} as const

export const d = dimensions

// ─── Service Icons ──────────────────────────────────────────────────────────

export const serviceIconFiles: Record<string, string> = {
  x: 'X.svg',
  instagram: 'Instagram.svg',
  facebook: 'Facebook.png',
  youtube: 'Youtube.png',
  bluesky: 'Bluesky.png',
  twitch: 'Twitch.svg',
}

export const serviceIconBackgrounds: Record<string, string> = {
  x: '#000000',
  instagram: '#ffffff',
  facebook: '#ffffff',
  youtube: '#ffffff',
  bluesky: '#ffffff',
  twitch: '#ffffff',
}
