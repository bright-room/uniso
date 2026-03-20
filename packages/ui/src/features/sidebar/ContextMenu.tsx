import { useEffect, useRef } from 'react'
import type { AccountListItem } from '../../types'
import styles from './ContextMenu.module.css'

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
    <div ref={ref} className={styles.menu} style={{ left: x, top: y }}>
      <div className={styles.header}>
        {account.serviceDisplayName} — {account.displayName}
      </div>
      <button className={styles.deleteButton} onClick={() => onDelete(account.accountId)}>
        {t('dialog.delete.title')}
      </button>
    </div>
  )
}
