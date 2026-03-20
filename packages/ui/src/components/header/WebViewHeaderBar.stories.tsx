import type { Meta, StoryObj } from '@storybook/react'
import { WebViewHeaderBar } from './WebViewHeaderBar'

const meta = {
  title: 'Header/WebViewHeaderBar',
  component: WebViewHeaderBar,
  parameters: {
    layout: 'fullscreen',
  },
  decorators: [
    (Story) => (
      <div style={{ position: 'relative', height: 40, marginLeft: 80 }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof WebViewHeaderBar>

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
    currentUrl: 'https://x.com/home',
  },
}

export const LongUrl: Story = {
  args: {
    account: {
      accountId: '2',
      serviceId: 'youtube',
      displayName: 'My Channel',
      brandColor: '#FF0000',
      iconResource: '▶',
      serviceDisplayName: 'YouTube',
      isActive: true,
    },
    currentUrl:
      'https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLrAXtmErZgOeiKm4sgNOknGvNjby9efdf&index=1',
  },
}

export const NoAccount: Story = {
  args: {
    account: null,
    currentUrl: null,
  },
}
