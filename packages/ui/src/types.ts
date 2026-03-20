export interface AccountListItem {
  accountId: string
  serviceId: string
  displayName: string | null
  brandColor: string
  iconResource: string
  serviceDisplayName: string
  isActive: boolean
}

export interface ServicePlugin {
  serviceId: string
  displayName: string
  domainPatterns: string[]
  brandColor: string
  iconResource: string
  authType: string
  sortOrder: number
}
