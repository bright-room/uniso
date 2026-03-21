import { useState } from 'react'
import { ServiceIcon } from '../../primitives/ServiceIcon'
import { serviceIconBackgrounds, serviceIconFiles } from '../../theme/tokens'
import styles from './TutorialScreen.module.css'

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
    <div className={styles.screen}>
      <div className={styles.stepContent}>
        {step === 0 && <WelcomeStep />}
        {step === 1 && <AddAccountStep />}
        {step === 2 && <SwitchAccountStep />}
        {step === 3 && <CompleteStep />}

        <h2 className={styles.stepTitle}>
          {step === 0 && t('tutorial.welcome.title')}
          {step === 1 && t('tutorial.add_account.title')}
          {step === 2 && t('tutorial.switch_account.title')}
          {step === 3 && t('tutorial.complete.title')}
        </h2>
        <p className={styles.stepDescription}>
          {step === 0 && t('tutorial.welcome.description')}
          {step === 1 && t('tutorial.add_account.description')}
          {step === 2 && t('tutorial.switch_account.description')}
          {step === 3 && t('tutorial.complete.description')}
        </p>
      </div>

      <div className={styles.dots}>
        {Array.from({ length: TOTAL_STEPS }).map((_, i) => (
          <div key={i} className={`${styles.dot} ${i === step ? styles.active : ''}`} />
        ))}
      </div>

      <div className={styles.nav}>
        <button
          onClick={handleBack}
          className={styles.navButtonBack}
          style={{ visibility: step === 0 ? 'hidden' : 'visible' }}
        >
          {t('tutorial.back')}
        </button>
        <button onClick={handleNext} className={styles.navButton}>
          {step === TOTAL_STEPS - 1 ? t('tutorial.start') : t('tutorial.next')}
        </button>
      </div>
    </div>
  )
}

function WelcomeStep() {
  const services = Object.keys(SERVICE_BRANDS)

  return (
    <div className={styles.serviceBrands}>
      {services.map((service) => (
        <ServiceIcon
          key={service}
          src={`./${serviceIconFiles[service]}`}
          size={48}
          backgroundColor={serviceIconBackgrounds[service] ?? '#ffffff'}
          borderRadius={12}
        />
      ))}
    </div>
  )
}

function AddAccountStep() {
  return (
    <div className={styles.addAccountDemo}>
      <div className={styles.addCircle}>+</div>
      <div className={styles.addLabel}>Click to add</div>
    </div>
  )
}

function SwitchAccountStep() {
  const services = Object.keys(SERVICE_BRANDS).slice(0, 4)

  return (
    <div className={styles.switchDemo}>
      {services.map((service, i) => (
        <div
          key={service}
          className={`${styles.switchItem} ${i === 0 ? styles.activeItem : ''}`}
        >
          <ServiceIcon
            src={`./${serviceIconFiles[service]}`}
            size={32}
            backgroundColor={i === 0 ? 'transparent' : serviceIconBackgrounds[service] ?? '#ffffff'}
            borderRadius={8}
          />
        </div>
      ))}
    </div>
  )
}

function CompleteStep() {
  return <div className={styles.celebration}>🎉</div>
}
