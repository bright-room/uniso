import type { MouseEvent } from 'react'
import type { AccountListItem } from '../../types'
import { ServiceIcon } from '../../primitives/ServiceIcon'
import { d, serviceIconBackgrounds } from '../../theme/tokens'
import styles from './AccountItem.module.css'

interface AccountItemProps {
  account: AccountListItem
  onSwitch: (accountId: string) => void
  onContextMenu: () => void
}

export function AccountItem({ account, onSwitch, onContextMenu }: AccountItemProps) {
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

      <ServiceIcon
        src={`/${account.iconResource}`}
        size={d.serviceIconSize}
        backgroundColor={serviceIconBackgrounds[account.serviceId] ?? '#ffffff'}
        borderRadius={d.serviceIconRadius}
      />

      <div
        className={styles.initials}
        style={{ background: `${account.brandColor}33` }}
      >
        {initials}
      </div>
    </button>
  )
}
