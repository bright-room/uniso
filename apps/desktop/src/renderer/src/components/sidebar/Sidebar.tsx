import { useEffect, useState } from 'react'
import { AccountItem } from './AccountItem'
import { AddAccountButton } from './AddAccountButton'

interface SidebarProps {
  accounts: AccountListItem[]
  onSwitch: (accountId: string) => void
  onAddClick: () => void
  onDelete: (accountId: string) => void
  onSettingsClick: () => void
  t: (key: string) => string
}

export function Sidebar({
  accounts,
  onSwitch,
  onAddClick,
  onDelete,
  onSettingsClick,
}: SidebarProps) {
  // Listen for delete action from native context menu
  useEffect(() => {
    return window.api.onContextMenuDelete((accountId) => {
      onDelete(accountId)
    })
  }, [onDelete])

  const handleContextMenu = (account: AccountListItem) => {
    window.api.showContextMenu(
      account.accountId,
      account.serviceDisplayName,
      account.displayName
    )
  }

  // Group accounts by service for separators
  const grouped: { account: AccountListItem; showSeparator: boolean }[] = []
  for (let i = 0; i < accounts.length; i++) {
    const showSeparator = i > 0 && accounts[i].serviceId !== accounts[i - 1].serviceId
    grouped.push({ account: accounts[i], showSeparator })
  }

  return (
    <div
      style={{
        width: 80,
        height: '100%',
        background: '#16162a',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        borderRight: '0.5px solid #2a2a4a',
        flexShrink: 0,
      }}
    >
      {/* Draggable title bar area for traffic lights on macOS */}
      <div
        style={{
          width: '100%',
          height: 48,
          flexShrink: 0,
          // @ts-expect-error Electron-specific CSS property
          WebkitAppRegion: 'drag',
        }}
      />

      {/* Sidebar content below traffic lights */}
      <div
        style={{
          flex: 1,
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          overflow: 'hidden',
        }}
      >
        {/* Account list */}
        <div
          style={{
            flex: 1,
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 4,
            padding: '8px 0',
            width: '100%',
          }}
        >
          {grouped.map(({ account, showSeparator }) => (
            <div key={account.accountId} style={{ display: 'contents' }}>
              {showSeparator && (
                <div
                  style={{
                    width: 32,
                    height: 0.5,
                    background: '#2a2a4a',
                    margin: '4px 0',
                    flexShrink: 0,
                  }}
                />
              )}
              <AccountItem
                account={account}
                onSwitch={onSwitch}
                onContextMenu={() => handleContextMenu(account)}
              />
            </div>
          ))}
        </div>

        {/* Bottom area */}
        <div
          style={{
            padding: '8px 0 12px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 8,
          }}
        >
          <AddAccountButton onClick={onAddClick} />
          <SettingsButton onClick={onSettingsClick} />
        </div>
      </div>
    </div>
  )
}

function SettingsButton({ onClick }: { onClick: () => void }) {
  const [hovered, setHovered] = useState(false)

  return (
    <button
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        width: 32,
        height: 32,
        borderRadius: '50%',
        border: 'none',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: hovered ? 'rgba(255,255,255,0.08)' : 'transparent',
        transition: 'background 0.15s',
        cursor: 'pointer',
        padding: 0,
      }}
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={hovered ? '#aaaaaa' : '#555555'} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="3" />
        <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z" />
      </svg>
    </button>
  )
}
