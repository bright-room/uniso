import { DialogOverlay } from './DialogOverlay'

interface DeleteAccountDialogProps {
  account: AccountListItem
  onClose: () => void
  onConfirm: () => void
  t: (key: string) => string
}

export function DeleteAccountDialog({ account, onClose, onConfirm, t }: DeleteAccountDialogProps) {
  return (
    <DialogOverlay onClose={onClose}>
      <div
        style={{
          background: '#1a1a2e',
          borderRadius: 12,
          padding: 24,
          width: 320,
          border: '0.5px solid #2a2a4a',
        }}
      >
        <h2
          style={{
            fontSize: 16,
            fontWeight: 600,
            color: '#ffffff',
            marginBottom: 12,
          }}
        >
          {t('dialog.delete.title')}
        </h2>

        <p style={{ fontSize: 13, color: '#aaaaaa', marginBottom: 8 }}>
          {account.serviceDisplayName} — {account.displayName}
        </p>

        <p style={{ fontSize: 13, color: '#ff4444', marginBottom: 20 }}>
          {t('dialog.delete.warning')}
        </p>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button
            onClick={onClose}
            style={{
              padding: '8px 16px',
              borderRadius: 6,
              fontSize: 13,
              color: '#aaaaaa',
              background: '#2a2a4a',
            }}
          >
            {t('button.cancel')}
          </button>
          <button
            onClick={onConfirm}
            style={{
              padding: '8px 16px',
              borderRadius: 6,
              fontSize: 13,
              color: '#ffffff',
              background: '#ff4444',
            }}
          >
            {t('button.delete')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
