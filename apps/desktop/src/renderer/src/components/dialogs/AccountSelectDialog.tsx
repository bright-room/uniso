import { serviceIcons } from '../../theme/tokens'
import { DialogOverlay } from './DialogOverlay'

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
      <div
        style={{
          background: '#1a1a2e',
          borderRadius: 12,
          padding: 24,
          width: 320,
          border: '0.5px solid #2a2a4a',
        }}
      >
        <h2 style={{ fontSize: 16, fontWeight: 600, color: '#ffffff', marginBottom: 16 }}>
          {t('dialog.account_select.title')}
        </h2>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 16 }}>
          {accounts.map((account) => (
            <button
              key={account.accountId}
              onClick={() => onSelect(account.accountId)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                padding: '10px 12px',
                borderRadius: 8,
                background: 'transparent',
                transition: 'background 0.15s',
                width: '100%',
                textAlign: 'left',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = '#2a2a4a')}
              onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
            >
              <div
                style={{
                  width: 24,
                  height: 24,
                  borderRadius: 6,
                  background: account.brandColor,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 12,
                  color: '#ffffff',
                }}
              >
                {serviceIcons[account.serviceId] ?? '?'}
              </div>
              <span style={{ fontSize: 13, color: '#ffffff' }}>{account.displayName}</span>
            </button>
          ))}
        </div>

        <button
          onClick={onClose}
          style={{
            width: '100%',
            padding: '8px 16px',
            borderRadius: 6,
            fontSize: 13,
            color: '#aaaaaa',
            background: '#2a2a4a',
            textAlign: 'center',
          }}
        >
          {t('dialog.account_select.open_external')}
        </button>
      </div>
    </DialogOverlay>
  )
}
