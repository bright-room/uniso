import type { Preview } from '@storybook/react'
import { ThemeProvider } from '../src/theme/ThemeContext'
import type { ThemeMode } from '../src/theme/ThemeContext'
import '../src/theme/global.css'

const preview: Preview = {
  parameters: {
    backgrounds: {
      default: 'dark',
      values: [
        { name: 'dark', value: '#1a1a2e' },
        { name: 'light', value: '#ffffff' },
      ],
    },
  },
  decorators: [
    (Story, context) => {
      const backgroundName = context.globals?.backgrounds?.value
      const mode: ThemeMode = backgroundName === '#ffffff' ? 'light' : 'dark'

      return (
        <ThemeProvider initialMode={mode}>
          <Story />
        </ThemeProvider>
      )
    },
  ],
}

export default preview
