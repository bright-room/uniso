package net.brightroom.uniso.ui.sidebar

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.plugin.ServicePluginRegistry
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SidebarViewModelTest {
    private lateinit var accountManager: AccountManager
    private lateinit var servicePluginRegistry: ServicePluginRegistry

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        accountManager =
            AccountManager(
                accountRepository = AccountRepository(database),
                sessionRepository = SessionRepository(database),
            )
        servicePluginRegistry = ServicePluginRegistry(database)
    }

    private fun TestScope.createViewModel(): SidebarViewModel =
        SidebarViewModel(
            accountManager = accountManager,
            servicePluginRegistry = servicePluginRegistry,
            scope = backgroundScope,
        )

    @Test
    fun initialStateIsEmpty() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            assertTrue(viewModel.sidebarAccounts.value.isEmpty())
            assertNull(viewModel.activeAccountId.value)
        }

    @Test
    fun addAccountReflectsInSidebarAccounts() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            accountManager.addAccount("x")

            val accounts = viewModel.sidebarAccounts.value
            assertEquals(1, accounts.size)
            assertEquals("x", accounts[0].serviceId)
            assertEquals("X", accounts[0].serviceName)
        }

    @Test
    fun accountClickSetsActiveAccount() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            val accountA = accountManager.addAccount("x").getOrThrow()
            val accountB = accountManager.addAccount("instagram").getOrThrow()

            viewModel.onAccountClick(accountB.accountId)
            assertEquals(accountB.accountId, viewModel.activeAccountId.value)

            viewModel.onAccountClick(accountA.accountId)
            assertEquals(accountA.accountId, viewModel.activeAccountId.value)
        }

    @Test
    fun removeAccountUpdatesSidebarAccounts() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            val account = accountManager.addAccount("x").getOrThrow()
            assertEquals(1, viewModel.sidebarAccounts.value.size)

            accountManager.removeAccount(account.accountId)
            assertTrue(viewModel.sidebarAccounts.value.isEmpty())
        }

    @Test
    fun sidebarAccountHasCorrectServiceMetadata() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            accountManager.addAccount("instagram")

            val account = viewModel.sidebarAccounts.value.first()
            assertEquals("Instagram", account.serviceName)
            assertTrue(account.url.contains("instagram.com"))
        }

    @Test
    fun getActiveAccountReturnsCorrectAccount() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            val added = accountManager.addAccount("x").getOrThrow()

            val active = viewModel.getActiveAccount()
            assertNotNull(active)
            assertEquals(added.accountId, active.accountId)
        }

    @Test
    fun showAddAccountDialogToggles() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            assertFalse(viewModel.showAddAccountDialog.value)

            viewModel.onAddAccountClick()
            assertTrue(viewModel.showAddAccountDialog.value)

            viewModel.dismissAddAccountDialog()
            assertFalse(viewModel.showAddAccountDialog.value)
        }

    @Test
    fun multipleAccountsGroupedByService() =
        runTest(UnconfinedTestDispatcher()) {
            val viewModel = createViewModel()
            accountManager.addAccount("x")
            accountManager.addAccount("x")
            accountManager.addAccount("instagram")

            val accounts = viewModel.sidebarAccounts.value
            assertEquals(3, accounts.size)
            assertEquals(2, accounts.count { it.serviceId == "x" })
            assertEquals(1, accounts.count { it.serviceId == "instagram" })
        }

    @Test
    fun deriveInitialsHandlesVariousFormats() {
        assertEquals("MA", SidebarViewModel.deriveInitials("@main_account"))
        assertEquals("TA", SidebarViewModel.deriveInitials("Taro Account"))
        assertEquals("HE", SidebarViewModel.deriveInitials("hello"))
        assertEquals("?", SidebarViewModel.deriveInitials(""))
    }
}
