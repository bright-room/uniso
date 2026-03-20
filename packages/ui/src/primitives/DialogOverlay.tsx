import type { ReactNode } from 'react'
import styles from './DialogOverlay.module.css'

interface DialogOverlayProps {
  children: ReactNode
  onClose: () => void
}

export function DialogOverlay({ children, onClose }: DialogOverlayProps) {
  return (
    <div className={styles.overlay} onClick={onClose}>
      <div onClick={(e) => e.stopPropagation()}>{children}</div>
    </div>
  )
}
