package net.brightroom.uniso.domain.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import net.brightroom.uniso.data.model.Account
import net.brightroom.uniso.data.model.AccountState
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.platform.PlatformPaths
import net.brightroom.uniso.ui.webview.WebViewLifecycleManager
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {
    private lateinit var tempDir: File
    private lateinit var sessionRepository: SessionRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var accountManager: AccountManager
    private lateinit var webViewLifecycleManager: WebViewLifecycleManager
    private lateinit var sessionManager: SessionManager
    private var currentTime = 1000L

    @BeforeTest
    fun setup() {
        val database = createTestDatabase()
        tempDir = File(System.getProperty("java.io.tmpdir"), "uniso-session-test-${System.nanoTime()}")
        tempDir.mkdirs()

        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }

        sessionRepository = SessionRepository(database)
        accountRepository = AccountRepository(database)
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
    }

    @AfterTest
    fun teardown() {
        tempDir.deleteRecursively()
    }

    // SM-001: Periodic save starts and executes at interval
    @Test
    fun periodicSaveExecutesAtInterval() =
        runTest {
            // Add an account so there's data to save
            accountManager.addAccount("x")
            val accountId =
                accountManager.accounts.value
                    .first()
                    .accountId
            accountManager.setActiveAccount(accountId)
            webViewLifecycleManager.activateWebView(accountId)

            sessionManager.startPeriodicSave(this, intervalMs = 1000L)

            // Before first interval, no app_state saved
            assertNull(sessionRepository.getAppState())

            // After first interval, state should be saved
            advanceTimeBy(1001L)
            val appState = sessionRepository.getAppState()
            assertNotNull(appState)
            assertEquals(accountId, appState.activeAccountId)
            assertFalse(appState.cleanShutdown)

            sessionManager.stopPeriodicSave()
        }

    // SM-002: Immediate save persists app_state and account_state
    @Test
    fun saveImmediatePersistsState() {
        accountManager.addAccount("x")
        val accountId =
            accountManager.accounts.value
                .first()
                .accountId
        accountManager.setActiveAccount(accountId)
        webViewLifecycleManager.activateWebView(accountId)
        webViewLifecycleManager.updateAccountUrl(accountId, "https://x.com/home")

        sessionManager.saveImmediate()

        val appState = sessionRepository.getAppState()
        assertNotNull(appState)
        assertEquals(accountId, appState.activeAccountId)
        assertFalse(appState.cleanShutdown)

        val accountState = sessionRepository.getAccountState(accountId)
        assertNotNull(accountState)
        assertEquals("https://x.com/home", accountState.lastUrl)
        assertEquals("active", accountState.webviewStatus)
    }

    // SM-003: Crash detection when clean_shutdown = 0
    @Test
    fun isCleanShutdownReturnsFalseAfterMarkStartup() {
        sessionManager.markStartup()

        assertFalse(sessionManager.isCleanShutdown())
    }

    // SM-004: Clean shutdown records clean_shutdown = 1
    @Test
    fun markCleanShutdownSetsFlag() {
        sessionManager.markStartup()
        assertFalse(sessionManager.isCleanShutdown())

        sessionManager.markCleanShutdown()
        assertTrue(sessionManager.isCleanShutdown())
    }

    // SM-005: Session restore returns saved account states
    @Test
    fun restoreSessionReturnsPreviousState() {
        // Setup: add accounts, save state, simulate clean shutdown
        accountManager.addAccount("x")
        accountManager.addAccount("instagram")
        val accounts = accountManager.accounts.value
        val acc1 = accounts[0].accountId
        val acc2 = accounts[1].accountId

        accountManager.setActiveAccount(acc1)
        webViewLifecycleManager.activateWebView(acc1)
        webViewLifecycleManager.updateAccountUrl(acc1, "https://x.com/home")

        sessionManager.saveImmediate()
        sessionManager.markCleanShutdown()

        // Restore
        val session = sessionManager.restoreSession()
        assertNotNull(session)
        assertEquals(acc1, session.activeAccountId)
        assertTrue(session.wasCleanShutdown)
        assertTrue(session.accountStates.isNotEmpty())

        val restoredAcc1 = session.accountStates.find { it.accountId == acc1 }
        assertNotNull(restoredAcc1)
        assertEquals("https://x.com/home", restoredAcc1.lastUrl)
    }

    // SM-006: Restore with no data returns null
    @Test
    fun restoreSessionReturnsNullWhenNoData() {
        val session = sessionManager.restoreSession()
        assertNull(session)
    }

    @Test
    fun saveImmediateTracksBackgroundWebViewStatus() {
        accountManager.addAccount("x")
        accountManager.addAccount("instagram")
        val accounts = accountManager.accounts.value
        val acc1 = accounts[0].accountId
        val acc2 = accounts[1].accountId

        // Activate both, switch so acc1 goes to background
        webViewLifecycleManager.activateWebView(acc1)
        webViewLifecycleManager.onAccountSwitched(acc1, acc2)
        accountManager.setActiveAccount(acc2)

        sessionManager.saveImmediate()

        val state1 = sessionRepository.getAccountState(acc1)
        assertNotNull(state1)
        assertEquals("background", state1.webviewStatus)

        val state2 = sessionRepository.getAccountState(acc2)
        assertNotNull(state2)
        assertEquals("active", state2.webviewStatus)
    }

    @Test
    fun stopPeriodicSavePreventsSubsequentSaves() =
        runTest {
            accountManager.addAccount("x")
            val accountId =
                accountManager.accounts.value
                    .first()
                    .accountId
            webViewLifecycleManager.activateWebView(accountId)

            sessionManager.startPeriodicSave(this, intervalMs = 1000L)
            advanceTimeBy(1001L)
            assertNotNull(sessionRepository.getAppState())

            sessionManager.stopPeriodicSave()

            // Clear saved state to verify no more saves happen
            sessionRepository.saveAppState(
                net.brightroom.uniso.data.model.AppState(
                    activeAccountId = null,
                    cleanShutdown = true,
                    lastSavedAt = "cleared",
                ),
            )

            advanceTimeBy(5000L)
            // State should remain as we set it (no overwrite from periodic save)
            val state = sessionRepository.getAppState()
            assertNotNull(state)
            assertEquals("cleared", state.lastSavedAt)
        }
}
