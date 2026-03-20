import styles from './AddAccountButton.module.css'

interface AddAccountButtonProps {
  onClick: () => void
}

export function AddAccountButton({ onClick }: AddAccountButtonProps) {
  return (
    <button onClick={onClick} className={styles.button}>
      +
    </button>
  )
}
