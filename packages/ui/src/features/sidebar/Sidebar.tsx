import type { AccountListItem } from '../../types'
import { AccountItem } from './AccountItem'
import { AddAccountButton } from './AddAccountButton'
import styles from './Sidebar.module.css'

interface SidebarProps {
  accounts: AccountListItem[]
  onSwitch: (accountId: string) => void
  onAddClick: () => void
  onSettingsClick: () => void
  onContextMenu: (account: AccountListItem) => void
  t: (key: string) => string
}

export function Sidebar({
  accounts,
  onSwitch,
  onAddClick,
  onSettingsClick,
  onContextMenu,
}: SidebarProps) {
  const grouped: { account: AccountListItem; showSeparator: boolean }[] = []
  for (let i = 0; i < accounts.length; i++) {
    const showSeparator = i > 0 && accounts[i].serviceId !== accounts[i - 1].serviceId
    grouped.push({ account: accounts[i], showSeparator })
  }

  return (
    <div className={styles.sidebar}>
      <div className={styles.titleBar} />

      <div className={styles.content}>
        <div className={styles.accountList}>
          {grouped.map(({ account, showSeparator }) => (
            <div key={account.accountId} style={{ display: 'contents' }}>
              {showSeparator && <div className={styles.separator} />}
              <AccountItem
                account={account}
                onSwitch={onSwitch}
                onContextMenu={() => onContextMenu(account)}
              />
            </div>
          ))}
        </div>

        <div className={styles.bottom}>
          <AddAccountButton onClick={onAddClick} />
          <SettingsButton onClick={onSettingsClick} />
        </div>
      </div>
    </div>
  )
}

function SettingsButton({ onClick }: { onClick: () => void }) {
  return (
    <button onClick={onClick} className={styles.settingsButton}>
      <svg
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        className={styles.settingsIcon}
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <circle cx="12" cy="12" r="3" />
        <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z" />
      </svg>
    </button>
  )
}
