package net.brightroom.uniso.domain.updater

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JvmAutoUpdaterTest {
    @Test
    fun initialStateHasNoUpdateInfo() {
        val updater = JvmAutoUpdater(appcastUrl = "http://invalid.test/appcast.xml")
        assertNull(updater.updateInfo.value)
        assertFalse(updater.isChecking.value)
    }

    @Test
    fun dismissUpdateClearsUpdateInfo() =
        runTest {
            val updater = createTestUpdater(newerVersion = "2.0.0")
            updater.checkForUpdates()
            advanceUntilIdle()

            assertNotNull(updater.updateInfo.value)
            updater.dismissUpdate()
            assertNull(updater.updateInfo.value)
        }

    @Test
    fun failedCheckDoesNotCrash() =
        runTest {
            val updater = JvmAutoUpdater(appcastUrl = "http://invalid.test/nonexistent.xml", currentVersion = "1.0.0")
            updater.checkForUpdatesInBackground()
            advanceUntilIdle()

            assertNull(updater.updateInfo.value)
            assertFalse(updater.isChecking.value)
        }

    @Test
    fun noUpdateWhenVersionIsCurrent() =
        runTest {
            val updater = createTestUpdater(newerVersion = "1.0.0")
            updater.checkForUpdates()
            advanceUntilIdle()

            assertNull(updater.updateInfo.value)
        }

    @Test
    fun detectsNewerVersion() =
        runTest {
            val updater = createTestUpdater(newerVersion = "2.0.0")
            updater.checkForUpdates()
            advanceUntilIdle()

            val info = updater.updateInfo.value
            assertNotNull(info)
            assertEquals("2.0.0", info.version)
            assertEquals("New release", info.releaseNotes)
            assertEquals("https://example.com/download", info.downloadUrl)
        }

    @Test
    fun ignoresOlderVersion() =
        runTest {
            val updater = createTestUpdater(newerVersion = "0.9.0")
            updater.checkForUpdates()
            advanceUntilIdle()

            assertNull(updater.updateInfo.value)
        }

    @Test
    fun isCheckingReturnsFalseAfterCheck() =
        runTest {
            val updater = createTestUpdater(newerVersion = "2.0.0")
            updater.checkForUpdates()
            advanceUntilIdle()

            assertFalse(updater.isChecking.value)
        }

    @Test
    fun backgroundCheckBehavesSameAsInteractive() =
        runTest {
            val updater = createTestUpdater(newerVersion = "3.0.0")
            updater.checkForUpdatesInBackground()
            advanceUntilIdle()

            val info = updater.updateInfo.value
            assertNotNull(info)
            assertEquals("3.0.0", info.version)
        }

    private fun createTestUpdater(newerVersion: String): TestableAutoUpdater =
        TestableAutoUpdater(
            currentVersion = "1.0.0",
            fakeUpdateInfo =
                UpdateInfo(
                    version = newerVersion,
                    releaseNotes = "New release",
                    downloadUrl = "https://example.com/download",
                ),
        )
}

/**
 * Testable implementation that skips HTTP and directly returns fake data.
 */
private class TestableAutoUpdater(
    currentVersion: String,
    private val fakeUpdateInfo: UpdateInfo?,
) : AutoUpdater {
    private val _updateInfo = kotlinx.coroutines.flow.MutableStateFlow<UpdateInfo?>(null)
    override val updateInfo = _updateInfo.asStateFlow()

    private val _isChecking = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isChecking = _isChecking.asStateFlow()

    private val versionToCheck = currentVersion

    override suspend fun checkForUpdates() = performCheck()

    override suspend fun checkForUpdatesInBackground() = performCheck()

    override fun dismissUpdate() {
        _updateInfo.value = null
    }

    private fun performCheck() {
        _isChecking.value = true
        try {
            if (fakeUpdateInfo != null && VersionComparator.isNewer(versionToCheck, fakeUpdateInfo.version)) {
                _updateInfo.value = fakeUpdateInfo
            }
        } finally {
            _isChecking.value = false
        }
    }
}

private fun <T> kotlinx.coroutines.flow.MutableStateFlow<T>.asStateFlow(): kotlinx.coroutines.flow.StateFlow<T> =
    this as kotlinx.coroutines.flow.StateFlow<T>
