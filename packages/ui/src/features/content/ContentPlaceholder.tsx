import type { AccountListItem } from '../../types'
import { serviceIcons } from '../../theme/tokens'
import styles from './ContentPlaceholder.module.css'

interface ContentPlaceholderProps {
  account: AccountListItem | null
  t: (key: string) => string
}

export function ContentPlaceholder({ account, t }: ContentPlaceholderProps) {
  const icon = account ? (serviceIcons[account.serviceId] ?? '?') : null

  return (
    <div className={styles.container}>
      {account ? (
        <>
          <div className={styles.serviceIcon} style={{ background: account.brandColor }}>
            {icon}
          </div>
          <span className={styles.serviceName}>{account.serviceDisplayName}</span>
          <span className={styles.displayName}>{account.displayName}</span>
        </>
      ) : (
        <span className={styles.emptyText}>{t('content.no_account')}</span>
      )}
    </div>
  )
}
