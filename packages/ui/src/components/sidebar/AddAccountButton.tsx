import { useState } from 'react'
import { useTheme } from '../../theme/ThemeContext'
import { d } from '../../theme/tokens'

interface AddAccountButtonProps {
  onClick: () => void
}

export function AddAccountButton({ onClick }: AddAccountButtonProps) {
  const { c } = useTheme()
  const [hovered, setHovered] = useState(false)

  return (
    <button
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        width: d.addButtonSize,
        height: d.addButtonSize,
        borderRadius: '50%',
        border: `1.5px dashed ${hovered ? c.textSecondary : c.borderPrimary}`,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: 18,
        color: hovered ? c.textSecondary : c.textTertiary,
        background: 'transparent',
        transition: 'all 0.15s',
        marginTop: 8,
        flexShrink: 0,
      }}
    >
      +
    </button>
  )
}
