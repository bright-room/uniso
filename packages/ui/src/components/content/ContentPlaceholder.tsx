import type { AccountListItem } from '../../types'
import { useTheme } from '../../theme/ThemeContext'
import { d, serviceIcons } from '../../theme/tokens'

interface ContentPlaceholderProps {
  account: AccountListItem | null
  t: (key: string) => string
}

export function ContentPlaceholder({ account, t }: ContentPlaceholderProps) {
  const { c } = useTheme()
  const icon = account ? (serviceIcons[account.serviceId] ?? '?') : null

  return (
    <div
      style={{
        position: 'fixed',
        top: d.headerHeight,
        left: d.sidebarWidth,
        right: 0,
        bottom: 0,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'transparent',
      }}
    >
      {account ? (
        <>
          {/* Service icon */}
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: d.borderRadius.xl + 4,
              background: account.brandColor,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 28,
              color: c.textPrimary,
              marginBottom: 12,
            }}
          >
            {icon}
          </div>

          {/* Service name */}
          <span
            style={{
              fontSize: 15,
              fontWeight: 500,
              color: c.textPrimary,
              marginBottom: 4,
            }}
          >
            {account.serviceDisplayName}
          </span>

          {/* Account display name */}
          <span
            style={{
              fontSize: d.fontSize.md,
              color: c.textSecondary,
            }}
          >
            {account.displayName}
          </span>
        </>
      ) : (
        <span
          style={{
            fontSize: d.fontSize.md,
            color: c.textTertiary,
          }}
        >
          {t('content.no_account')}
        </span>
      )}
    </div>
  )
}
