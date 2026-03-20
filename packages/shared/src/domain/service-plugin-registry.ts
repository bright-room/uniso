import type { ServicePluginRepository } from '../data/service-plugin-repository'
import type { ServicePlugin } from '../types'

export class ServicePluginRegistry {
  private plugins: ServicePlugin[] = []
  private domainMap: Map<string, string> = new Map()

  constructor(private repo: ServicePluginRepository) {}

  initialize(): void {
    this.plugins = this.repo.getAll()
    this.domainMap.clear()
    for (const plugin of this.plugins) {
      for (const domain of plugin.domainPatterns) {
        this.domainMap.set(domain, plugin.serviceId)
      }
    }
  }

  getAll(): ServicePlugin[] {
    return this.plugins
  }

  getById(serviceId: string): ServicePlugin | undefined {
    return this.plugins.find((p) => p.serviceId === serviceId)
  }

  findByDomain(hostname: string): string | undefined {
    // Direct match
    if (this.domainMap.has(hostname)) {
      return this.domainMap.get(hostname)
    }
    // Subdomain match (e.g., "www.x.com" -> "x.com")
    for (const [domain, serviceId] of this.domainMap) {
      if (hostname.endsWith(`.${domain}`)) {
        return serviceId
      }
    }
    return undefined
  }

  getDefaultUrl(serviceId: string): string | undefined {
    const plugin = this.getById(serviceId)
    if (!plugin || plugin.domainPatterns.length === 0) return undefined
    return `https://${plugin.domainPatterns[0]}`
  }
}
