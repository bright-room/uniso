package net.brightroom.uniso.integration

import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.SqlSettingsRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.identity.IdentityManager
import net.brightroom.uniso.domain.init.AppInitializer
import net.brightroom.uniso.domain.session.SessionManager
import net.brightroom.uniso.domain.settings.AppLocale
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.platform.PlatformLocale
import net.brightroom.uniso.platform.PlatformPaths
import net.brightroom.uniso.ui.webview.WebViewLifecycleManager
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Performance verification tests for critical operations.
 *
 * NFR-001 targets:
 * - Sidebar operation response: < 300ms
 * - App startup (core init): < 5s
 * - Account switch (in-memory): < 500ms
 *
 * These tests validate that domain/data layer operations complete
 * well within the performance budgets. Actual rendering and WebView
 * load times require manual/E2E testing.
 */
class PerformanceVerificationTest {
    private lateinit var tempDir: File

    @BeforeTest
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "uniso-perf-test-${System.nanoTime()}")
        tempDir.mkdirs()
    }

    @AfterTest
    fun teardown() {
        tempDir.deleteRecursively()
    }

    /**
     * Core initialization (DB + accounts + settings + i18n) should complete within 2s.
     * This is the domain-layer portion of the 5s startup budget.
     */
    @Test
    fun coreInitializationCompletesWithin2Seconds() {
        val database = createTestDatabase()
        val accountRepository = AccountRepository(database)
        val sessionRepository = SessionRepository(database)
        val settingsRepository = SqlSettingsRepository(database)

        val accountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }
        val webViewLifecycleManager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { sessionRepository.saveAccountState(it) },
            )
        val sessionManager =
            SessionManager(
                sessionRepository = sessionRepository,
                accountManager = accountManager,
                webViewLifecycleManager = webViewLifecycleManager,
            )
        val identityManager = IdentityManager(settingsRepository)
        val platformLocale =
            object : PlatformLocale {
                override fun getSystemLocale(): AppLocale = AppLocale.EN
            }
        val i18nManager =
            I18nManager(
                platformLocale = platformLocale,
                settingsRepository = settingsRepository,
            )

        val initializer =
            AppInitializer(
                accountManager = accountManager,
                sessionManager = sessionManager,
                webViewLifecycleManager = webViewLifecycleManager,
                identityManager = identityManager,
                i18nManager = i18nManager,
                settingsRepository = settingsRepository,
            )

        val startMs = System.currentTimeMillis()
        initializer.initializeCore()
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(
            elapsedMs < 2000,
            "Core initialization took ${elapsedMs}ms, exceeding the 2000ms budget",
        )
    }

    /**
     * Adding 10 accounts should complete within 1s (well within sidebar response budget).
     */
    @Test
    fun adding10AccountsCompletesWithin1Second() {
        val database = createTestDatabase()
        val accountManager =
            AccountManager(
                accountRepository = AccountRepository(database),
                sessionRepository = SessionRepository(database),
            )

        val startMs = System.currentTimeMillis()
        repeat(10) { i ->
            val serviceId = if (i % 2 == 0) "x" else "instagram"
            accountManager.addAccount(serviceId)
        }
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(
            elapsedMs < 1000,
            "Adding 10 accounts took ${elapsedMs}ms, exceeding the 1000ms budget",
        )
    }

    /**
     * Account switching (in-memory, without WebView) should complete within 10ms.
     */
    @Test
    fun accountSwitchingCompletesWithin10Ms() {
        val database = createTestDatabase()
        val accountManager =
            AccountManager(
                accountRepository = AccountRepository(database),
                sessionRepository = SessionRepository(database),
            )

        // Setup: add accounts
        val accounts =
            (0 until 10).map { i ->
                accountManager.addAccount(if (i % 2 == 0) "x" else "instagram").getOrThrow()
            }

        // Measure switching between all accounts
        val startMs = System.currentTimeMillis()
        repeat(100) { i ->
            accountManager.setActiveAccount(accounts[i % accounts.size].accountId)
        }
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(
            elapsedMs < 100,
            "100 account switches took ${elapsedMs}ms (avg ${elapsedMs / 100.0}ms each), exceeding budget",
        )
    }

    /**
     * WebView lifecycle management (activate + switch + LRU eviction) for 10 accounts
     * should complete within 500ms.
     */
    @Test
    fun webViewLifecycleManagementPerformance() {
        var time = 0L
        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }
        val manager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = {},
                maxBackgroundCount = 3,
                currentTimeMs = { time },
            )

        val accountIds = (0 until 10).map { "account-$it" }

        val startMs = System.currentTimeMillis()

        // Activate and switch through all accounts
        manager.onAccountSwitched(null, accountIds[0])
        for (i in 1 until accountIds.size) {
            time += 1000
            manager.onAccountSwitched(accountIds[i - 1], accountIds[i])
        }

        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(
            elapsedMs < 500,
            "WebView lifecycle operations for 10 accounts took ${elapsedMs}ms, exceeding 500ms budget",
        )
    }

    /**
     * Session save (with 10 activated WebViews) should complete within 200ms.
     */
    @Test
    fun sessionSaveWith10AccountsCompletesWithin200Ms() {
        val database = createTestDatabase()
        val accountRepository = AccountRepository(database)
        val sessionRepository = SessionRepository(database)
        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }
        val accountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
        val webViewLifecycleManager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { sessionRepository.saveAccountState(it) },
                maxBackgroundCount = 10,
            )
        val sessionManager =
            SessionManager(
                sessionRepository = sessionRepository,
                accountManager = accountManager,
                webViewLifecycleManager = webViewLifecycleManager,
            )

        // Setup: add and activate 10 accounts
        repeat(10) { i ->
            val account = accountManager.addAccount(if (i % 2 == 0) "x" else "instagram").getOrThrow()
            webViewLifecycleManager.activateWebView(account.accountId)
            webViewLifecycleManager.updateAccountUrl(account.accountId, "https://example.com/page$i")
        }

        val startMs = System.currentTimeMillis()
        sessionManager.saveImmediate()
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(
            elapsedMs < 200,
            "Session save with 10 accounts took ${elapsedMs}ms, exceeding 200ms budget",
        )
    }

    /**
     * Session restore should complete within 100ms.
     */
    @Test
    fun sessionRestoreCompletesWithin100Ms() {
        val database = createTestDatabase()
        val accountRepository = AccountRepository(database)
        val sessionRepository = SessionRepository(database)
        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }
        val accountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
        val webViewLifecycleManager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { sessionRepository.saveAccountState(it) },
                maxBackgroundCount = 10,
            )
        val sessionManager =
            SessionManager(
                sessionRepository = sessionRepository,
                accountManager = accountManager,
                webViewLifecycleManager = webViewLifecycleManager,
            )

        // Setup: add accounts and save state
        repeat(10) { i ->
            val account = accountManager.addAccount(if (i % 2 == 0) "x" else "instagram").getOrThrow()
            webViewLifecycleManager.activateWebView(account.accountId)
            webViewLifecycleManager.updateAccountUrl(account.accountId, "https://example.com/page$i")
        }
        sessionManager.saveImmediate()

        val startMs = System.currentTimeMillis()
        val session = sessionManager.restoreSession()
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(
            session != null,
            "Session restore returned null",
        )
        assertTrue(
            elapsedMs < 100,
            "Session restore took ${elapsedMs}ms, exceeding 100ms budget",
        )
    }

    /**
     * cyclic account navigation (next/previous) with 10 accounts should be near-instant.
     */
    @Test
    fun cyclicAccountNavigationPerformance() {
        val database = createTestDatabase()
        val accountManager =
            AccountManager(
                accountRepository = AccountRepository(database),
                sessionRepository = SessionRepository(database),
            )

        repeat(10) { i ->
            accountManager.addAccount(if (i % 2 == 0) "x" else "instagram")
        }

        val startMs = System.currentTimeMillis()
        repeat(100) {
            accountManager.switchToNextAccount()
        }
        repeat(100) {
            accountManager.switchToPreviousAccount()
        }
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(
            elapsedMs < 100,
            "200 cyclic navigations took ${elapsedMs}ms, exceeding 100ms budget",
        )
    }
}
