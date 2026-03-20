import { DialogOverlay } from '../../primitives/DialogOverlay'
import dialogStyles from './dialog.module.css'

interface TelemetryConsentDialogProps {
  onAllow: () => void
  onDeny: () => void
  t: (key: string) => string
}

export function TelemetryConsentDialog({ onAllow, onDeny, t }: TelemetryConsentDialogProps) {
  return (
    <DialogOverlay onClose={onDeny}>
      <div className={dialogStyles.dialogWide}>
        <h2 className={dialogStyles.title} style={{ marginBottom: 12 }}>
          {t('dialog.telemetry.title')}
        </h2>
        <p className={dialogStyles.description} style={{ marginBottom: 20 }}>
          {t('dialog.telemetry.message')}
        </p>

        <div className={dialogStyles.actions}>
          <button className={dialogStyles.btnSecondary} onClick={onDeny}>
            {t('dialog.telemetry.deny')}
          </button>
          <button className={dialogStyles.btnSubtle} onClick={onAllow}>
            {t('dialog.telemetry.allow')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
