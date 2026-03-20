import type { ServicePlugin } from '../../types'
import { useTheme } from '../../theme/ThemeContext'
import { d, serviceIcons } from '../../theme/tokens'
import { DialogOverlay } from './DialogOverlay'

interface AddAccountDialogProps {
  services: ServicePlugin[]
  onClose: () => void
  onAdd: (serviceId: string) => void
  t: (key: string) => string
}

export function AddAccountDialog({ services, onClose, onAdd, t }: AddAccountDialogProps) {
  const { c } = useTheme()

  return (
    <DialogOverlay onClose={onClose}>
      <div
        style={{
          background: c.bgPrimary,
          borderRadius: d.dialogBorderRadius,
          padding: d.dialogPadding,
          width: d.dialogWidth,
          border: `0.5px solid ${c.borderSecondary}`,
        }}
      >
        <h2
          style={{
            fontSize: d.fontSize.xl,
            fontWeight: 600,
            color: c.textPrimary,
            marginBottom: 4,
          }}
        >
          {t('dialog.add_account.title')}
        </h2>
        <p
          style={{
            fontSize: d.fontSize.md,
            color: c.textTertiary,
            marginBottom: 20,
          }}
        >
          {t('dialog.add_account.subtitle')}
        </p>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr 1fr',
            gap: 12,
          }}
        >
          {services.map((service) => (
            <button
              key={service.serviceId}
              onClick={() => {
                onAdd(service.serviceId)
                onClose()
              }}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: 8,
                padding: 12,
                borderRadius: d.borderRadius.md,
                background: 'transparent',
                transition: 'background 0.15s',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = c.bgTertiary)}
              onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
            >
              <div
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: d.borderRadius.lg,
                  background: service.brandColor,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: d.fontSize.xl,
                  color: c.textPrimary,
                }}
              >
                {serviceIcons[service.serviceId] ?? '?'}
              </div>
              <span style={{ fontSize: d.fontSize.sm, color: c.textSecondary }}>
                {service.displayName}
              </span>
            </button>
          ))}
        </div>
      </div>
    </DialogOverlay>
  )
}
