import type { AccountListItem } from '../../types'
import { DialogOverlay } from '../../primitives/DialogOverlay'
import dialogStyles from './dialog.module.css'

interface DeleteAccountDialogProps {
  account: AccountListItem
  onClose: () => void
  onConfirm: () => void
  t: (key: string) => string
}

export function DeleteAccountDialog({ account, onClose, onConfirm, t }: DeleteAccountDialogProps) {
  return (
    <DialogOverlay onClose={onClose}>
      <div className={dialogStyles.dialog}>
        <h2 className={dialogStyles.title} style={{ marginBottom: 12 }}>
          {t('dialog.delete.title')}
        </h2>
        <p className={dialogStyles.description} style={{ marginBottom: 8 }}>
          {account.serviceDisplayName} — {account.displayName}
        </p>
        <p style={{ fontSize: 'var(--font-md)', color: 'var(--color-danger)', marginBottom: 20 }}>
          {t('dialog.delete.warning')}
        </p>

        <div className={dialogStyles.actions}>
          <button className={dialogStyles.btnSecondary} onClick={onClose}>
            {t('button.cancel')}
          </button>
          <button className={dialogStyles.btnDanger} onClick={onConfirm}>
            {t('button.delete')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
