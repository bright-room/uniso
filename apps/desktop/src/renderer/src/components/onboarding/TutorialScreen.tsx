import { useState } from 'react'
import { serviceIcons } from '../../theme/tokens'

interface TutorialScreenProps {
  onComplete: () => void
  t: (key: string) => string
}

const SERVICE_BRANDS: Record<string, string> = {
  x: '#000000',
  instagram: '#E1306C',
  facebook: '#1877F2',
  youtube: '#FF0000',
  bluesky: '#0085FF',
  twitch: '#9146FF',
}

const TOTAL_STEPS = 4

export function TutorialScreen({ onComplete, t }: TutorialScreenProps) {
  const [step, setStep] = useState(0)

  const handleNext = () => {
    if (step === TOTAL_STEPS - 1) {
      onComplete()
    } else {
      setStep(step + 1)
    }
  }

  const handleBack = () => {
    if (step > 0) {
      setStep(step - 1)
    }
  }

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        background: '#1a1a2e',
        zIndex: 2000,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {/* Step content */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 16,
        }}
      >
        {step === 0 && <WelcomeStep />}
        {step === 1 && <AddAccountStep />}
        {step === 2 && <SwitchAccountStep />}
        {step === 3 && <CompleteStep />}

        <h2 style={{ fontSize: 20, fontWeight: 600, color: '#ffffff', margin: 0 }}>
          {step === 0 && t('tutorial.welcome.title')}
          {step === 1 && t('tutorial.add_account.title')}
          {step === 2 && t('tutorial.switch_account.title')}
          {step === 3 && t('tutorial.complete.title')}
        </h2>
        <p
          style={{ fontSize: 14, color: '#aaaaaa', margin: 0, textAlign: 'center', maxWidth: 360 }}
        >
          {step === 0 && t('tutorial.welcome.description')}
          {step === 1 && t('tutorial.add_account.description')}
          {step === 2 && t('tutorial.switch_account.description')}
          {step === 3 && t('tutorial.complete.description')}
        </p>
      </div>

      {/* Dot navigation */}
      <div
        style={{
          display: 'flex',
          gap: 6,
          marginTop: 32,
        }}
      >
        {Array.from({ length: TOTAL_STEPS }).map((_, i) => (
          <div
            key={i}
            style={{
              width: 6,
              height: 6,
              borderRadius: '50%',
              background: i === step ? '#ffffff' : '#3a3a5a',
            }}
          />
        ))}
      </div>

      {/* Navigation buttons */}
      <div
        style={{
          display: 'flex',
          gap: 12,
          marginTop: 24,
        }}
      >
        <button
          onClick={handleBack}
          style={{
            visibility: step === 0 ? 'hidden' : 'visible',
            padding: '8px 20px',
            borderRadius: 8,
            fontSize: 13,
            fontWeight: 500,
            color: '#aaaaaa',
            background: '#2a2a4a',
            border: '0.5px solid #3a3a5a',
            cursor: 'pointer',
          }}
        >
          {t('tutorial.back')}
        </button>
        <button
          onClick={handleNext}
          style={{
            padding: '8px 20px',
            borderRadius: 8,
            fontSize: 13,
            fontWeight: 500,
            color: '#ffffff',
            background: '#3a3a5a',
            border: '0.5px solid #3a3a5a',
            cursor: 'pointer',
          }}
        >
          {step === TOTAL_STEPS - 1 ? t('tutorial.start') : t('tutorial.next')}
        </button>
      </div>
    </div>
  )
}

function WelcomeStep() {
  const services = Object.keys(SERVICE_BRANDS)

  return (
    <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
      {services.map((service) => (
        <div
          key={service}
          style={{
            width: 40,
            height: 40,
            borderRadius: 10,
            background: SERVICE_BRANDS[service],
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 18,
            color: '#ffffff',
          }}
        >
          {serviceIcons[service]}
        </div>
      ))}
    </div>
  )
}

function AddAccountStep() {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 8,
        marginBottom: 8,
      }}
    >
      <div
        style={{
          width: 32,
          height: 32,
          borderRadius: '50%',
          border: '2px dashed #3a3a5a',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 16,
          color: '#666666',
        }}
      >
        +
      </div>
      <div
        style={{
          padding: '4px 12px',
          borderRadius: 12,
          background: '#2a2a4a',
          fontSize: 12,
          color: '#aaaaaa',
        }}
      >
        Click to add
      </div>
    </div>
  )
}

function SwitchAccountStep() {
  const services = Object.keys(SERVICE_BRANDS).slice(0, 4)

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 6,
        padding: '12px 8px',
        background: '#16162a',
        borderRadius: 10,
        border: '0.5px solid #2a2a4a',
        marginBottom: 8,
      }}
    >
      {services.map((service, i) => (
        <div
          key={service}
          style={{
            width: 36,
            height: 36,
            borderRadius: 8,
            background: i === 0 ? '#ffffff' : SERVICE_BRANDS[service],
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 14,
            color: i === 0 ? '#000000' : '#ffffff',
          }}
        >
          {serviceIcons[service]}
        </div>
      ))}
    </div>
  )
}

function CompleteStep() {
  return <div style={{ fontSize: 48, marginBottom: 8 }}>🎉</div>
}
