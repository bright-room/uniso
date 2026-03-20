import { useEffect, useRef } from 'react'
import type { AccountListItem } from '../../types'
import { useTheme } from '../../theme/ThemeContext'
import { d } from '../../theme/tokens'

interface ContextMenuProps {
  account: AccountListItem
  x: number
  y: number
  onClose: () => void
  onDelete: (accountId: string) => void
  t: (key: string) => string
}

export function ContextMenu({ account, x, y, onClose, onDelete, t }: ContextMenuProps) {
  const { c } = useTheme()
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
        background: c.bgPrimary,
        border: `0.5px solid ${c.borderSecondary}`,
        borderRadius: d.borderRadius.md,
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
          fontSize: d.fontSize.sm,
          color: c.textTertiary,
          borderBottom: `0.5px solid ${c.borderSecondary}`,
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
          fontSize: d.fontSize.md,
          color: c.danger,
          textAlign: 'left',
          background: 'transparent',
        }}
        onMouseEnter={(e) => (e.currentTarget.style.background = c.bgSecondary)}
        onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
      >
        {t('dialog.delete.title')}
      </button>
    </div>
  )
}
