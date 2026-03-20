import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { TutorialScreen } from './TutorialScreen'

const t = (key: string) => {
  const strings: Record<string, string> = {
    'tutorial.welcome.title': 'Welcome to Uniso',
    'tutorial.welcome.description': 'Manage all your social media accounts in one place.',
    'tutorial.add_account.title': 'Add an Account',
    'tutorial.add_account.description': 'Click the + button in the sidebar to add your first account.',
    'tutorial.switch_account.title': 'Switch Accounts',
    'tutorial.switch_account.description':
      'Click an account in the sidebar or use Ctrl+Tab to quickly switch between accounts.',
    'tutorial.complete.title': 'You\'re All Set!',
    'tutorial.complete.description': 'Start by adding your first account. Enjoy Uniso!',
    'tutorial.back': 'Back',
    'tutorial.next': 'Next',
    'tutorial.start': 'Get Started',
  }
  return strings[key] ?? key
}

const meta = {
  title: 'Onboarding/TutorialScreen',
  component: TutorialScreen,
  args: {
    onComplete: fn(),
    t,
  },
  parameters: {
    layout: 'fullscreen',
  },
} satisfies Meta<typeof TutorialScreen>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
