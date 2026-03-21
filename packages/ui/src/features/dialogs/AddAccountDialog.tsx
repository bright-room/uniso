import type { ServicePlugin } from '../../types'
import { ServiceIcon } from '../../primitives/ServiceIcon'
import { DialogOverlay } from '../../primitives/DialogOverlay'
import { d, serviceIconBackgrounds } from '../../theme/tokens'
import dialogStyles from './dialog.module.css'
import styles from './AddAccountDialog.module.css'

interface AddAccountDialogProps {
  services: ServicePlugin[]
  onClose: () => void
  onAdd: (serviceId: string) => void
  t: (key: string) => string
}

export function AddAccountDialog({ services, onClose, onAdd, t }: AddAccountDialogProps) {
  return (
    <DialogOverlay onClose={onClose}>
      <div className={dialogStyles.dialog}>
        <h2 className={dialogStyles.title} style={{ marginBottom: 4 }}>
          {t('dialog.add_account.title')}
        </h2>
        <p className={dialogStyles.subtitle} style={{ marginBottom: 20 }}>
          {t('dialog.add_account.subtitle')}
        </p>

        <div className={styles.grid}>
          {services.map((service) => (
            <button
              key={service.serviceId}
              onClick={() => {
                onAdd(service.serviceId)
                onClose()
              }}
              className={styles.serviceButton}
            >
              <ServiceIcon
                src={`/${service.iconResource}`}
                size={36}
                backgroundColor={serviceIconBackgrounds[service.serviceId] ?? '#ffffff'}
                borderRadius={d.borderRadius.lg}
              />
              <span className={styles.serviceName}>{service.displayName}</span>
            </button>
          ))}
        </div>
      </div>
    </DialogOverlay>
  )
}
