import { serviceIcons } from '../../theme/tokens'

interface WebViewHeaderBarProps {
  account: AccountListItem | null
  currentUrl: string | null
}

export function WebViewHeaderBar({ account, currentUrl }: WebViewHeaderBarProps) {
  if (!account) return null

  const icon = serviceIcons[account.serviceId] ?? '?'

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 80,
        right: 0,
        height: 40,
        background: '#1a1a2e',
        borderBottom: '0.5px solid #2a2a4a',
        display: 'flex',
        alignItems: 'center',
        padding: '0 16px',
        gap: 8,
        zIndex: 10,
        // @ts-expect-error Electron-specific CSS property
        WebkitAppRegion: 'drag',
      }}
    >
      {/* Service icon */}
      <div
        style={{
          width: 18,
          height: 18,
          borderRadius: 4,
          background: account.brandColor,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 10,
          color: '#ffffff',
          flexShrink: 0,
        }}
      >
        {icon}
      </div>

      {/* Account name */}
      <span
        style={{
          fontSize: 13,
          fontWeight: 500,
          color: '#ffffff',
          whiteSpace: 'nowrap',
        }}
      >
        {account.serviceDisplayName} — {account.displayName}
      </span>

      {/* URL */}
      <span
        style={{
          fontSize: 12,
          color: '#666666',
          marginLeft: 'auto',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          maxWidth: '50%',
        }}
      >
        {currentUrl}
      </span>
    </div>
  )
}
