import styles from './ServiceIcon.module.css'

interface ServiceIconProps {
  src: string
  size?: number
  backgroundColor?: string
  borderRadius?: number
}

export function ServiceIcon({
  src,
  size = 24,
  backgroundColor = '#ffffff',
  borderRadius = 6,
}: ServiceIconProps) {
  return (
    <div
      className={styles.container}
      style={{
        width: size,
        height: size,
        borderRadius,
        background: backgroundColor,
      }}
    >
      <img className={styles.icon} src={src} alt="" />
    </div>
  )
}
