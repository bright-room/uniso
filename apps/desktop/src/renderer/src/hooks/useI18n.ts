import { useCallback, useEffect, useState } from 'react'

export function useI18n() {
  const [strings, setStrings] = useState<Record<string, string>>({})
  const [locale, setLocale] = useState('en')

  useEffect(() => {
    Promise.all([window.api.getI18nStrings(), window.api.getLocale()]).then(([s, l]) => {
      setStrings(s)
      setLocale(l)
    })

    const unsub = window.api.onLocaleChanged((data) => {
      setStrings(data.strings)
      setLocale(data.locale)
    })
    return unsub
  }, [])

  const t = useCallback((key: string) => strings[key] ?? key, [strings])

  return { t, locale, setLocale: (l: string) => window.api.setLocale(l) }
}
