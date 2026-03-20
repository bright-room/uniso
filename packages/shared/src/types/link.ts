export type LinkClassification =
  | { type: 'same-domain' }
  | { type: 'external'; url: string }
  | { type: 'internal-no-account'; url: string; serviceId: string }
  | { type: 'internal-single-account'; url: string; accountId: string }
  | { type: 'internal-multi-account'; url: string; serviceId: string; accountIds: string[] }
