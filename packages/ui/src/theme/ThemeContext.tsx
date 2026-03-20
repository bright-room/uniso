import { type ReactNode, createContext, useContext, useState } from 'react'
import { colors } from './tokens'

export type ThemeMode = 'light' | 'dark'
export type ThemeColors = (typeof colors)[ThemeMode]

interface ThemeContextValue {
  mode: ThemeMode
  c: ThemeColors
  setMode: (mode: ThemeMode) => void
  toggle: () => void
}

const ThemeContext = createContext<ThemeContextValue | null>(null)

interface ThemeProviderProps {
  initialMode?: ThemeMode
  children: ReactNode
}

export function ThemeProvider({ initialMode = 'dark', children }: ThemeProviderProps) {
  const [mode, setMode] = useState<ThemeMode>(initialMode)

  const value: ThemeContextValue = {
    mode,
    c: colors[mode],
    setMode,
    toggle: () => setMode((prev) => (prev === 'dark' ? 'light' : 'dark')),
  }

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
}

export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext)
  if (!ctx) {
    throw new Error('useTheme must be used within a ThemeProvider')
  }
  return ctx
}
