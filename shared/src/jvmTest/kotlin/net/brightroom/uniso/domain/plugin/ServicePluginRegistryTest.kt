package net.brightroom.uniso.domain.plugin

import net.brightroom.uniso.data.repository.createTestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ServicePluginRegistryTest {
    private lateinit var registry: ServicePluginRegistry

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        registry = ServicePluginRegistry(database)
    }

    @Test
    fun getAllReturnsPluginsSortedBySortOrder() {
        val plugins = registry.getAll()

        // createTestDatabase inserts x (sort=1) and instagram (sort=2)
        assertEquals(2, plugins.size)
        assertEquals("x", plugins[0].serviceId)
        assertEquals("instagram", plugins[1].serviceId)
    }

    @Test
    fun getByIdReturnsCorrectPlugin() {
        val plugin = registry.getById("x")

        assertNotNull(plugin)
        assertEquals("x", plugin.serviceId)
        assertEquals("X", plugin.displayName)
        assertEquals(listOf("x.com", "twitter.com"), plugin.domainPatterns)
        assertEquals(AuthType.COOKIE, plugin.authType)
    }

    @Test
    fun getByIdReturnsNullForUnknownId() {
        assertNull(registry.getById("unknown"))
    }

    @Test
    fun findByDomainMatchesExactDomain() {
        val plugin = registry.findByDomain("x.com")

        assertNotNull(plugin)
        assertEquals("x", plugin.serviceId)
    }

    @Test
    fun findByDomainMatchesAlternateDomain() {
        val plugin = registry.findByDomain("twitter.com")

        assertNotNull(plugin)
        assertEquals("x", plugin.serviceId)
    }

    @Test
    fun findByDomainStripsWwwPrefix() {
        val plugin = registry.findByDomain("www.instagram.com")

        assertNotNull(plugin)
        assertEquals("instagram", plugin.serviceId)
    }

    @Test
    fun findByDomainMatchesSubdomain() {
        val plugin = registry.findByDomain("mobile.x.com")

        assertNotNull(plugin)
        assertEquals("x", plugin.serviceId)
    }

    @Test
    fun findByDomainReturnsNullForUnknownDomain() {
        assertNull(registry.findByDomain("example.com"))
    }

    @Test
    fun parseDomainPatternsHandlesJsonArray() {
        val result = ServicePluginRegistry.parseDomainPatterns("""["x.com","twitter.com"]""")
        assertEquals(listOf("x.com", "twitter.com"), result)
    }

    @Test
    fun parseBrandColorConvertsHexToArgb() {
        val color = ServicePluginRegistry.parseBrandColor("#E1306C")
        assertEquals(0xFFE1306C, color)
    }

    @Test
    fun pluginBrandColorIsParsedCorrectly() {
        val plugin = registry.getById("x")
        assertNotNull(plugin)
        assertEquals(0xFF000000, plugin.brandColor)
    }
}
