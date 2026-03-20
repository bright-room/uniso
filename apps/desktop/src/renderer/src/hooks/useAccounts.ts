import type { AccountListItem } from '@uniso/ui'
import { useEffect, useState } from 'react'

export function useAccounts() {
  const [accounts, setAccounts] = useState<AccountListItem[]>([])

  useEffect(() => {
    window.api.listAccounts().then(setAccounts)
    const unsub = window.api.onAccountsChanged(setAccounts)
    return unsub
  }, [])

  return accounts
}
