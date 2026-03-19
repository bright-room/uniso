package net.brightroom.uniso.ui.webview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.brightroom.uniso.platform.PlatformPaths
import java.io.File

/**
 * Manages WebView lifecycle for per-account session isolation.
 *
 * Each account that has been "activated" (switched to at least once) gets its own
 * WebView instance in the composition tree with an independent CefRequestContext,
 * providing cookie and storage isolation between accounts.
 *
 * Profile directories are created per-account at {appDataDir}/cef_profiles/{accountId}
 * for future use when JCEF supports per-context cache_path.
 */
class WebViewLifecycleManager(
    private val platformPaths: PlatformPaths,
) {
    private val _activatedAccountIds = MutableStateFlow<Set<String>>(emptySet())
    val activatedAccountIds: StateFlow<Set<String>> = _activatedAccountIds.asStateFlow()

    /**
     * Marks an account as activated, ensuring a WebView will be created for it.
     * Creates the per-account profile directory if it doesn't exist.
     */
    fun activateWebView(accountId: String) {
        ensureProfileDir(accountId)
        _activatedAccountIds.value = _activatedAccountIds.value + accountId
    }

    /**
     * Completely destroys a WebView and its associated profile data.
     * Used when an account is deleted.
     */
    fun destroyWebView(accountId: String) {
        _activatedAccountIds.value = _activatedAccountIds.value - accountId
        deleteProfileDir(accountId)
    }

    /**
     * Destroys all WebView instances. Used during app shutdown.
     */
    fun destroyAll() {
        _activatedAccountIds.value = emptySet()
    }

    /**
     * Returns the profile directory path for an account.
     */
    fun getProfilePath(accountId: String): String = platformPaths.getCefProfileDir(accountId)

    /**
     * Returns the number of currently activated WebViews.
     */
    fun getActiveCount(): Int = _activatedAccountIds.value.size

    private fun ensureProfileDir(accountId: String) {
        val dir = File(platformPaths.getCefProfileDir(accountId))
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun deleteProfileDir(accountId: String) {
        val dir = File(platformPaths.getCefProfileDir(accountId))
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }
}
