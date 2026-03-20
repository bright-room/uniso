import { useTheme } from '../../theme/ThemeContext'
import { d } from '../../theme/tokens'
import { DialogOverlay } from './DialogOverlay'

interface TelemetryConsentDialogProps {
  onAllow: () => void
  onDeny: () => void
  t: (key: string) => string
}

export function TelemetryConsentDialog({ onAllow, onDeny, t }: TelemetryConsentDialogProps) {
  const { c } = useTheme()

  return (
    <DialogOverlay onClose={onDeny}>
      <div
        style={{
          background: c.bgPrimary,
          borderRadius: d.dialogBorderRadius,
          padding: d.dialogPadding,
          width: d.dialogWidthWide,
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
          {t('dialog.telemetry.title')}
        </h2>
        <p style={{ fontSize: d.fontSize.md, color: c.textSecondary, marginBottom: 20 }}>
          {t('dialog.telemetry.message')}
        </p>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button
            onClick={onDeny}
            style={{
              padding: '8px 16px',
              borderRadius: d.borderRadius.sm,
              fontSize: d.fontSize.md,
              color: c.textSecondary,
              background: c.bgTertiary,
            }}
          >
            {t('dialog.telemetry.deny')}
          </button>
          <button
            onClick={onAllow}
            style={{
              padding: '8px 16px',
              borderRadius: d.borderRadius.sm,
              fontSize: d.fontSize.md,
              color: c.textPrimary,
              background: c.borderPrimary,
            }}
          >
            {t('dialog.telemetry.allow')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
