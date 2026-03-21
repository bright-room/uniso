import type { Meta, StoryObj } from '@storybook/react'
import { ServiceIcon } from './ServiceIcon'
import { serviceIconFiles } from '../theme/tokens'

const meta: Meta<typeof ServiceIcon> = {
  title: 'Primitives/ServiceIcon',
  component: ServiceIcon,
  argTypes: {
    size: { control: { type: 'range', min: 16, max: 128, step: 2 } },
    borderRadius: { control: { type: 'range', min: 0, max: 32, step: 1 } },
    backgroundColor: { control: 'color' },
  },
}

export default meta
type Story = StoryObj<typeof ServiceIcon>

const services = [
  { id: 'x', name: 'X', backgroundColor: '#000000' },
  { id: 'instagram', name: 'Instagram', backgroundColor: '#ffffff' },
  { id: 'facebook', name: 'Facebook', backgroundColor: '#ffffff' },
  { id: 'youtube', name: 'YouTube', backgroundColor: '#ffffff' },
  { id: 'bluesky', name: 'Bluesky', backgroundColor: '#ffffff' },
  { id: 'twitch', name: 'Twitch', backgroundColor: '#ffffff' },
]

export const Default: Story = {
  args: {
    src: `./${serviceIconFiles.x}`,
    size: 48,
    backgroundColor: '#000000',
    borderRadius: 10,
  },
}

export const AllServices: Story = {
  render: () => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {services.map((service) => (
        <div key={service.id} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <ServiceIcon
            src={`./${serviceIconFiles[service.id]}`}
            size={48}
            backgroundColor={service.backgroundColor}
            borderRadius={10}
          />
          <span>{service.name}</span>
          <span style={{ color: '#888', fontSize: 12 }}>
            ({serviceIconFiles[service.id]})
          </span>
        </div>
      ))}
    </div>
  ),
}

export const Sizes: Story = {
  args: {
    backgroundColor: "#000000"
  },

  render: () => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {[18, 24, 36, 48, 64].map((size) => (
        <div key={size} style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <span style={{ width: 40, fontSize: 12, color: '#888' }}>{size}px</span>
          {services.map((service) => (
            <ServiceIcon
              key={service.id}
              src={`./${serviceIconFiles[service.id]}`}
              size={size}
              backgroundColor={service.backgroundColor}
              borderRadius={size * 0.2}
            />
          ))}
        </div>
      ))}
    </div>
  )
}

export const WhiteBackground: Story = {
  render: () => (
    <div style={{ display: 'flex', gap: 12 }}>
      {services.map((service) => (
        <ServiceIcon
          key={service.id}
          src={`./${serviceIconFiles[service.id]}`}
          size={48}
          backgroundColor="#ffffff"
          borderRadius={10}
        />
      ))}
    </div>
  ),
}
