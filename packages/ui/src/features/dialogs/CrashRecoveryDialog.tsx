import { DialogOverlay } from '../../primitives/DialogOverlay'
import dialogStyles from './dialog.module.css'

interface CrashRecoveryDialogProps {
  onRestore: () => void
  onStartFresh: () => void
  t: (key: string) => string
}

export function CrashRecoveryDialog({ onRestore, onStartFresh, t }: CrashRecoveryDialogProps) {
  return (
    <DialogOverlay onClose={onRestore}>
      <div className={dialogStyles.dialog}>
        <h2 className={dialogStyles.title} style={{ marginBottom: 12 }}>
          {t('dialog.crash.title')}
        </h2>
        <p className={dialogStyles.description} style={{ marginBottom: 20 }}>
          {t('dialog.crash.message')}
        </p>

        <div className={dialogStyles.actions}>
          <button className={dialogStyles.btnSecondary} onClick={onStartFresh}>
            {t('button.start_new')}
          </button>
          <button className={dialogStyles.btnPrimary} onClick={onRestore}>
            {t('button.restore')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
