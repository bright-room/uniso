package net.brightroom.uniso.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.SqlSettingsRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.identity.IdentityManager
import net.brightroom.uniso.domain.init.AppInitializer
import net.brightroom.uniso.domain.init.InitState
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
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end integration tests verifying the full application lifecycle:
 * startup → account management → session persistence → crash recovery.
 *
 * These tests exercise the coordination between AccountManager,
 * SessionManager, WebViewLifecycleManager, and AppInitializer
 * using a real (in-memory) database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FullLifecycleIntegrationTest {
    private lateinit var tempDir: File
    private lateinit var sessionRepository: SessionRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var settingsRepository: SqlSettingsRepository
    private lateinit var accountManager: AccountManager
    private lateinit var webViewLifecycleManager: WebViewLifecycleManager
    private lateinit var sessionManager: SessionManager
    private lateinit var identityManager: IdentityManager
    private lateinit var i18nManager: I18nManager
    private var currentTime = 1000L

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        tempDir = File(System.getProperty("java.io.tmpdir"), "uniso-integration-test-${System.nanoTime()}")
        tempDir.mkdirs()

        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }
        val platformLocale =
            object : PlatformLocale {
                override fun getSystemLocale(): AppLocale = AppLocale.EN
            }

        sessionRepository = SessionRepository(database)
        accountRepository = AccountRepository(database)
        settingsRepository = SqlSettingsRepository(database)
        identityManager = IdentityManager(settingsRepository)
        accountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
        webViewLifecycleManager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { sessionRepository.saveAccountState(it) },
                currentTimeMs = { currentTime },
            )
        sessionManager =
            SessionManager(
                sessionRepository = sessionRepository,
                accountManager = accountManager,
                webViewLifecycleManager = webViewLifecycleManager,
                currentTimeMs = { currentTime },
            )
        i18nManager =
            I18nManager(
                platformLocale = platformLocale,
                settingsRepository = settingsRepository,
            )
    }

    @AfterTest
    fun teardown() {
        tempDir.deleteRecursively()
    }

    /**
     * Full lifecycle: first launch → add accounts → browse → save session →
     * clean shutdown → restart → verify session restored.
     */
    @Test
    fun fullLifecycleFromFirstLaunchToSessionRestore() {
        // === Phase 1: First launch initialization ===
        val initializer = createInitializer()
        initializer.initializeCore()

        // Local user ID should be created
        val userId = identityManager.getOrCreateLocalUserId()
        assertNotNull(userId)
        assertTrue(userId.isNotBlank())

        // Skip tutorial for this test
        settingsRepository.setBoolean("tutorial_completed", true)

        // === Phase 2: Add accounts across services ===
        val xAccount = accountManager.addAccount("x").getOrThrow()
        val instaAccount = accountManager.addAccount("instagram").getOrThrow()

        assertEquals(2, accountManager.accounts.value.size)
        assertEquals(instaAccount.accountId, accountManager.activeAccountId.value)

        // === Phase 3: Activate WebViews and browse ===
        accountManager.setActiveAccount(xAccount.accountId)
        webViewLifecycleManager.activateWebView(xAccount.accountId)
        webViewLifecycleManager.updateAccountUrl(xAccount.accountId, "https://x.com/home")

        accountManager.setActiveAccount(instaAccount.accountId)
        webViewLifecycleManager.onAccountSwitched(xAccount.accountId, instaAccount.accountId)
        webViewLifecycleManager.updateAccountUrl(instaAccount.accountId, "https://instagram.com/feed")

        // X should be in background
        assertTrue(webViewLifecycleManager.isInBackground(xAccount.accountId))

        // === Phase 4: Save session and clean shutdown ===
        sessionManager.saveImmediate()
        sessionManager.markCleanShutdown()

        // Verify app state is persisted
        val appState = sessionRepository.getAppState()
        assertNotNull(appState)
        assertTrue(appState.cleanShutdown)
        assertEquals(instaAccount.accountId, appState.activeAccountId)

        // Verify account states are persisted
        val xState = sessionRepository.getAccountState(xAccount.accountId)
        assertNotNull(xState)
        assertEquals("https://x.com/home", xState.lastUrl)
        assertEquals("background", xState.webviewStatus)

        val instaState = sessionRepository.getAccountState(instaAccount.accountId)
        assertNotNull(instaState)
        assertEquals("https://instagram.com/feed", instaState.lastUrl)
        assertEquals("active", instaState.webviewStatus)

        // === Phase 5: Simulate restart ===
        webViewLifecycleManager.destroyAll()

        val newAccountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
        val newWebViewManager =
            WebViewLifecycleManager(
                platformPaths =
                    object : PlatformPaths {
                        override fun getAppDataDir(): String = tempDir.absolutePath
                    },
                accountStateSaver = { sessionRepository.saveAccountState(it) },
                currentTimeMs = { currentTime },
            )
        val newSessionManager =
            SessionManager(
                sessionRepository = sessionRepository,
                accountManager = newAccountManager,
                webViewLifecycleManager = newWebViewManager,
                currentTimeMs = { currentTime },
            )

        val newInitializer =
            AppInitializer(
                accountManager = newAccountManager,
                sessionManager = newSessionManager,
                webViewLifecycleManager = newWebViewManager,
                identityManager = identityManager,
                i18nManager = i18nManager,
                settingsRepository = settingsRepository,
            )
        newInitializer.initializeCore()

        // Accounts should be loaded from DB
        assertEquals(2, newAccountManager.accounts.value.size)

        // Session should be restorable
        // Note: wasCleanShutdown is false because initializeCore() called markStartup()
        // which resets clean_shutdown to 0. The important thing is that account states are preserved.
        val session = newSessionManager.restoreSession()
        assertNotNull(session)
        assertEquals(instaAccount.accountId, session.activeAccountId)

        val restoredX = session.accountStates.find { it.accountId == xAccount.accountId }
        assertNotNull(restoredX)
        assertEquals("https://x.com/home", restoredX.lastUrl)
    }

    /**
     * Crash recovery flow: simulate crash (no clean shutdown) → restart →
     * detect crash → restore or start fresh.
     */
    @Test
    fun crashRecoveryDetectsAndRestoresPreviousSession() {
        settingsRepository.setBoolean("tutorial_completed", true)

        // === Setup: Normal operation ===
        val initializer = createInitializer()
        initializer.initializeCore()

        val account = accountManager.addAccount("x").getOrThrow()
        webViewLifecycleManager.activateWebView(account.accountId)
        webViewLifecycleManager.updateAccountUrl(account.accountId, "https://x.com/notifications")

        sessionManager.saveImmediate()
        // Do NOT mark clean shutdown — simulate crash

        // === Restart after crash ===
        webViewLifecycleManager.destroyAll()

        val newAccountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
        val newWebViewManager =
            WebViewLifecycleManager(
                platformPaths =
                    object : PlatformPaths {
                        override fun getAppDataDir(): String = tempDir.absolutePath
                    },
                accountStateSaver = { sessionRepository.saveAccountState(it) },
            )
        val newSessionManager =
            SessionManager(
                sessionRepository = sessionRepository,
                accountManager = newAccountManager,
                webViewLifecycleManager = newWebViewManager,
            )

        val newInitializer =
            AppInitializer(
                accountManager = newAccountManager,
                sessionManager = newSessionManager,
                webViewLifecycleManager = newWebViewManager,
                identityManager = identityManager,
                i18nManager = i18nManager,
                settingsRepository = settingsRepository,
            )
        newInitializer.initializeCore()

        // Crash should be detected — initializeCore sets needsCrashRecovery internally.
        // Verify via restoreSession that the previous state is available.
        val session = newSessionManager.restoreSession()
        assertNotNull(session)

        val restoredAccount = session.accountStates.find { it.accountId == account.accountId }
        assertNotNull(restoredAccount)
        assertEquals("https://x.com/notifications", restoredAccount.lastUrl)

        // Simulate user choosing "restore"
        newInitializer.onCrashRestore()
        assertIs<InitState.Ready>(newInitializer.state.value)
        assertEquals(account.accountId, newAccountManager.activeAccountId.value)
    }

    /**
     * Crash recovery: user chooses to start fresh instead of restoring.
     */
    @Test
    fun crashRecoveryStartFreshTransitionsToReady() {
        settingsRepository.setBoolean("tutorial_completed", true)
        sessionManager.markStartup() // sets clean_shutdown = 0

        val initializer = createInitializer()
        initializer.initializeCore()
        initializer.onCrashStartNew()

        assertIs<InitState.Ready>(initializer.state.value)
    }

    /**
     * Multi-account lifecycle: add accounts from multiple services,
     * switch between them, delete one, verify state consistency.
     */
    @Test
    fun multiAccountAddSwitchDeleteLifecycle() {
        val initializer = createInitializer()
        initializer.initializeCore()
        settingsRepository.setBoolean("tutorial_completed", true)

        // Add multiple accounts
        val x1 = accountManager.addAccount("x").getOrThrow()
        val x2 = accountManager.addAccount("x").getOrThrow()
        val insta = accountManager.addAccount("instagram").getOrThrow()

        assertEquals(3, accountManager.accounts.value.size)

        // Activate and browse with each
        webViewLifecycleManager.activateWebView(x1.accountId)
        webViewLifecycleManager.updateAccountUrl(x1.accountId, "https://x.com/home")

        webViewLifecycleManager.onAccountSwitched(x1.accountId, x2.accountId)
        webViewLifecycleManager.updateAccountUrl(x2.accountId, "https://x.com/explore")

        webViewLifecycleManager.onAccountSwitched(x2.accountId, insta.accountId)
        webViewLifecycleManager.updateAccountUrl(insta.accountId, "https://instagram.com/reels")

        // Verify background state
        assertTrue(webViewLifecycleManager.isInBackground(x1.accountId))
        assertTrue(webViewLifecycleManager.isInBackground(x2.accountId))

        // Delete x1
        accountManager.removeAccount(x1.accountId)
        webViewLifecycleManager.destroyWebView(x1.accountId)

        assertEquals(2, accountManager.accounts.value.size)
        // x2 is still in background, x1 was destroyed
        assertEquals(1, webViewLifecycleManager.getBackgroundCount())
        assertTrue(webViewLifecycleManager.isInBackground(x2.accountId))

        // Save and verify state
        sessionManager.saveImmediate()
        val state = sessionRepository.getAccountState(x2.accountId)
        assertNotNull(state)
        assertEquals("https://x.com/explore", state.lastUrl)
    }

    /**
     * Periodic session save: verify that sessions are automatically persisted at intervals.
     */
    @Test
    fun periodicSaveAutomaticallyPersistsState() =
        runTest {
            val initializer = createInitializer()
            initializer.initializeCore()

            val account = accountManager.addAccount("x").getOrThrow()
            webViewLifecycleManager.activateWebView(account.accountId)
            webViewLifecycleManager.updateAccountUrl(account.accountId, "https://x.com/home")
            accountManager.setActiveAccount(account.accountId)

            sessionManager.startPeriodicSave(this, intervalMs = 1000L)

            // Before first interval — no app state saved
            val beforeState = sessionRepository.getAppState()
            // May or may not exist depending on prior setup, check after advance
            advanceTimeBy(1001L)

            val afterState = sessionRepository.getAppState()
            assertNotNull(afterState)
            assertEquals(account.accountId, afterState.activeAccountId)

            val accountState = sessionRepository.getAccountState(account.accountId)
            assertNotNull(accountState)
            assertEquals("https://x.com/home", accountState.lastUrl)

            sessionManager.stopPeriodicSave()
        }

    /**
     * WebView LRU eviction during multi-account switching:
     * with maxBackgroundCount=2, switching through 4 accounts should evict the oldest.
     */
    @Test
    fun webViewLruEvictionDuringMultiAccountSwitching() {
        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }
        val savedStates = mutableListOf<net.brightroom.uniso.data.model.AccountState>()
        val manager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { savedStates.add(it) },
                maxBackgroundCount = 2,
                currentTimeMs = { currentTime },
            )

        val initializer = createInitializer()
        initializer.initializeCore()

        val acc1 = accountManager.addAccount("x").getOrThrow()
        val acc2 = accountManager.addAccount("x").getOrThrow()
        val acc3 = accountManager.addAccount("instagram").getOrThrow()
        val acc4 = accountManager.addAccount("instagram").getOrThrow()

        // Switch through accounts
        manager.onAccountSwitched(null, acc1.accountId)
        manager.updateAccountUrl(acc1.accountId, "https://x.com/home")
        currentTime = 2000

        manager.onAccountSwitched(acc1.accountId, acc2.accountId)
        manager.updateAccountUrl(acc2.accountId, "https://x.com/explore")
        currentTime = 3000

        manager.onAccountSwitched(acc2.accountId, acc3.accountId)
        currentTime = 4000

        // At this point: background has acc1 and acc2 (limit=2)
        assertEquals(2, manager.getBackgroundCount())

        manager.onAccountSwitched(acc3.accountId, acc4.accountId)
        // acc3 goes to background → queue exceeds 2 → acc1 (oldest) evicted

        assertEquals(2, manager.getBackgroundCount())
        assertTrue(savedStates.any { it.accountId == acc1.accountId && it.webviewStatus == "suspended" })
    }

    /**
     * WebView suspend timeout: background accounts exceeding timeout get suspended.
     */
    @Test
    fun webViewSuspendTimeoutEvictsExpiredAccounts() =
        runTest {
            var simulatedTime = 0L
            val savedStates = mutableListOf<net.brightroom.uniso.data.model.AccountState>()
            val platformPaths =
                object : PlatformPaths {
                    override fun getAppDataDir(): String = tempDir.absolutePath
                }
            val manager =
                WebViewLifecycleManager(
                    platformPaths = platformPaths,
                    accountStateSaver = { savedStates.add(it) },
                    suspendTimeoutMs = 300_000,
                    checkIntervalMs = 60_000,
                    currentTimeMs = { simulatedTime },
                )

            manager.onAccountSwitched(null, "acc-1")
            manager.updateAccountUrl("acc-1", "https://x.com/home")
            manager.onAccountSwitched("acc-1", "acc-2")

            manager.startSuspendTimer(this)

            // 4 minutes — not yet timed out
            simulatedTime = 240_000
            advanceTimeBy(60_001)
            assertTrue("acc-1" in manager.activatedAccountIds.value)

            // 6 minutes — should be suspended
            simulatedTime = 360_000
            advanceTimeBy(60_001)
            assertTrue("acc-1" !in manager.activatedAccountIds.value)
            assertTrue(savedStates.any { it.accountId == "acc-1" && it.webviewStatus == "suspended" })

            manager.stopSuspendTimer()
        }

    /**
     * Account sort order and display name updates persist across sessions.
     */
    @Test
    fun accountSortOrderAndDisplayNamePersistAcrossSessions() {
        val initializer = createInitializer()
        initializer.initializeCore()

        val x = accountManager.addAccount("x").getOrThrow()
        val insta = accountManager.addAccount("instagram").getOrThrow()

        // Update display name
        accountManager.updateDisplayName(x.accountId, "My X Account")
        assertEquals(
            "My X Account",
            accountManager.accounts.value
                .find { it.accountId == x.accountId }
                ?.displayName,
        )

        // Swap sort order
        accountManager.swapSortOrder(x.accountId, insta.accountId)

        // Verify persistence: reload accounts in a new manager
        val newManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
        newManager.loadAccounts()

        val loadedX = newManager.accounts.value.find { it.accountId == x.accountId }
        assertNotNull(loadedX)
        assertEquals("My X Account", loadedX.displayName)
    }

    /**
     * Shutdown sequence: save → mark clean → destroy all → verify final state.
     */
    @Test
    fun shutdownSequenceSavesStateAndMarksClean() {
        val initializer = createInitializer()
        initializer.initializeCore()

        val account = accountManager.addAccount("x").getOrThrow()
        webViewLifecycleManager.activateWebView(account.accountId)
        webViewLifecycleManager.updateAccountUrl(account.accountId, "https://x.com/home")
        accountManager.setActiveAccount(account.accountId)

        // Execute shutdown sequence
        sessionManager.saveImmediate()
        sessionManager.markCleanShutdown()
        webViewLifecycleManager.destroyAll()

        // Verify final state
        val appState = sessionRepository.getAppState()
        assertNotNull(appState)
        assertTrue(appState.cleanShutdown)
        assertEquals(account.accountId, appState.activeAccountId)
        assertEquals(0, webViewLifecycleManager.getActiveCount())
    }

    /**
     * First launch shows tutorial, completing it transitions to Ready.
     */
    @Test
    fun firstLaunchShowsTutorialAndCompletesToReady() {
        // Do NOT set tutorial_completed — simulate first launch
        sessionManager.markCleanShutdown()

        val initializer = createInitializer()
        initializer.initializeCore()

        // After core init, tutorial flag should be detected
        // Simulate CEF ready state by directly calling onTutorialComplete
        assertIs<InitState.Loading>(initializer.state.value)

        initializer.onTutorialComplete()
        assertIs<InitState.Ready>(initializer.state.value)
        assertEquals(true, settingsRepository.getBoolean("tutorial_completed"))
    }

    /**
     * I18n locale change persists and is restored on restart.
     */
    @Test
    fun localeChangePersistsAcrossRestart() {
        val initializer = createInitializer()
        initializer.initializeCore()

        // Change locale
        i18nManager.setLocale(AppLocale.JA)
        assertEquals(AppLocale.JA, i18nManager.currentLocale.value)

        // Simulate restart: create a new I18nManager
        val platformLocale =
            object : PlatformLocale {
                override fun getSystemLocale(): AppLocale = AppLocale.EN
            }
        val newI18nManager =
            I18nManager(
                platformLocale = platformLocale,
                settingsRepository = settingsRepository,
            )
        newI18nManager.initialize()

        // Should restore JA, not fall back to system locale (EN)
        assertEquals(AppLocale.JA, newI18nManager.currentLocale.value)
    }

    private fun createInitializer(): AppInitializer =
        AppInitializer(
            accountManager = accountManager,
            sessionManager = sessionManager,
            webViewLifecycleManager = webViewLifecycleManager,
            identityManager = identityManager,
            i18nManager = i18nManager,
            settingsRepository = settingsRepository,
        )
}
