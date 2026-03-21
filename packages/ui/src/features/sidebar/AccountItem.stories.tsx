import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { AccountItem } from './AccountItem'

const meta = {
  title: 'Sidebar/AccountItem',
  component: AccountItem,
  args: {
    onSwitch: fn(),
    onContextMenu: fn(),
  },
  decorators: [
    (Story) => (
      <div style={{ padding: 20 }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof AccountItem>

export default meta
type Story = StoryObj<typeof meta>

export const Active: Story = {
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
  },
}

export const Inactive: Story = {
  args: {
    account: {
      accountId: '2',
      serviceId: 'instagram',
      displayName: 'john_doe',
      brandColor: '#E4405F',
      iconResource: 'Instagram.svg',
      serviceDisplayName: 'Instagram',
      isActive: false,
    },
  },
}

export const YouTube: Story = {
  args: {
    account: {
      accountId: '3',
      serviceId: 'youtube',
      displayName: 'My Channel',
      brandColor: '#FF0000',
      iconResource: 'Youtube.png',
      serviceDisplayName: 'YouTube',
      isActive: false,
    },
  },
}

export const NullDisplayName: Story = {
  name: 'No Display Name',
  args: {
    account: {
      accountId: '4',
      serviceId: 'bluesky',
      displayName: null,
      brandColor: '#0085FF',
      iconResource: 'Bluesky.png',
      serviceDisplayName: 'Bluesky',
      isActive: false,
    },
  },
}
