import type { MouseEvent } from 'react'
import type { AccountListItem } from '../../types'
import { serviceIcons } from '../../theme/tokens'
import styles from './AccountItem.module.css'

interface AccountItemProps {
  account: AccountListItem
  onSwitch: (accountId: string) => void
  onContextMenu: () => void
}

export function AccountItem({ account, onSwitch, onContextMenu }: AccountItemProps) {
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
      className={`${styles.item} ${account.isActive ? styles.active : ''}`}
    >
      {account.isActive && <div className={styles.activeIndicator} />}

      <div
        className={styles.serviceIcon}
        style={{ background: account.brandColor }}
      >
        {icon}
      </div>

      <div
        className={styles.initials}
        style={{ background: `${account.brandColor}33` }}
      >
        {initials}
      </div>
    </button>
  )
}
