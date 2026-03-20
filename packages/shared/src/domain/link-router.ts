import type { AccountRepository } from '../data/account-repository'
import type { LinkClassification } from '../types'
import type { ServicePluginRegistry } from './service-plugin-registry'

export class LinkRouter {
  constructor(
    private registry: ServicePluginRegistry,
    private accountRepo: AccountRepository,
  ) {}

  classifyLink(url: string, currentServiceId: string): LinkClassification {
    let hostname: string
    try {
      hostname = new URL(url).hostname
    } catch {
      return { type: 'external', url }
    }

    // Check if the target domain belongs to a known service
    const targetServiceId = this.registry.findByDomain(hostname)

    // Not a known service -> external
    if (!targetServiceId) {
      return { type: 'external', url }
    }

    // Same service as current -> same-domain navigation
    if (targetServiceId === currentServiceId) {
      return { type: 'same-domain' }
    }

    // Different known service -> check accounts
    const accounts = this.accountRepo.getByServiceId(targetServiceId)

    if (accounts.length === 0) {
      return { type: 'internal-no-account', url, serviceId: targetServiceId }
    }

    if (accounts.length === 1) {
      return { type: 'internal-single-account', url, accountId: accounts[0].accountId }
    }

    return {
      type: 'internal-multi-account',
      url,
      serviceId: targetServiceId,
      accountIds: accounts.map((a) => a.accountId),
    }
  }
}
