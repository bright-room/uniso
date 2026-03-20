export type WebViewStatus = 'active' | 'background' | 'destroyed'

export interface AccountState {
  accountId: string
  lastUrl: string | null
  scrollPositionY: number
  webviewStatus: WebViewStatus
  lastAccessedAt: string
}

export interface AccountStateWithInfo extends AccountState {
  serviceId: string
  displayName: string | null
}
