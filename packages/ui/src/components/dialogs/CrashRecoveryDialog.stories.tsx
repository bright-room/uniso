import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { CrashRecoveryDialog } from './CrashRecoveryDialog'

const t = (key: string) => {
  const strings: Record<string, string> = {
    'dialog.crash.title': 'Restore Previous Session?',
    'dialog.crash.message':
      'It looks like Uniso didn\'t shut down properly. Would you like to restore your previous session?',
    'button.start_new': 'Start Fresh',
    'button.restore': 'Restore',
  }
  return strings[key] ?? key
}

const meta = {
  title: 'Dialogs/CrashRecoveryDialog',
  component: CrashRecoveryDialog,
  args: {
    onRestore: fn(),
    onStartFresh: fn(),
    t,
  },
  parameters: {
    layout: 'centered',
  },
} satisfies Meta<typeof CrashRecoveryDialog>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
