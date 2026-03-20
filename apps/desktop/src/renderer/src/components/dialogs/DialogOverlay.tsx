import type { ReactNode } from 'react'

interface DialogOverlayProps {
  children: ReactNode
  onClose: () => void
}

export function DialogOverlay({ children, onClose }: DialogOverlayProps) {
  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed',
        inset: 0,
        background: 'rgba(0,0,0,0.5)',
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
