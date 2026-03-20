import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { TelemetryConsentDialog } from './TelemetryConsentDialog'

const t = (key: string) => {
  const strings: Record<string, string> = {
    'dialog.telemetry.title': 'Help Improve Uniso',
    'dialog.telemetry.message':
      'We collect anonymous usage data to improve Uniso. No personal information is collected. You can change this at any time in Settings.',
    'dialog.telemetry.allow': 'Allow',
    'dialog.telemetry.deny': 'No Thanks',
  }
  return strings[key] ?? key
}

const meta = {
  title: 'Dialogs/TelemetryConsentDialog',
  component: TelemetryConsentDialog,
  args: {
    onAllow: fn(),
    onDeny: fn(),
    t,
  },
  parameters: {
    layout: 'centered',
  },
} satisfies Meta<typeof TelemetryConsentDialog>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
