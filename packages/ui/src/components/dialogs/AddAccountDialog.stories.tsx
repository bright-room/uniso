import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import type { ServicePlugin } from '../../types'
import { AddAccountDialog } from './AddAccountDialog'

const mockServices: ServicePlugin[] = [
  {
    serviceId: 'x',
    displayName: 'X',
    domainPatterns: ['x.com', 'twitter.com'],
    brandColor: '#000000',
    iconResource: '𝕏',
    authType: 'cookie',
    sortOrder: 1,
  },
  {
    serviceId: 'instagram',
    displayName: 'Instagram',
    domainPatterns: ['instagram.com'],
    brandColor: '#E4405F',
    iconResource: '📷',
    authType: 'cookie',
    sortOrder: 2,
  },
  {
    serviceId: 'youtube',
    displayName: 'YouTube',
    domainPatterns: ['youtube.com'],
    brandColor: '#FF0000',
    iconResource: '▶',
    authType: 'cookie',
    sortOrder: 3,
  },
  {
    serviceId: 'bluesky',
    displayName: 'Bluesky',
    domainPatterns: ['bsky.app'],
    brandColor: '#0085FF',
    iconResource: '🦋',
    authType: 'cookie',
    sortOrder: 4,
  },
  {
    serviceId: 'facebook',
    displayName: 'Facebook',
    domainPatterns: ['facebook.com'],
    brandColor: '#1877F2',
    iconResource: 'f',
    authType: 'cookie',
    sortOrder: 5,
  },
  {
    serviceId: 'twitch',
    displayName: 'Twitch',
    domainPatterns: ['twitch.tv'],
    brandColor: '#9146FF',
    iconResource: '📺',
    authType: 'cookie',
    sortOrder: 6,
  },
]

const meta = {
  title: 'Dialogs/AddAccountDialog',
  component: AddAccountDialog,
  args: {
    services: mockServices,
    onClose: fn(),
    onAdd: fn(),
    t: (key: string) => {
      const strings: Record<string, string> = {
        'dialog.add_account.title': 'Add Account',
        'dialog.add_account.subtitle': 'Select a service to add',
      }
      return strings[key] ?? key
    },
  },
  parameters: {
    layout: 'centered',
  },
} satisfies Meta<typeof AddAccountDialog>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
