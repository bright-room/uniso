import type { ReactNode } from 'react'
import { useTheme } from '../../theme/ThemeContext'

interface DialogOverlayProps {
  children: ReactNode
  onClose: () => void
}

export function DialogOverlay({ children, onClose }: DialogOverlayProps) {
  const { c } = useTheme()

  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed',
        inset: 0,
        background: c.overlay,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
      }}
    >
      <div onClick={(e) => e.stopPropagation()}>{children}</div>
    </div>
  )
}
