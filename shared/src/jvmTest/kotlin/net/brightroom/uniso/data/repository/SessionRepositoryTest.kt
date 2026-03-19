package net.brightroom.uniso.data.repository

import net.brightroom.uniso.data.model.Account
import net.brightroom.uniso.data.model.AccountState
import net.brightroom.uniso.data.model.AppState
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionRepositoryTest {
    private lateinit var sessionRepo: SessionRepository
    private lateinit var accountRepo: AccountRepository

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        sessionRepo = SessionRepository(database)
        accountRepo = AccountRepository(database)
    }

    @Test
    fun saveAndGetAppState() {
        val state =
            AppState(
                activeAccountId = null,
                cleanShutdown = false,
                lastSavedAt = "2026-01-01T00:00:00Z",
            )
        sessionRepo.saveAppState(state)

        val loaded = sessionRepo.getAppState()
        assertNotNull(loaded)
        assertNull(loaded.activeAccountId)
        assertFalse(loaded.cleanShutdown)
        assertEquals("2026-01-01T00:00:00Z", loaded.lastSavedAt)
    }

    @Test
    fun saveAppStateWithActiveAccount() {
        accountRepo.insert(
            Account("acc-1", "x", null, null, 0, "2026-01-01T00:00:00Z"),
        )
        val state =
            AppState(
                activeAccountId = "acc-1",
                cleanShutdown = true,
                lastSavedAt = "2026-01-01T12:00:00Z",
            )
        sessionRepo.saveAppState(state)

        val loaded = sessionRepo.getAppState()
        assertNotNull(loaded)
        assertEquals("acc-1", loaded.activeAccountId)
        assertTrue(loaded.cleanShutdown)
    }

    @Test
    fun upsertAccountState() {
        accountRepo.insert(
            Account("acc-1", "x", null, null, 0, "2026-01-01T00:00:00Z"),
        )

        val state =
            AccountState(
                accountId = "acc-1",
                lastUrl = "https://x.com/home",
                scrollPositionY = 100,
                webviewStatus = "active",
                lastAccessedAt = "2026-01-01T00:00:00Z",
            )
        sessionRepo.saveAccountState(state)

        val loaded = sessionRepo.getAccountState("acc-1")
        assertNotNull(loaded)
        assertEquals("https://x.com/home", loaded.lastUrl)
        assertEquals(100, loaded.scrollPositionY)
        assertEquals("active", loaded.webviewStatus)
    }

    @Test
    fun upsertAccountStateUpdatesExisting() {
        accountRepo.insert(
            Account("acc-1", "x", null, null, 0, "2026-01-01T00:00:00Z"),
        )

        sessionRepo.saveAccountState(
            AccountState("acc-1", "https://x.com/home", 100, "active", "2026-01-01T00:00:00Z"),
        )
        sessionRepo.saveAccountState(
            AccountState("acc-1", "https://x.com/explore", 200, "background", "2026-01-01T01:00:00Z"),
        )

        val loaded = sessionRepo.getAccountState("acc-1")
        assertNotNull(loaded)
        assertEquals("https://x.com/explore", loaded.lastUrl)
        assertEquals(200, loaded.scrollPositionY)
        assertEquals("background", loaded.webviewStatus)
    }

    @Test
    fun getAccountStateReturnsNullForMissing() {
        assertNull(sessionRepo.getAccountState("nonexistent"))
    }

    @Test
    fun getAllAccountStates() {
        accountRepo.insert(Account("acc-1", "x", null, null, 0, "2026-01-01T00:00:00Z"))
        accountRepo.insert(Account("acc-2", "instagram", null, null, 0, "2026-01-01T00:00:00Z"))

        sessionRepo.saveAccountState(
            AccountState("acc-1", "https://x.com", 0, "active", "2026-01-01T00:00:00Z"),
        )
        sessionRepo.saveAccountState(
            AccountState("acc-2", "https://instagram.com", 0, "destroyed", "2026-01-01T00:00:00Z"),
        )

        val states = sessionRepo.getAllAccountStates()
        assertEquals(2, states.size)
    }

    @Test
    fun deleteAccountState() {
        accountRepo.insert(Account("acc-1", "x", null, null, 0, "2026-01-01T00:00:00Z"))
        sessionRepo.saveAccountState(
            AccountState("acc-1", "https://x.com", 0, "active", "2026-01-01T00:00:00Z"),
        )

        sessionRepo.deleteAccountState("acc-1")
        assertNull(sessionRepo.getAccountState("acc-1"))
    }
}
