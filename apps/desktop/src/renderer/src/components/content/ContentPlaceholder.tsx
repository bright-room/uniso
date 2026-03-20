import { serviceIcons } from '../../theme/tokens'

interface ContentPlaceholderProps {
  account: AccountListItem | null
  t: (key: string) => string
}

export function ContentPlaceholder({ account, t }: ContentPlaceholderProps) {
  const icon = account ? (serviceIcons[account.serviceId] ?? '?') : null

  return (
    <div
      style={{
        position: 'fixed',
        top: 40,
        left: 80,
        right: 0,
        bottom: 0,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'transparent',
      }}
    >
      {account ? (
        <>
          {/* Service icon */}
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: 16,
              background: account.brandColor,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 28,
              color: '#ffffff',
              marginBottom: 12,
            }}
          >
            {icon}
          </div>

          {/* Service name */}
          <span
            style={{
              fontSize: 15,
              fontWeight: 500,
              color: '#ffffff',
              marginBottom: 4,
            }}
          >
            {account.serviceDisplayName}
          </span>

          {/* Account display name */}
          <span
            style={{
              fontSize: 13,
              color: '#aaaaaa',
            }}
          >
            {account.displayName}
          </span>
        </>
      ) : (
        <span
          style={{
            fontSize: 13,
            color: '#666666',
          }}
        >
          {t('content.no_account')}
        </span>
      )}
    </div>
  )
}
