package net.brightroom.uniso.ui.webview

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import net.brightroom.uniso.data.model.AccountState
import net.brightroom.uniso.platform.PlatformPaths
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WebViewLifecycleManagerTest {
    private lateinit var tempDir: File
    private lateinit var platformPaths: PlatformPaths
    private lateinit var manager: WebViewLifecycleManager
    private val savedStates = mutableListOf<AccountState>()
    private var currentTime = 0L

    @BeforeTest
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "uniso-test-${System.nanoTime()}")
        tempDir.mkdirs()

        platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }

        savedStates.clear()
        currentTime = 0L

        manager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { savedStates.add(it) },
                currentTimeMs = { currentTime },
            )
    }

    @AfterTest
    fun teardown() {
        tempDir.deleteRecursively()
    }

    // ──────────────────────────────────────────
    // WV-001: New WebView generation (existing)
    // ──────────────────────────────────────────

    @Test
    fun `activateWebView adds account to activated set`() {
        val accountId = "test-account-1"

        manager.activateWebView(accountId)

        assertTrue(accountId in manager.activatedAccountIds.value)
        assertEquals(1, manager.getActiveCount())
    }

    @Test
    fun `activateWebView creates profile directory`() {
        val accountId = "test-account-1"

        manager.activateWebView(accountId)

        val profileDir = File(platformPaths.getCefProfileDir(accountId))
        assertTrue(profileDir.exists())
        assertTrue(profileDir.isDirectory)
    }

    @Test
    fun `activateWebView is idempotent`() {
        val accountId = "test-account-1"

        manager.activateWebView(accountId)
        manager.activateWebView(accountId)

        assertEquals(1, manager.getActiveCount())
    }

    @Test
    fun `multiple accounts can be activated independently`() {
        manager.activateWebView("account-1")
        manager.activateWebView("account-2")
        manager.activateWebView("account-3")

        assertEquals(3, manager.getActiveCount())
        assertTrue("account-1" in manager.activatedAccountIds.value)
        assertTrue("account-2" in manager.activatedAccountIds.value)
        assertTrue("account-3" in manager.activatedAccountIds.value)
    }

    // ──────────────────────────────────────────
    // WV-006: Complete destruction (existing)
    // ──────────────────────────────────────────

    @Test
    fun `destroyWebView removes account from activated set`() {
        val accountId = "test-account-1"
        manager.activateWebView(accountId)

        manager.destroyWebView(accountId)

        assertFalse(accountId in manager.activatedAccountIds.value)
        assertEquals(0, manager.getActiveCount())
    }

    @Test
    fun `destroyWebView deletes profile directory`() {
        val accountId = "test-account-1"
        manager.activateWebView(accountId)
        val profileDir = File(platformPaths.getCefProfileDir(accountId))
        assertTrue(profileDir.exists())

        File(profileDir, "cookies").writeText("test-cookies")
        File(profileDir, "cache").mkdir()
        File(File(profileDir, "cache"), "data.bin").writeText("cached-data")

        manager.destroyWebView(accountId)

        assertFalse(profileDir.exists())
    }

    @Test
    fun `destroyWebView on non-existent account is no-op`() {
        manager.destroyWebView("non-existent")
        assertEquals(0, manager.getActiveCount())
    }

    @Test
    fun `destroyAll removes all activated accounts`() {
        manager.activateWebView("account-1")
        manager.activateWebView("account-2")
        manager.activateWebView("account-3")

        manager.destroyAll()

        assertEquals(0, manager.getActiveCount())
        assertTrue(manager.activatedAccountIds.value.isEmpty())
    }

    @Test
    fun `getProfilePath returns correct path`() {
        val accountId = "test-account-1"
        val expectedPath = "${tempDir.absolutePath}/cef_profiles/$accountId"

        assertEquals(expectedPath, manager.getProfilePath(accountId))
    }

    // ──────────────────────────────────────────
    // WV-002: Background queue and LRU eviction
    // ──────────────────────────────────────────

    @Test
    fun `onAccountSwitched moves previous account to background`() {
        manager.activateWebView("account-1")

        manager.onAccountSwitched("account-1", "account-2")

        assertTrue("account-1" in manager.activatedAccountIds.value)
        assertTrue("account-2" in manager.activatedAccountIds.value)
        assertTrue(manager.isInBackground("account-1"))
        assertFalse(manager.isInBackground("account-2"))
        assertEquals(1, manager.getBackgroundCount())
    }

    @Test
    fun `onAccountSwitched with null previous does not crash`() {
        manager.onAccountSwitched(null, "account-1")

        assertTrue("account-1" in manager.activatedAccountIds.value)
        assertEquals(0, manager.getBackgroundCount())
    }

    @Test
    fun `onAccountSwitched to same account is no-op for background`() {
        manager.activateWebView("account-1")

        manager.onAccountSwitched("account-1", "account-1")

        assertEquals(0, manager.getBackgroundCount())
        assertTrue("account-1" in manager.activatedAccountIds.value)
    }

    @Test
    fun `returning to background account removes it from background queue`() {
        manager.onAccountSwitched(null, "account-1")
        manager.onAccountSwitched("account-1", "account-2")

        assertTrue(manager.isInBackground("account-1"))

        manager.onAccountSwitched("account-2", "account-1")

        assertFalse(manager.isInBackground("account-1"))
        assertTrue(manager.isInBackground("account-2"))
    }

    @Test
    fun `LRU eviction suspends oldest when exceeding maxBackgroundCount`() {
        val mgr =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { savedStates.add(it) },
                maxBackgroundCount = 2,
                currentTimeMs = { currentTime },
            )

        mgr.onAccountSwitched(null, "account-1")
        currentTime = 1000
        mgr.onAccountSwitched("account-1", "account-2")
        currentTime = 2000
        mgr.onAccountSwitched("account-2", "account-3")
        currentTime = 3000
        // Now: account-1 and account-2 in background (limit=2), account-3 active
        assertEquals(2, mgr.getBackgroundCount())

        mgr.onAccountSwitched("account-3", "account-4")
        // account-3 goes to background -> queue now has account-1, account-2, account-3 (3 > limit 2)
        // account-1 (oldest) should be evicted

        assertEquals(2, mgr.getBackgroundCount())
        assertFalse("account-1" in mgr.activatedAccountIds.value)
        assertTrue("account-2" in mgr.activatedAccountIds.value)
        assertTrue("account-3" in mgr.activatedAccountIds.value)
        assertTrue("account-4" in mgr.activatedAccountIds.value)
    }

    @Test
    fun `LRU eviction saves state before suspending`() {
        val mgr =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { savedStates.add(it) },
                maxBackgroundCount = 1,
                currentTimeMs = { currentTime },
            )

        mgr.onAccountSwitched(null, "account-1")
        mgr.updateAccountUrl("account-1", "https://x.com/home")
        currentTime = 1000
        mgr.onAccountSwitched("account-1", "account-2")
        // background has account-1 (limit=1), that's fine
        currentTime = 2000
        mgr.onAccountSwitched("account-2", "account-3")
        // account-2 goes to background -> queue has account-1, account-2 (2 > limit 1)
        // account-1 evicted

        assertEquals(1, savedStates.size)
        assertEquals("account-1", savedStates[0].accountId)
        assertEquals("https://x.com/home", savedStates[0].lastUrl)
        assertEquals("suspended", savedStates[0].webviewStatus)
    }

    @Test
    fun `LRU order updates when account is re-accessed`() {
        val mgr =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { savedStates.add(it) },
                maxBackgroundCount = 2,
                currentTimeMs = { currentTime },
            )

        mgr.onAccountSwitched(null, "account-1")
        currentTime = 1000
        mgr.onAccountSwitched("account-1", "account-2")
        currentTime = 2000
        mgr.onAccountSwitched("account-2", "account-3")
        // background: account-1(t=1000), account-2(t=2000)

        currentTime = 3000
        mgr.onAccountSwitched("account-3", "account-1")
        // account-1 removed from background, account-3 goes to background
        // background: account-2(t=2000), account-3(t=3000)

        currentTime = 4000
        mgr.onAccountSwitched("account-1", "account-4")
        // account-1 goes to background -> queue: account-2, account-3, account-1 (3 > limit 2)
        // account-2 (oldest) should be evicted

        assertEquals(1, savedStates.size)
        assertEquals("account-2", savedStates[0].accountId)
    }

    // ──────────────────────────────────────────
    // WV-003: Suspend timeout
    // ──────────────────────────────────────────

    @Test
    fun `checkTimeouts suspends accounts exceeding timeout`() {
        manager.onAccountSwitched(null, "account-1")
        currentTime = 0
        manager.onAccountSwitched("account-1", "account-2")
        // account-1 in background since t=0

        currentTime = 300_000 // exactly 5 minutes

        manager.checkTimeouts()

        assertFalse("account-1" in manager.activatedAccountIds.value)
        assertEquals(0, manager.getBackgroundCount())
        assertEquals(1, savedStates.size)
        assertEquals("account-1", savedStates[0].accountId)
    }

    @Test
    fun `checkTimeouts does not suspend accounts within timeout`() {
        manager.onAccountSwitched(null, "account-1")
        currentTime = 0
        manager.onAccountSwitched("account-1", "account-2")

        currentTime = 299_999 // just under 5 minutes

        manager.checkTimeouts()

        assertTrue("account-1" in manager.activatedAccountIds.value)
        assertEquals(1, manager.getBackgroundCount())
        assertEquals(0, savedStates.size)
    }

    @Test
    fun `startSuspendTimer periodically checks timeouts`() =
        runTest {
            var simulatedTime = 0L
            val mgr =
                WebViewLifecycleManager(
                    platformPaths = platformPaths,
                    accountStateSaver = { savedStates.add(it) },
                    suspendTimeoutMs = 300_000,
                    checkIntervalMs = 60_000,
                    currentTimeMs = { simulatedTime },
                )

            mgr.onAccountSwitched(null, "account-1")
            mgr.onAccountSwitched("account-1", "account-2")

            mgr.startSuspendTimer(this)

            // Advance 4 minutes — not yet timed out
            simulatedTime = 240_000
            advanceTimeBy(60_001)
            assertTrue("account-1" in mgr.activatedAccountIds.value)

            // Advance to 5+ minutes — should be suspended after next check
            simulatedTime = 360_000
            advanceTimeBy(60_001)
            assertFalse("account-1" in mgr.activatedAccountIds.value)

            mgr.stopSuspendTimer()
        }

    // ──────────────────────────────────────────
    // WV-004: Restore after suspend
    // ──────────────────────────────────────────

    @Test
    fun `suspended account preserves profile directory`() {
        manager.activateWebView("account-1")
        val profileDir = File(platformPaths.getCefProfileDir("account-1"))
        assertTrue(profileDir.exists())

        manager.onAccountSwitched("account-1", "account-2")

        // Manually trigger suspend
        manager.suspendWebView("account-1")

        // Profile directory should still exist
        assertTrue(profileDir.exists())
        // But account should be removed from activated set
        assertFalse("account-1" in manager.activatedAccountIds.value)
    }

    @Test
    fun `re-activating suspended account adds it back to activated set`() {
        manager.onAccountSwitched(null, "account-1")
        manager.onAccountSwitched("account-1", "account-2")
        manager.suspendWebView("account-1")

        assertFalse("account-1" in manager.activatedAccountIds.value)

        // Re-activate
        manager.onAccountSwitched("account-2", "account-1")

        assertTrue("account-1" in manager.activatedAccountIds.value)
        assertFalse(manager.isInBackground("account-1"))
    }

    @Test
    fun `suspended account state includes last known URL`() {
        manager.onAccountSwitched(null, "account-1")
        manager.updateAccountUrl("account-1", "https://x.com/notifications")

        manager.suspendWebView("account-1")

        assertEquals(1, savedStates.size)
        assertEquals("https://x.com/notifications", savedStates[0].lastUrl)
    }

    @Test
    fun `suspend clears tracked URL`() {
        manager.onAccountSwitched(null, "account-1")
        manager.updateAccountUrl("account-1", "https://x.com/home")

        manager.suspendWebView("account-1")

        assertNull(manager.getAccountUrl("account-1"))
    }

    // ──────────────────────────────────────────
    // WV-005: Background count management
    // ──────────────────────────────────────────

    @Test
    fun `getBackgroundCount returns correct count`() {
        assertEquals(0, manager.getBackgroundCount())

        manager.onAccountSwitched(null, "account-1")
        assertEquals(0, manager.getBackgroundCount())

        manager.onAccountSwitched("account-1", "account-2")
        assertEquals(1, manager.getBackgroundCount())

        manager.onAccountSwitched("account-2", "account-3")
        assertEquals(2, manager.getBackgroundCount())
    }

    @Test
    fun `background count respects maxBackgroundCount limit`() {
        val mgr =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { savedStates.add(it) },
                maxBackgroundCount = 2,
                currentTimeMs = { currentTime },
            )

        mgr.onAccountSwitched(null, "a1")
        currentTime = 1000
        mgr.onAccountSwitched("a1", "a2")
        currentTime = 2000
        mgr.onAccountSwitched("a2", "a3")
        currentTime = 3000
        mgr.onAccountSwitched("a3", "a4")
        currentTime = 4000
        mgr.onAccountSwitched("a4", "a5")

        // Max 2 in background, rest should be suspended
        assertEquals(2, mgr.getBackgroundCount())
    }

    @Test
    fun `destroyWebView removes from background queue`() {
        manager.onAccountSwitched(null, "account-1")
        manager.onAccountSwitched("account-1", "account-2")
        assertEquals(1, manager.getBackgroundCount())

        manager.destroyWebView("account-1")

        assertEquals(0, manager.getBackgroundCount())
        assertFalse("account-1" in manager.activatedAccountIds.value)
    }

    @Test
    fun `destroyAll clears background queue`() {
        manager.onAccountSwitched(null, "account-1")
        manager.onAccountSwitched("account-1", "account-2")
        manager.onAccountSwitched("account-2", "account-3")

        manager.destroyAll()

        assertEquals(0, manager.getBackgroundCount())
        assertEquals(0, manager.getActiveCount())
    }

    // ──────────────────────────────────────────
    // URL tracking
    // ──────────────────────────────────────────

    @Test
    fun `updateAccountUrl tracks URL`() {
        manager.activateWebView("account-1")
        manager.updateAccountUrl("account-1", "https://x.com/home")

        assertEquals("https://x.com/home", manager.getAccountUrl("account-1"))
    }

    @Test
    fun `destroyWebView clears tracked URL`() {
        manager.activateWebView("account-1")
        manager.updateAccountUrl("account-1", "https://x.com/home")

        manager.destroyWebView("account-1")

        assertNull(manager.getAccountUrl("account-1"))
    }
}
