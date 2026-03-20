import { DialogOverlay } from './DialogOverlay'

interface TelemetryConsentDialogProps {
  onAllow: () => void
  onDeny: () => void
  t: (key: string) => string
}

export function TelemetryConsentDialog({ onAllow, onDeny, t }: TelemetryConsentDialogProps) {
  return (
    <DialogOverlay onClose={onDeny}>
      <div
        style={{
          background: '#1a1a2e',
          borderRadius: 12,
          padding: 24,
          width: 380,
          border: '0.5px solid #2a2a4a',
        }}
      >
        <h2 style={{ fontSize: 16, fontWeight: 600, color: '#ffffff', marginBottom: 12 }}>
          {t('dialog.telemetry.title')}
        </h2>
        <p style={{ fontSize: 13, color: '#aaaaaa', marginBottom: 20 }}>
          {t('dialog.telemetry.message')}
        </p>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button
            onClick={onDeny}
            style={{
              padding: '8px 16px',
              borderRadius: 6,
              fontSize: 13,
              color: '#aaaaaa',
              background: '#2a2a4a',
            }}
          >
            {t('dialog.telemetry.deny')}
          </button>
          <button
            onClick={onAllow}
            style={{
              padding: '8px 16px',
              borderRadius: 6,
              fontSize: 13,
              color: '#ffffff',
              background: '#3a3a5a',
            }}
          >
            {t('dialog.telemetry.allow')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
