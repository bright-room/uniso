import { DialogOverlay } from './DialogOverlay'

interface CrashRecoveryDialogProps {
  onRestore: () => void
  onStartFresh: () => void
  t: (key: string) => string
}

export function CrashRecoveryDialog({ onRestore, onStartFresh, t }: CrashRecoveryDialogProps) {
  return (
    <DialogOverlay onClose={onRestore}>
      <div
        style={{
          background: '#1a1a2e',
          borderRadius: 12,
          padding: 24,
          width: 320,
          border: '0.5px solid #2a2a4a',
        }}
      >
        <h2 style={{ fontSize: 16, fontWeight: 600, color: '#ffffff', marginBottom: 12 }}>
          {t('dialog.crash.title')}
        </h2>
        <p style={{ fontSize: 13, color: '#aaaaaa', marginBottom: 20 }}>
          {t('dialog.crash.message')}
        </p>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button
            onClick={onStartFresh}
            style={{
              padding: '8px 16px',
              borderRadius: 6,
              fontSize: 13,
              color: '#aaaaaa',
              background: '#2a2a4a',
            }}
          >
            {t('button.start_new')}
          </button>
          <button
            onClick={onRestore}
            style={{
              padding: '8px 16px',
              borderRadius: 6,
              fontSize: 13,
              color: '#ffffff',
              background: '#0085FF',
            }}
          >
            {t('button.restore')}
          </button>
        </div>
      </div>
    </DialogOverlay>
  )
}
