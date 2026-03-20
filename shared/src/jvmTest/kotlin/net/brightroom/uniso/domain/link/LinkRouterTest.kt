package net.brightroom.uniso.domain.link

import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.plugin.ServicePluginRegistry
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LinkRouterTest {
    private lateinit var linkRouter: LinkRouter
    private lateinit var accountManager: AccountManager
    private lateinit var servicePluginRegistry: ServicePluginRegistry

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        servicePluginRegistry = ServicePluginRegistry(database)
        accountManager =
            AccountManager(
                accountRepository = AccountRepository(database),
                sessionRepository = SessionRepository(database),
            )
    }

    private fun createRouter(): LinkRouter =
        LinkRouter(
            servicePluginRegistry = servicePluginRegistry,
            accountManager = accountManager,
        )

    // LR-001: External link → ExternalLink
    @Test
    fun externalLinkClassifiedAsExternalLink() {
        val account = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()

        val result = router.classifyLink("https://example.com/article", account.accountId)

        assertIs<LinkClassification.ExternalLink>(result)
        assertEquals("https://example.com/article", result.url)
    }

    // LR-002: Internal link, single account → InternalSingleAccount
    @Test
    fun internalLinkWithSingleAccountClassifiedAsInternalSingleAccount() {
        // Source account is Instagram, target is X
        val igAccount = accountManager.addAccount("instagram").getOrThrow()
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()

        val result = router.classifyLink("https://x.com/user", igAccount.accountId)

        assertIs<LinkClassification.InternalSingleAccount>(result)
        assertEquals("https://x.com/user", result.url)
        assertEquals(xAccount.accountId, result.account.accountId)
    }

    // LR-003: Internal link, multiple accounts → InternalMultiAccount
    @Test
    fun internalLinkWithMultipleAccountsClassifiedAsInternalMultiAccount() {
        // Source account is X, target is Instagram with 2 accounts
        val xAccount = accountManager.addAccount("x").getOrThrow()
        accountManager.addAccount("instagram")
        accountManager.addAccount("instagram")
        val router = createRouter()

        val result = router.classifyLink("https://instagram.com/p/123", xAccount.accountId)

        assertIs<LinkClassification.InternalMultiAccount>(result)
        assertEquals("https://instagram.com/p/123", result.url)
        assertEquals("instagram", result.serviceId)
        assertEquals(2, result.accounts.size)
    }

    // LR-004: Internal link, no logged-in account → InternalNoAccount
    @Test
    fun internalLinkWithNoAccountClassifiedAsInternalNoAccount() {
        // Source is X, target is Instagram but no Instagram accounts
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()

        val result = router.classifyLink("https://instagram.com/user", xAccount.accountId)

        assertIs<LinkClassification.InternalNoAccount>(result)
        assertEquals("https://instagram.com/user", result.url)
    }

    // LR-005: Same domain navigation → SameDomainNavigation
    @Test
    fun sameDomainNavigationClassifiedAsSameDomainNavigation() {
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()

        val result = router.classifyLink("https://x.com/home", xAccount.accountId)

        assertIs<LinkClassification.SameDomainNavigation>(result)
        assertEquals("https://x.com/home", result.url)
    }

    // LR-006: Subdomain support
    @Test
    fun subdomainMatchedCorrectly() {
        val igAccount = accountManager.addAccount("instagram").getOrThrow()
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()

        val result = router.classifyLink("https://mobile.x.com/user", igAccount.accountId)

        assertIs<LinkClassification.InternalSingleAccount>(result)
        assertEquals(xAccount.accountId, result.account.accountId)
    }

    @Test
    fun handleNavigationReturnsFalseForSameDomain() {
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()

        val shouldCancel =
            router.handleNavigation(
                url = "https://x.com/home",
                sourceAccountId = xAccount.accountId,
                onExternalLink = {},
                onSwitchAccount = { _, _ -> },
                onShowAccountSelector = {},
            )

        assertEquals(false, shouldCancel)
    }

    @Test
    fun handleNavigationReturnsTrueForExternalLink() {
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()
        var openedUrl: String? = null

        val shouldCancel =
            router.handleNavigation(
                url = "https://example.com/article",
                sourceAccountId = xAccount.accountId,
                onExternalLink = { openedUrl = it },
                onSwitchAccount = { _, _ -> },
                onShowAccountSelector = {},
            )

        assertEquals(true, shouldCancel)
        assertEquals("https://example.com/article", openedUrl)
    }

    @Test
    fun handleNavigationSwitchesAccountForSingleMatch() {
        val igAccount = accountManager.addAccount("instagram").getOrThrow()
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()
        var switchedTo: String? = null

        val shouldCancel =
            router.handleNavigation(
                url = "https://x.com/user",
                sourceAccountId = igAccount.accountId,
                onExternalLink = {},
                onSwitchAccount = { accountId, _ -> switchedTo = accountId },
                onShowAccountSelector = {},
            )

        assertEquals(true, shouldCancel)
        assertEquals(xAccount.accountId, switchedTo)
    }

    @Test
    fun handleNavigationShowsSelectorForMultipleAccounts() {
        val xAccount = accountManager.addAccount("x").getOrThrow()
        accountManager.addAccount("instagram")
        accountManager.addAccount("instagram")
        val router = createRouter()
        var selectorShown = false

        val shouldCancel =
            router.handleNavigation(
                url = "https://instagram.com/p/123",
                sourceAccountId = xAccount.accountId,
                onExternalLink = {},
                onSwitchAccount = { _, _ -> },
                onShowAccountSelector = { selectorShown = true },
            )

        assertEquals(true, shouldCancel)
        assertEquals(true, selectorShown)
    }

    @Test
    fun extractDomainHandlesValidUrl() {
        assertEquals("x.com", LinkRouter.extractDomain("https://x.com/user"))
        assertEquals("mobile.x.com", LinkRouter.extractDomain("https://mobile.x.com/home"))
        assertEquals("example.com", LinkRouter.extractDomain("https://example.com/path?q=1"))
    }

    @Test
    fun extractDomainReturnsNullForInvalidUrl() {
        assertEquals(null, LinkRouter.extractDomain("not-a-url"))
        assertEquals(null, LinkRouter.extractDomain(""))
    }

    @Test
    fun sameDomainViaAlternateDomain() {
        // X account should recognize twitter.com as same domain
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val router = createRouter()

        val result = router.classifyLink("https://twitter.com/home", xAccount.accountId)

        assertIs<LinkClassification.SameDomainNavigation>(result)
    }
}
