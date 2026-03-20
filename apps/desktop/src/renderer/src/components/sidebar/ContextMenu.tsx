import { useEffect, useRef } from 'react'

interface ContextMenuProps {
  account: AccountListItem
  x: number
  y: number
  onClose: () => void
  onDelete: (accountId: string) => void
  t: (key: string) => string
}

export function ContextMenu({ account, x, y, onClose, onDelete, t }: ContextMenuProps) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        onClose()
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [onClose])

  return (
    <div
      ref={ref}
      style={{
        position: 'fixed',
        left: x,
        top: y,
        background: '#1a1a2e',
        border: '0.5px solid #2a2a4a',
        borderRadius: 8,
        boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
        minWidth: 160,
        padding: '4px 0',
        zIndex: 9999,
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: '6px 12px',
          fontSize: 12,
          color: '#666666',
          borderBottom: '0.5px solid #2a2a4a',
        }}
      >
        {account.serviceDisplayName} — {account.displayName}
      </div>

      {/* Delete */}
      <button
        onClick={() => onDelete(account.accountId)}
        style={{
          width: '100%',
          padding: '6px 12px',
          fontSize: 13,
          color: '#ff4444',
          textAlign: 'left',
          background: 'transparent',
        }}
        onMouseEnter={(e) => (e.currentTarget.style.background = '#16162a')}
        onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
      >
        {t('dialog.delete.title')}
      </button>
    </div>
  )
}
