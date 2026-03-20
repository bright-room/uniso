import type { Database as SqlJsDatabase } from 'sql.js'
import type { ServicePlugin } from '../types/service-plugin'

export class ServicePluginRepository {
  constructor(private db: SqlJsDatabase) {}

  getAll(): ServicePlugin[] {
    const stmt = this.db.prepare('SELECT * FROM service_plugin ORDER BY sort_order')
    const results: ServicePlugin[] = []
    while (stmt.step()) {
      results.push(mapRow(stmt.getAsObject()))
    }
    stmt.free()
    return results
  }

  getById(serviceId: string): ServicePlugin | undefined {
    const stmt = this.db.prepare('SELECT * FROM service_plugin WHERE service_id = ?')
    stmt.bind([serviceId])
    if (!stmt.step()) {
      stmt.free()
      return undefined
    }
    const result = mapRow(stmt.getAsObject())
    stmt.free()
    return result
  }

  getAllDomainPatterns(): { serviceId: string; domain: string }[] {
    const plugins = this.getAll()
    const result: { serviceId: string; domain: string }[] = []
    for (const plugin of plugins) {
      for (const domain of plugin.domainPatterns) {
        result.push({ serviceId: plugin.serviceId, domain })
      }
    }
    return result
  }
}

function mapRow(row: Record<string, unknown>): ServicePlugin {
  return {
    serviceId: row.service_id as string,
    displayName: row.display_name as string,
    domainPatterns: JSON.parse(row.domain_patterns as string) as string[],
    brandColor: row.brand_color as string,
    iconResource: row.icon_resource as string,
    authType: row.auth_type as ServicePlugin['authType'],
    sortOrder: row.sort_order as number,
  }
}
