package net.brightroom.uniso.domain.updater

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import net.brightroom.uniso.Constants
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger

/**
 * JVM implementation of [AutoUpdater].
 * Fetches a Sparkle-compatible Appcast XML from [appcastUrl] and compares
 * the latest version against the running application version.
 *
 * On macOS, native Sparkle integration (via JNI) can replace this implementation.
 * On Windows, WinSparkle (via JNA) can replace this implementation.
 */
class JvmAutoUpdater(
    private val appcastUrl: String,
    private val currentVersion: String = Constants.APP_VERSION,
) : AutoUpdater {
    private val logger = Logger.getLogger(JvmAutoUpdater::class.java.name)

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    override val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    override val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    override suspend fun checkForUpdates() {
        performCheck()
    }

    override suspend fun checkForUpdatesInBackground() {
        performCheck()
    }

    override fun dismissUpdate() {
        _updateInfo.value = null
    }

    private suspend fun performCheck() {
        if (_isChecking.value) return
        _isChecking.value = true

        try {
            val info = withContext(Dispatchers.IO) { fetchAndParse() }
            if (info != null && VersionComparator.isNewer(currentVersion, info.version)) {
                _updateInfo.value = info
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Update check failed", e)
        } finally {
            _isChecking.value = false
        }
    }

    private fun fetchAndParse(): UpdateInfo? {
        val xml = URI(appcastUrl).toURL().readText()
        return AppcastParser.parse(xml)
    }
}
