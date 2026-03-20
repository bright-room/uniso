import type { AccountListItem } from '../../types'
import { useTheme } from '../../theme/ThemeContext'
import { d, serviceIcons } from '../../theme/tokens'

interface WebViewHeaderBarProps {
  account: AccountListItem | null
  currentUrl: string | null
}

export function WebViewHeaderBar({ account, currentUrl }: WebViewHeaderBarProps) {
  const { c } = useTheme()

  if (!account) return null

  const icon = serviceIcons[account.serviceId] ?? '?'

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: d.sidebarWidth,
        right: 0,
        height: d.headerHeight,
        background: c.bgPrimary,
        borderBottom: `0.5px solid ${c.borderSecondary}`,
        display: 'flex',
        alignItems: 'center',
        padding: '0 16px',
        gap: 8,
        zIndex: 10,
        // @ts-expect-error Electron-specific CSS property
        WebkitAppRegion: 'drag',
      }}
    >
      {/* Service icon */}
      <div
        style={{
          width: 18,
          height: 18,
          borderRadius: d.borderRadius.xs,
          background: account.brandColor,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 10,
          color: c.textPrimary,
          flexShrink: 0,
        }}
      >
        {icon}
      </div>

      {/* Account name */}
      <span
        style={{
          fontSize: d.fontSize.md,
          fontWeight: 500,
          color: c.textPrimary,
          whiteSpace: 'nowrap',
        }}
      >
        {account.serviceDisplayName} — {account.displayName}
      </span>

      {/* URL */}
      <span
        style={{
          fontSize: d.fontSize.sm,
          color: c.textTertiary,
          marginLeft: 'auto',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          maxWidth: '50%',
        }}
      >
        {currentUrl}
      </span>
    </div>
  )
}
