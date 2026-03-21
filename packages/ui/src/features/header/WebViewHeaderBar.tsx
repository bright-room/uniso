import type { AccountListItem } from '../../types'
import { ServiceIcon } from '../../primitives/ServiceIcon'
import { serviceIconBackgrounds } from '../../theme/tokens'
import styles from './WebViewHeaderBar.module.css'

interface WebViewHeaderBarProps {
  account: AccountListItem | null
  currentUrl: string | null
}

export function WebViewHeaderBar({ account, currentUrl }: WebViewHeaderBarProps) {
  if (!account) return null

  return (
    <div className={styles.header}>
      <ServiceIcon
        src={`./${account.iconResource}`}
        size={18}
        backgroundColor={serviceIconBackgrounds[account.serviceId] ?? '#ffffff'}
        borderRadius={4}
      />
      <span className={styles.accountName}>
        {account.serviceDisplayName} — {account.displayName}
      </span>
      <span className={styles.url}>{currentUrl}</span>
    </div>
  )
}
