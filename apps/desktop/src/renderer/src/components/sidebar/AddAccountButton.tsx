import { useState } from 'react'

interface AddAccountButtonProps {
  onClick: () => void
}

export function AddAccountButton({ onClick }: AddAccountButtonProps) {
  const [hovered, setHovered] = useState(false)

  return (
    <button
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        width: 40,
        height: 40,
        borderRadius: '50%',
        border: `1.5px dashed ${hovered ? '#aaaaaa' : '#3a3a5a'}`,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: 18,
        color: hovered ? '#aaaaaa' : '#666666',
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
