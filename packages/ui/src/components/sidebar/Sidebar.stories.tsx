import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import type { AccountListItem } from '../../types'
import { Sidebar } from './Sidebar'

const mockAccounts: AccountListItem[] = [
  {
    accountId: '1',
    serviceId: 'x',
    displayName: 'Alice',
    brandColor: '#000000',
    iconResource: '𝕏',
    serviceDisplayName: 'X',
    isActive: true,
  },
  {
    accountId: '2',
    serviceId: 'x',
    displayName: 'Bob',
    brandColor: '#000000',
    iconResource: '𝕏',
    serviceDisplayName: 'X',
    isActive: false,
  },
  {
    accountId: '3',
    serviceId: 'instagram',
    displayName: 'alice_photo',
    brandColor: '#E4405F',
    iconResource: '📷',
    serviceDisplayName: 'Instagram',
    isActive: false,
  },
  {
    accountId: '4',
    serviceId: 'youtube',
    displayName: 'Alice Channel',
    brandColor: '#FF0000',
    iconResource: '▶',
    serviceDisplayName: 'YouTube',
    isActive: false,
  },
]

const t = (key: string) => key

const meta = {
  title: 'Sidebar/Sidebar',
  component: Sidebar,
  args: {
    accounts: mockAccounts,
    onSwitch: fn(),
    onAddClick: fn(),
    onSettingsClick: fn(),
    onContextMenu: fn(),
    t,
  },
  parameters: {
    layout: 'fullscreen',
  },
  decorators: [
    (Story) => (
      <div style={{ height: '100vh' }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof Sidebar>

export default meta
type Story = StoryObj<typeof meta>

export const WithAccounts: Story = {}

export const Empty: Story = {
  args: {
    accounts: [],
  },
}

export const SingleAccount: Story = {
  args: {
    accounts: [mockAccounts[0]],
  },
}

export const ManyAccounts: Story = {
  args: {
    accounts: [
      ...mockAccounts,
      {
        accountId: '5',
        serviceId: 'bluesky',
        displayName: 'alice.bsky.social',
        brandColor: '#0085FF',
        iconResource: '🦋',
        serviceDisplayName: 'Bluesky',
        isActive: false,
      },
      {
        accountId: '6',
        serviceId: 'facebook',
        displayName: 'Alice Smith',
        brandColor: '#1877F2',
        iconResource: 'f',
        serviceDisplayName: 'Facebook',
        isActive: false,
      },
      {
        accountId: '7',
        serviceId: 'twitch',
        displayName: 'alice_streams',
        brandColor: '#9146FF',
        iconResource: '📺',
        serviceDisplayName: 'Twitch',
        isActive: false,
      },
    ],
  },
}
