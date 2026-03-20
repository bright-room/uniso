import { useEffect, useState } from 'react'
import type { AccountListItem } from '@uniso/ui'

export function useAccounts() {
  const [accounts, setAccounts] = useState<AccountListItem[]>([])

  useEffect(() => {
    window.api.listAccounts().then(setAccounts)
    const unsub = window.api.onAccountsChanged(setAccounts)
    return unsub
  }, [])

  return accounts
}
