import { useState, type MouseEvent } from 'react'
import { serviceIcons } from '../../theme/tokens'

interface AccountItemProps {
  account: AccountListItem
  onSwitch: (accountId: string) => void
  onContextMenu: () => void
}

export function AccountItem({ account, onSwitch, onContextMenu }: AccountItemProps) {
  const [hovered, setHovered] = useState(false)
  const icon = serviceIcons[account.serviceId] ?? '?'
  const initials = (account.displayName ?? account.serviceDisplayName)
    .substring(0, 2)
    .toUpperCase()

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
        width: 48,
        height: 48,
        borderRadius: 8,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        position: 'relative',
        background: account.isActive
          ? '#ffffff'
          : hovered
            ? 'rgba(255,255,255,0.08)'
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
            width: 3,
            borderRadius: '0 2px 2px 0',
            background: '#1d1d1f',
          }}
        />
      )}

      {/* Service icon */}
      <div
        style={{
          width: 24,
          height: 24,
          borderRadius: 6,
          background: account.isActive ? account.brandColor : account.brandColor,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 12,
          color: '#ffffff',
          lineHeight: 1,
        }}
      >
        {icon}
      </div>

      {/* Avatar initials */}
      <div
        style={{
          width: 14,
          height: 14,
          borderRadius: '50%',
          background: `${account.brandColor}33`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 7,
          fontWeight: 600,
          color: account.isActive ? '#86868b' : '#aaaaaa',
          lineHeight: 1,
        }}
      >
        {initials}
      </div>
    </button>
  )
}
