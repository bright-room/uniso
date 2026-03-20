import type { Meta, StoryObj } from '@storybook/react'
import { fn } from 'storybook/test'
import { useTheme } from '../theme/ThemeContext'
import { d } from '../theme/tokens'
import { DialogOverlay } from './DialogOverlay'

function SampleDialogContent() {
  const { c } = useTheme()
  return (
    <div
      style={{
        background: c.bgPrimary,
        borderRadius: d.dialogBorderRadius,
        padding: d.dialogPadding,
        width: d.dialogWidth,
        border: `0.5px solid ${c.borderSecondary}`,
        color: c.textPrimary,
        fontSize: d.fontSize.md,
      }}
    >
      Dialog content goes here
    </div>
  )
}

const meta = {
  title: 'Dialogs/DialogOverlay',
  component: DialogOverlay,
  args: {
    onClose: fn(),
    children: <SampleDialogContent />,
  },
  parameters: {
    layout: 'fullscreen',
  },
} satisfies Meta<typeof DialogOverlay>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
