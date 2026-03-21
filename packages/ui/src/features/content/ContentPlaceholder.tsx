import type { AccountListItem } from '../../types'
import { ServiceIcon } from '../../primitives/ServiceIcon'
import { serviceIconBackgrounds } from '../../theme/tokens'
import styles from './ContentPlaceholder.module.css'

interface ContentPlaceholderProps {
  account: AccountListItem | null
  t: (key: string) => string
}

export function ContentPlaceholder({ account, t }: ContentPlaceholderProps) {
  return (
    <div className={styles.container}>
      {account ? (
        <>
          <ServiceIcon
            src={`/${account.iconResource}`}
            size={64}
            backgroundColor={serviceIconBackgrounds[account.serviceId] ?? '#ffffff'}
            borderRadius={16}
          />
          <span className={styles.serviceName}>{account.serviceDisplayName}</span>
          <span className={styles.displayName}>{account.displayName}</span>
        </>
      ) : (
        <span className={styles.emptyText}>{t('content.no_account')}</span>
      )}
    </div>
  )
}
