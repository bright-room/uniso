package net.brightroom.uniso.data.repository

import net.brightroom.uniso.data.model.Account
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccountRepositoryTest {
    private lateinit var repository: AccountRepository

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        repository = AccountRepository(database)
    }

    @Test
    fun insertAndGetAll() {
        val account = createAccount("acc-1", "x")
        repository.insert(account)

        val accounts = repository.getAll()
        assertEquals(1, accounts.size)
        assertEquals("acc-1", accounts[0].accountId)
        assertEquals("x", accounts[0].serviceId)
    }

    @Test
    fun deleteRemovesAccount() {
        repository.insert(createAccount("acc-1", "x"))
        repository.insert(createAccount("acc-2", "instagram"))
        assertEquals(2, repository.getCount())

        repository.delete("acc-1")
        assertEquals(1, repository.getCount())
        assertEquals("acc-2", repository.getAll()[0].accountId)
    }

    @Test
    fun getAllReturnsSortedBySortOrderThenCreatedAt() {
        repository.insert(createAccount("acc-1", "x", sortOrder = 2, createdAt = "2026-01-01"))
        repository.insert(createAccount("acc-2", "x", sortOrder = 1, createdAt = "2026-01-02"))
        repository.insert(createAccount("acc-3", "x", sortOrder = 1, createdAt = "2026-01-01"))

        val accounts = repository.getAll()
        assertEquals("acc-3", accounts[0].accountId)
        assertEquals("acc-2", accounts[1].accountId)
        assertEquals("acc-1", accounts[2].accountId)
    }

    @Test
    fun getByServiceIdFiltersCorrectly() {
        repository.insert(createAccount("acc-1", "x"))
        repository.insert(createAccount("acc-2", "instagram"))
        repository.insert(createAccount("acc-3", "x"))

        val xAccounts = repository.getByServiceId("x")
        assertEquals(2, xAccounts.size)
        assertTrue(xAccounts.all { it.serviceId == "x" })

        val instaAccounts = repository.getByServiceId("instagram")
        assertEquals(1, instaAccounts.size)
    }

    @Test
    fun getCountReturnsCorrectCount() {
        assertEquals(0, repository.getCount())
        repository.insert(createAccount("acc-1", "x"))
        assertEquals(1, repository.getCount())
        repository.insert(createAccount("acc-2", "instagram"))
        assertEquals(2, repository.getCount())
    }

    @Test
    fun updateDisplayName() {
        repository.insert(createAccount("acc-1", "x", displayName = "Old Name"))
        repository.updateDisplayName("acc-1", "New Name")

        val account = repository.getAll()[0]
        assertEquals("New Name", account.displayName)
    }

    private fun createAccount(
        id: String,
        serviceId: String,
        displayName: String? = null,
        sortOrder: Int = 0,
        createdAt: String = "2026-01-01T00:00:00Z",
    ) = Account(
        accountId = id,
        serviceId = serviceId,
        displayName = displayName,
        avatarUrl = null,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )
}
