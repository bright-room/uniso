package net.brightroom.uniso.domain.identity

import net.brightroom.uniso.data.repository.SqlSettingsRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IdentityManagerTest {
    private lateinit var identityManager: IdentityManager
    private lateinit var settingsRepository: SqlSettingsRepository

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        settingsRepository = SqlSettingsRepository(database)
        identityManager = IdentityManager(settingsRepository)
    }

    // IM-001: 初回ID生成
    @Test
    fun generatesNewUuidWhenNoLocalUserExists() {
        val id = identityManager.getOrCreateLocalUserId()

        assertNotNull(id)
        assertTrue(id.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
        assertNotNull(settingsRepository.getLocalUser())
    }

    // IM-002: 既存ID返却
    @Test
    fun returnsExistingIdWhenLocalUserExists() {
        val firstId = identityManager.getOrCreateLocalUserId()
        val secondId = identityManager.getOrCreateLocalUserId()

        assertEquals(firstId, secondId)
    }

    // IM-003: 冪等性
    @Test
    fun multipleCallsReturnSameId() {
        val ids = (1..5).map { identityManager.getOrCreateLocalUserId() }

        assertTrue(ids.all { it == ids[0] })
    }
}
