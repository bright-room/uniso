import type { AccountListItem } from '../../types'
import { ServiceIcon } from '../../primitives/ServiceIcon'
import { DialogOverlay } from '../../primitives/DialogOverlay'
import { d, serviceIconBackgrounds } from '../../theme/tokens'
import dialogStyles from './dialog.module.css'
import styles from './AccountSelectDialog.module.css'

interface AccountSelectDialogProps {
  accounts: AccountListItem[]
  url: string
  onSelect: (accountId: string) => void
  onClose: () => void
  t: (key: string) => string
}

export function AccountSelectDialog({
  accounts,
  url: _url,
  onSelect,
  onClose,
  t,
}: AccountSelectDialogProps) {
  return (
    <DialogOverlay onClose={onClose}>
      <div className={dialogStyles.dialog}>
        <h2 className={dialogStyles.title} style={{ marginBottom: 16 }}>
          {t('dialog.account_select.title')}
        </h2>

        <div className={styles.list}>
          {accounts.map((account) => (
            <button
              key={account.accountId}
              onClick={() => onSelect(account.accountId)}
              className={styles.accountButton}
            >
              <ServiceIcon
                src={`/${account.iconResource}`}
                size={d.serviceIconSize}
                backgroundColor={serviceIconBackgrounds[account.serviceId] ?? '#ffffff'}
                borderRadius={d.serviceIconRadius}
              />
              <span className={styles.accountName}>{account.displayName}</span>
            </button>
          ))}
        </div>

        <button className={styles.externalButton} onClick={onClose}>
          {t('dialog.account_select.open_external')}
        </button>
      </div>
    </DialogOverlay>
  )
}
