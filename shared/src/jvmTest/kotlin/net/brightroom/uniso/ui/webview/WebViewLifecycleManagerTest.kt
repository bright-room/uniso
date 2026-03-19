package net.brightroom.uniso.ui.webview

import net.brightroom.uniso.platform.PlatformPaths
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebViewLifecycleManagerTest {
    private lateinit var tempDir: File
    private lateinit var platformPaths: PlatformPaths
    private lateinit var manager: WebViewLifecycleManager

    @BeforeTest
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "uniso-test-${System.nanoTime()}")
        tempDir.mkdirs()

        platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }

        manager = WebViewLifecycleManager(platformPaths)
    }

    @AfterTest
    fun teardown() {
        tempDir.deleteRecursively()
    }

    // WV-001: New WebView generation
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

    // WV-006: Complete destruction
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

        // Add some files to simulate CEF profile data
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
}
