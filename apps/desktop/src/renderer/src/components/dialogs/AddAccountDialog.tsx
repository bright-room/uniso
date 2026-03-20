import { useState, useEffect } from 'react'
import { DialogOverlay } from './DialogOverlay'
import { serviceIcons } from '../../theme/tokens'

interface AddAccountDialogProps {
  onClose: () => void
  onAdd: (serviceId: string) => void
  t: (key: string) => string
}

export function AddAccountDialog({ onClose, onAdd, t }: AddAccountDialogProps) {
  const [services, setServices] = useState<ServicePlugin[]>([])

  useEffect(() => {
    window.api.getServicePlugins().then(setServices)
  }, [])

  return (
    <DialogOverlay onClose={onClose}>
      <div
        style={{
          background: '#1a1a2e',
          borderRadius: 12,
          padding: 24,
          width: 320,
          border: '0.5px solid #2a2a4a',
        }}
      >
        <h2
          style={{
            fontSize: 16,
            fontWeight: 600,
            color: '#ffffff',
            marginBottom: 4,
          }}
        >
          {t('dialog.add_account.title')}
        </h2>
        <p
          style={{
            fontSize: 13,
            color: '#666666',
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
                borderRadius: 8,
                background: 'transparent',
                transition: 'background 0.15s',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = '#2a2a4a')}
              onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
            >
              <div
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: 10,
                  background: service.brandColor,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 16,
                  color: '#ffffff',
                }}
              >
                {serviceIcons[service.serviceId] ?? '?'}
              </div>
              <span style={{ fontSize: 12, color: '#aaaaaa' }}>{service.displayName}</span>
            </button>
          ))}
        </div>
      </div>
    </DialogOverlay>
  )
}
