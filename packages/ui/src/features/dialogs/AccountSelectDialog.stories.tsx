import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import type { AccountListItem } from '../../types'
import { AccountSelectDialog } from './AccountSelectDialog'

const mockAccounts: AccountListItem[] = [
  {
    accountId: '1',
    serviceId: 'x',
    displayName: 'Alice',
    brandColor: '#000000',
    iconResource: 'X.svg',
    serviceDisplayName: 'X',
    isActive: false,
  },
  {
    accountId: '2',
    serviceId: 'x',
    displayName: 'Bob',
    brandColor: '#000000',
    iconResource: 'X.svg',
    serviceDisplayName: 'X',
    isActive: false,
  },
]

const t = (key: string) => {
  const strings: Record<string, string> = {
    'dialog.account_select.title': 'Select Account',
    'dialog.account_select.open_external': 'Open in Browser',
  }
  return strings[key] ?? key
}

const meta = {
  title: 'Dialogs/AccountSelectDialog',
  component: AccountSelectDialog,
  args: {
    accounts: mockAccounts,
    url: 'https://x.com/some-post',
    onSelect: fn(),
    onClose: fn(),
    t,
  },
  parameters: {
    layout: 'centered',
  },
} satisfies Meta<typeof AccountSelectDialog>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}

export const SingleAccount: Story = {
  args: {
    accounts: [mockAccounts[0]],
  },
}
