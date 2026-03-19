package net.brightroom.uniso.domain.account

import net.brightroom.uniso.data.model.Account
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import net.brightroom.uniso.domain.plan.FreePlanProvider
import net.brightroom.uniso.domain.plan.PlanInfo
import net.brightroom.uniso.domain.plan.PlanProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountManagerTest {
    private lateinit var accountManager: AccountManager
    private lateinit var accountRepository: AccountRepository
    private lateinit var sessionRepository: SessionRepository

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        accountRepository = AccountRepository(database)
        sessionRepository = SessionRepository(database)
        accountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
                planProvider = FreePlanProvider(),
            )
    }

    // AM-001: アカウント追加_正常系
    @Test
    fun addAccountReturnsSuccessAndPersists() {
        val result = accountManager.addAccount("x")

        assertTrue(result.isSuccess)
        val account = result.getOrThrow()
        assertEquals("x", account.serviceId)
        assertNotNull(account.accountId)
        assertEquals(1, accountManager.accounts.value.size)
        assertEquals(1, accountRepository.getCount())
    }

    // AM-002: アカウント追加_PlanProvider許可
    @Test
    fun addAccountWithFreePlanProviderAlwaysSucceeds() {
        repeat(5) {
            val result = accountManager.addAccount("x")
            assertTrue(result.isSuccess)
        }
        assertEquals(5, accountManager.accounts.value.size)
    }

    // AM-002 variant: PlanProvider拒否
    @Test
    fun addAccountFailsWhenPlanLimitReached() {
        val limitedPlan =
            object : PlanProvider {
                override fun checkAccountLimit(currentCount: Int): Boolean = currentCount < 2

                override fun getCurrentPlan(): PlanInfo = PlanInfo("limited", "Limited", false)

                override fun onLimitReached() {}
            }

        val database = createTestDatabase()
        val manager =
            AccountManager(
                accountRepository = AccountRepository(database),
                sessionRepository = SessionRepository(database),
                planProvider = limitedPlan,
            )

        assertTrue(manager.addAccount("x").isSuccess)
        assertTrue(manager.addAccount("x").isSuccess)
        val result = manager.addAccount("x")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AccountLimitReachedException)
    }

    // AM-003: アカウント削除_正常系
    @Test
    fun removeAccountDeletesFromDbAndSessionData() {
        val account = accountManager.addAccount("x").getOrThrow()
        assertEquals(1, accountRepository.getCount())

        val result = accountManager.removeAccount(account.accountId)
        assertTrue(result.isSuccess)
        assertEquals(0, accountRepository.getCount())
        assertEquals(0, accountManager.accounts.value.size)
    }

    // AM-004: アカウント削除_アクティブアカウント
    @Test
    fun removeActiveAccountSwitchesToFirst() {
        val accountA = accountManager.addAccount("x").getOrThrow()
        val accountB = accountManager.addAccount("instagram").getOrThrow()
        accountManager.setActiveAccount(accountA.accountId)

        accountManager.removeAccount(accountA.accountId)

        assertEquals(accountB.accountId, accountManager.activeAccountId.value)
    }

    // AM-005: アクティブアカウント切替
    @Test
    fun setActiveAccountChangesActiveId() {
        val accountA = accountManager.addAccount("x").getOrThrow()
        val accountB = accountManager.addAccount("instagram").getOrThrow()

        accountManager.setActiveAccount(accountB.accountId)
        assertEquals(accountB.accountId, accountManager.activeAccountId.value)

        accountManager.setActiveAccount(accountA.accountId)
        assertEquals(accountA.accountId, accountManager.activeAccountId.value)
    }

    // AM-006: サービスごとの取得
    @Test
    fun getAccountsByServiceFiltersCorrectly() {
        accountManager.addAccount("x")
        accountManager.addAccount("x")
        accountManager.addAccount("instagram")

        val xAccounts = accountManager.getAccountsByService("x")
        assertEquals(2, xAccounts.size)
        assertTrue(xAccounts.all { it.serviceId == "x" })

        val instaAccounts = accountManager.getAccountsByService("instagram")
        assertEquals(1, instaAccounts.size)
    }

    // AM-007: 0件状態での削除
    @Test
    fun removeNonExistentAccountCompletesWithoutError() {
        val result = accountManager.removeAccount("non-existent-id")
        assertTrue(result.isSuccess)
    }

    @Test
    fun firstAddedAccountBecomesActive() {
        val account = accountManager.addAccount("x").getOrThrow()
        assertEquals(account.accountId, accountManager.activeAccountId.value)
    }

    @Test
    fun setActiveAccountIgnoresInvalidId() {
        accountManager.addAccount("x")
        val originalActive = accountManager.activeAccountId.value

        accountManager.setActiveAccount("non-existent-id")
        assertEquals(originalActive, accountManager.activeAccountId.value)
    }

    @Test
    fun loadAccountsRestoresFromDatabase() {
        val account = accountManager.addAccount("x").getOrThrow()

        // Create a new manager with the same repositories
        val newManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
                planProvider = FreePlanProvider(),
            )
        assertTrue(newManager.accounts.value.isEmpty())

        newManager.loadAccounts()
        assertEquals(1, newManager.accounts.value.size)
        assertEquals(account.accountId, newManager.accounts.value[0].accountId)
    }

    @Test
    fun removeLastAccountSetsActiveToNull() {
        val account = accountManager.addAccount("x").getOrThrow()
        accountManager.removeAccount(account.accountId)
        assertNull(accountManager.activeAccountId.value)
    }
}
