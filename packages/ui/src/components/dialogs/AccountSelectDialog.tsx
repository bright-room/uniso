import type { AccountListItem } from '../../types'
import { useTheme } from '../../theme/ThemeContext'
import { d, serviceIcons } from '../../theme/tokens'
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
            marginBottom: 16,
          }}
        >
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
                borderRadius: d.borderRadius.md,
                background: 'transparent',
                transition: 'background 0.15s',
                width: '100%',
                textAlign: 'left',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = c.bgTertiary)}
              onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
            >
              <div
                style={{
                  width: d.serviceIconSize,
                  height: d.serviceIconSize,
                  borderRadius: d.serviceIconRadius,
                  background: account.brandColor,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: d.fontSize.sm,
                  color: c.textPrimary,
                }}
              >
                {serviceIcons[account.serviceId] ?? '?'}
              </div>
              <span style={{ fontSize: d.fontSize.md, color: c.textPrimary }}>
                {account.displayName}
              </span>
            </button>
          ))}
        </div>

        <button
          onClick={onClose}
          style={{
            width: '100%',
            padding: '8px 16px',
            borderRadius: d.borderRadius.sm,
            fontSize: d.fontSize.md,
            color: c.textSecondary,
            background: c.bgTertiary,
            textAlign: 'center',
          }}
        >
          {t('dialog.account_select.open_external')}
        </button>
      </div>
    </DialogOverlay>
  )
}
