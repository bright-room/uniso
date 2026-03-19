package net.brightroom.uniso.data.repository

import net.brightroom.uniso.data.model.LocalUser
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlSettingsRepositoryTest {
    private lateinit var repository: SqlSettingsRepository

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        repository = SqlSettingsRepository(database)
    }

    @Test
    fun getStringReturnsNullForMissingKey() {
        assertNull(repository.getString("nonexistent"))
    }

    @Test
    fun setAndGetString() {
        repository.setString("locale", "ja")
        assertEquals("ja", repository.getString("locale"))
    }

    @Test
    fun setStringOverwritesExisting() {
        repository.setString("locale", "ja")
        repository.setString("locale", "en")
        assertEquals("en", repository.getString("locale"))
    }

    @Test
    fun getBooleanReturnsNullForMissingKey() {
        assertNull(repository.getBoolean("nonexistent"))
    }

    @Test
    fun setAndGetBoolean() {
        repository.setBoolean("telemetry_enabled", true)
        assertTrue(repository.getBoolean("telemetry_enabled")!!)

        repository.setBoolean("telemetry_enabled", false)
        assertFalse(repository.getBoolean("telemetry_enabled")!!)
    }

    @Test
    fun insertAndGetLocalUser() {
        val user = LocalUser(id = "user-123", createdAt = "2026-01-01T00:00:00Z")
        repository.insertLocalUser(user)

        val loaded = repository.getLocalUser()
        assertNotNull(loaded)
        assertEquals("user-123", loaded.id)
        assertEquals("2026-01-01T00:00:00Z", loaded.createdAt)
    }

    @Test
    fun getLocalUserReturnsNullWhenEmpty() {
        assertNull(repository.getLocalUser())
    }
}
