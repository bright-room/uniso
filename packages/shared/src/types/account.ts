export interface Account {
  accountId: string
  serviceId: string
  displayName: string | null
  avatarUrl: string | null
  sortOrder: number
  createdAt: string
}

export interface AccountWithService extends Account {
  serviceDisplayName: string
  brandColor: string
  iconResource: string
}
