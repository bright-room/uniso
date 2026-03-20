import { useEffect, useState } from 'react'

export function useCurrentUrl() {
  const [url, setUrl] = useState<string | null>(null)

  useEffect(() => {
    window.api.getAccountUrl().then(setUrl)
    const unsub = window.api.onUrlChanged((data) => {
      setUrl(data.url)
    })
    return unsub
  }, [])

  return url
}
