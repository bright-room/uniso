import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { AddAccountButton } from './AddAccountButton'

const meta = {
  title: 'Sidebar/AddAccountButton',
  component: AddAccountButton,
  args: {
    onClick: fn(),
  },
  decorators: [
    (Story) => (
      <div style={{ padding: 20 }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof AddAccountButton>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
