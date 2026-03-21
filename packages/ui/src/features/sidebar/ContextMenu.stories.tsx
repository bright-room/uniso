import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { ContextMenu } from './ContextMenu'

const meta = {
  title: 'Sidebar/ContextMenu',
  component: ContextMenu,
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
    x: 100,
    y: 100,
    onClose: fn(),
    onDelete: fn(),
    t: (key: string) => {
      const strings: Record<string, string> = {
        'dialog.delete.title': 'Delete Account',
      }
      return strings[key] ?? key
    },
  },
  parameters: {
    layout: 'fullscreen',
  },
} satisfies Meta<typeof ContextMenu>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
