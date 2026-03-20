import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { SettingsScreen } from './SettingsScreen'

const t = (key: string) => {
  const strings: Record<string, string> = {
    'settings.title': 'Settings',
    'settings.general': 'General',
    'settings.language': 'Language',
    'settings.show_tutorial': 'Show Tutorial',
    'settings.privacy': 'Privacy',
    'settings.telemetry': 'Usage Analytics',
    'settings.telemetry.description': 'Help improve Uniso by sending anonymous usage data.',
    'settings.keyboard_shortcuts': 'Keyboard Shortcuts',
    'settings.shortcut.next_account': 'Next Account',
    'settings.shortcut.prev_account': 'Previous Account',
    'settings.shortcut.reload': 'Reload',
    'settings.shortcut.force_reload': 'Force Reload',
    'settings.shortcut.add_account': 'Add Account',
    'settings.shortcut.settings': 'Settings',
    'settings.app_info': 'App Info',
    'settings.version': 'Version',
    'button.close': 'Close',
  }
  return strings[key] ?? key
}

const meta = {
  title: 'Settings/SettingsScreen',
  component: SettingsScreen,
  args: {
    locale: 'en',
    telemetryEnabled: false,
    onLocaleChange: fn(),
    onTelemetryChange: fn(),
    onClose: fn(),
    onShowTutorial: fn(),
    t,
  },
  parameters: {
    layout: 'fullscreen',
  },
  decorators: [
    (Story) => (
      <div style={{ position: 'relative', height: '100vh', marginLeft: 80 }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof SettingsScreen>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}

export const Japanese: Story = {
  args: {
    locale: 'ja',
  },
}

export const TelemetryEnabled: Story = {
  args: {
    telemetryEnabled: true,
  },
}
