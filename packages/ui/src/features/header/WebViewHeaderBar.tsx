import type { AccountListItem } from '../../types'
import { serviceIcons } from '../../theme/tokens'
import styles from './WebViewHeaderBar.module.css'

interface WebViewHeaderBarProps {
  account: AccountListItem | null
  currentUrl: string | null
}

export function WebViewHeaderBar({ account, currentUrl }: WebViewHeaderBarProps) {
  if (!account) return null

  const icon = serviceIcons[account.serviceId] ?? '?'

  return (
    <div className={styles.header}>
      <div className={styles.serviceIcon} style={{ background: account.brandColor }}>
        {icon}
      </div>
      <span className={styles.accountName}>
        {account.serviceDisplayName} — {account.displayName}
      </span>
      <span className={styles.url}>{currentUrl}</span>
    </div>
  )
}
