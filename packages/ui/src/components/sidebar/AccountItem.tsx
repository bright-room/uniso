import { type MouseEvent, useState } from 'react'
import type { AccountListItem } from '../../types'
import { useTheme } from '../../theme/ThemeContext'
import { d, serviceIcons } from '../../theme/tokens'

interface AccountItemProps {
  account: AccountListItem
  onSwitch: (accountId: string) => void
  onContextMenu: () => void
}

export function AccountItem({ account, onSwitch, onContextMenu }: AccountItemProps) {
  const { c } = useTheme()
  const [hovered, setHovered] = useState(false)
  const icon = serviceIcons[account.serviceId] ?? '?'
  const initials = (account.displayName ?? account.serviceDisplayName).substring(0, 2).toUpperCase()

  const handleContextMenu = (e: MouseEvent) => {
    e.preventDefault()
    onContextMenu()
  }

  return (
    <button
      onClick={() => onSwitch(account.accountId)}
      onContextMenu={handleContextMenu}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        width: d.accountItemSize,
        height: d.accountItemSize,
        borderRadius: d.borderRadius.md,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        position: 'relative',
        background: account.isActive
          ? c.textPrimary
          : hovered
            ? c.hover
            : 'transparent',
        transition: 'background 0.15s',
        padding: 0,
      }}
    >
      {/* Active indicator */}
      {account.isActive && (
        <div
          style={{
            position: 'absolute',
            left: -12,
            top: 12,
            bottom: 12,
            width: d.activeIndicatorWidth,
            borderRadius: '0 2px 2px 0',
            background: '#1d1d1f',
          }}
        />
      )}

      {/* Service icon */}
      <div
        style={{
          width: d.serviceIconSize,
          height: d.serviceIconSize,
          borderRadius: d.serviceIconRadius,
          background: account.brandColor,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: d.fontSize.sm,
          color: c.textPrimary,
          lineHeight: 1,
        }}
      >
        {icon}
      </div>

      {/* Avatar initials */}
      <div
        style={{
          width: d.avatarSize,
          height: d.avatarSize,
          borderRadius: '50%',
          background: `${account.brandColor}33`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: d.fontSize.xs,
          fontWeight: 600,
          color: account.isActive ? '#86868b' : c.textSecondary,
          lineHeight: 1,
        }}
      >
        {initials}
      </div>
    </button>
  )
}
