import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { DeleteAccountDialog } from './DeleteAccountDialog'

const t = (key: string) => {
  const strings: Record<string, string> = {
    'dialog.delete.title': 'Delete Account',
    'dialog.delete.warning': 'This will remove all session data for this account. This action cannot be undone.',
    'button.cancel': 'Cancel',
    'button.delete': 'Delete',
  }
  return strings[key] ?? key
}

const meta = {
  title: 'Dialogs/DeleteAccountDialog',
  component: DeleteAccountDialog,
  args: {
    account: {
      accountId: '1',
      serviceId: 'x',
      displayName: 'John Doe',
      brandColor: '#000000',
      iconResource: 'X.svg',
      serviceDisplayName: 'X',
      isActive: true,
    },
    onClose: fn(),
    onConfirm: fn(),
    t,
  },
  parameters: {
    layout: 'centered',
  },
} satisfies Meta<typeof DeleteAccountDialog>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
