import { useTheme } from '../../theme/ThemeContext'
import { d } from '../../theme/tokens'
import { DialogOverlay } from './DialogOverlay'

interface CrashRecoveryDialogProps {
  onRestore: () => void
  onStartFresh: () => void
  t: (key: string) => string
}

export function CrashRecoveryDialog({ onRestore, onStartFresh, t }: CrashRecoveryDialogProps) {
  const { c } = useTheme()

  return (
    <DialogOverlay onClose={onRestore}>
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
            marginBottom: 12,
          }}
        >
          {t('dialog.crash.title')}
        </h2>
        <p style={{ fontSize: d.fontSize.md, color: c.textSecondary, marginBottom: 20 }}>
          {t('dialog.crash.message')}
        </p>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button
            onClick={onStartFresh}
            style={{
              padding: '8px 16px',
              borderRadius: d.borderRadius.sm,
              fontSize: d.fontSize.md,
              color: c.textSecondary,
              background: c.bgTertiary,
            }}
          >
            {t('button.start_new')}
          </button>
          <button
            onClick={onRestore}
            style={{
              padding: '8px 16px',
              borderRadius: d.borderRadius.sm,
              fontSize: d.fontSize.md,
              color: c.textPrimary,
              background: c.accent,
            }}
          >
            {t('button.restore')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
