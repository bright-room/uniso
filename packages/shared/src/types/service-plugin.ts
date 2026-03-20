export type AuthType = 'cookie' | 'oauth'

export interface ServicePlugin {
  serviceId: string
  displayName: string
  domainPatterns: string[]
  brandColor: string
  iconResource: string
  authType: AuthType
  sortOrder: number
}
