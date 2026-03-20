import type { AccountListItem } from '../../types'
import { useTheme } from '../../theme/ThemeContext'
import { d } from '../../theme/tokens'
import { DialogOverlay } from './DialogOverlay'

interface DeleteAccountDialogProps {
  account: AccountListItem
  onClose: () => void
  onConfirm: () => void
  t: (key: string) => string
}

export function DeleteAccountDialog({ account, onClose, onConfirm, t }: DeleteAccountDialogProps) {
  const { c } = useTheme()

  return (
    <DialogOverlay onClose={onClose}>
      <div
        style={{
          background: c.bgPrimary,
          borderRadius: d.dialogBorderRadius,
          padding: d.dialogPadding,
          width: d.dialogWidth,
          border: `0.5px solid ${c.borderSecondary}`,
        }}
      >
        <h2
          style={{
            fontSize: d.fontSize.xl,
            fontWeight: 600,
            color: c.textPrimary,
            marginBottom: 12,
          }}
        >
          {t('dialog.delete.title')}
        </h2>

        <p style={{ fontSize: d.fontSize.md, color: c.textSecondary, marginBottom: 8 }}>
          {account.serviceDisplayName} — {account.displayName}
        </p>

        <p style={{ fontSize: d.fontSize.md, color: c.danger, marginBottom: 20 }}>
          {t('dialog.delete.warning')}
        </p>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button
            onClick={onClose}
            style={{
              padding: '8px 16px',
              borderRadius: d.borderRadius.sm,
              fontSize: d.fontSize.md,
              color: c.textSecondary,
              background: c.bgTertiary,
            }}
          >
            {t('button.cancel')}
          </button>
          <button
            onClick={onConfirm}
            style={{
              padding: '8px 16px',
              borderRadius: d.borderRadius.sm,
              fontSize: d.fontSize.md,
              color: c.textPrimary,
              background: c.danger,
            }}
          >
            {t('button.delete')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
