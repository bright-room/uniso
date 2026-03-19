package net.brightroom.uniso.domain.plugin

import net.brightroom.uniso.data.db.UnisoDatabase

class ServicePluginRegistry(
    private val database: UnisoDatabase,
) {
    fun getAll(): List<ServicePlugin> =
        database.servicePluginQueries
            .selectAll()
            .executeAsList()
            .map { it.toDomain() }

    fun getById(serviceId: String): ServicePlugin? =
        database.servicePluginQueries
            .selectById(serviceId)
            .executeAsOneOrNull()
            ?.toDomain()

    fun findByDomain(domain: String): ServicePlugin? {
        val normalizedDomain = domain.removePrefix("www.").lowercase()
        return getAll().firstOrNull { plugin ->
            plugin.domainPatterns.any { pattern ->
                normalizedDomain == pattern || normalizedDomain.endsWith(".$pattern")
            }
        }
    }

    private fun net.brightroom.uniso.data.db.Service_plugin.toDomain(): ServicePlugin =
        ServicePlugin(
            serviceId = service_id,
            displayName = display_name,
            domainPatterns = parseDomainPatterns(domain_patterns),
            brandColor = parseBrandColor(brand_color),
            iconResource = icon_resource,
            authType = AuthType.fromString(auth_type),
            sortOrder = sort_order.toInt(),
        )

    companion object {
        internal fun parseDomainPatterns(json: String): List<String> =
            json
                .removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }

        internal fun parseBrandColor(hex: String): Long {
            val colorHex = hex.removePrefix("#")
            return (0xFF000000 or colorHex.toLong(16))
        }
    }
}
