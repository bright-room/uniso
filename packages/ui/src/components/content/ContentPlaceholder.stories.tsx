import type { Meta, StoryObj } from '@storybook/react'
import { ContentPlaceholder } from './ContentPlaceholder'

const t = (key: string) => {
  const strings: Record<string, string> = {
    'content.no_account': 'No account selected',
  }
  return strings[key] ?? key
}

const meta = {
  title: 'Content/ContentPlaceholder',
  component: ContentPlaceholder,
  args: { t },
  parameters: {
    layout: 'fullscreen',
  },
} satisfies Meta<typeof ContentPlaceholder>

export default meta
type Story = StoryObj<typeof meta>

export const WithAccount: Story = {
  args: {
    account: {
      accountId: '1',
      serviceId: 'x',
      displayName: 'John Doe',
      brandColor: '#000000',
      iconResource: '𝕏',
      serviceDisplayName: 'X',
      isActive: true,
    },
  },
}

export const NoAccount: Story = {
  args: {
    account: null,
  },
}
