package net.brightroom.uniso.ui.webview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.brightroom.uniso.data.model.AccountState
import net.brightroom.uniso.platform.PlatformPaths
import java.io.File
import java.time.Instant

/**
 * Manages WebView lifecycle for per-account session isolation with lazy destruction.
 *
 * Each account that has been "activated" (switched to at least once) gets its own
 * WebView instance in the composition tree with an independent CefRequestContext,
 * providing cookie and storage isolation between accounts.
 *
 * Background WebViews are tracked in an LRU queue. When the queue exceeds
 * [maxBackgroundCount], the least recently used WebView is suspended (state saved,
 * WebView removed from composition). A periodic timer also suspends WebViews
 * that have been in the background longer than [suspendTimeoutMs].
 *
 * Suspended WebViews retain their CEF profile directory (cookies, storage),
 * so re-activation restores the logged-in session without requiring re-login.
 */
class WebViewLifecycleManager(
    private val platformPaths: PlatformPaths,
    private val accountStateSaver: (AccountState) -> Unit = {},
    private val maxBackgroundCount: Int = DEFAULT_MAX_BACKGROUND_COUNT,
    private val suspendTimeoutMs: Long = DEFAULT_SUSPEND_TIMEOUT_MS,
    private val checkIntervalMs: Long = DEFAULT_CHECK_INTERVAL_MS,
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
) {
    private val _activatedAccountIds = MutableStateFlow<Set<String>>(emptySet())
    val activatedAccountIds: StateFlow<Set<String>> = _activatedAccountIds.asStateFlow()

    /** LRU-ordered background queue: accountId -> timestamp when moved to background (ms). */
    private val backgroundQueue = LinkedHashMap<String, Long>()

    /** Tracks last known URL per account for state preservation on suspend. */
    private val accountUrls = mutableMapOf<String, String>()

    private var suspendTimerJob: Job? = null

    /**
     * Marks an account as activated, ensuring a WebView will be created for it.
     * Creates the per-account profile directory if it doesn't exist.
     * Removes the account from background queue if it was there.
     */
    fun activateWebView(accountId: String) {
        ensureProfileDir(accountId)
        backgroundQueue.remove(accountId)
        _activatedAccountIds.value = _activatedAccountIds.value + accountId
    }

    /**
     * Handles an account switch: activates the new account and moves the previous one
     * to the background queue with LRU eviction.
     */
    fun onAccountSwitched(
        fromAccountId: String?,
        toAccountId: String,
    ) {
        activateWebView(toAccountId)

        if (fromAccountId != null &&
            fromAccountId != toAccountId &&
            fromAccountId in _activatedAccountIds.value
        ) {
            moveToBackground(fromAccountId)
        }
    }

    /**
     * Suspends a WebView: saves its state via [accountStateSaver], then removes it
     * from the activated set. The profile directory is preserved so the session
     * (cookies, storage) persists for future re-activation.
     */
    fun suspendWebView(accountId: String) {
        val url = accountUrls[accountId]
        accountStateSaver(
            AccountState(
                accountId = accountId,
                lastUrl = url,
                scrollPositionY = 0,
                webviewStatus = WEBVIEW_STATUS_SUSPENDED,
                lastAccessedAt = Instant.ofEpochMilli(currentTimeMs()).toString(),
            ),
        )

        _activatedAccountIds.value = _activatedAccountIds.value - accountId
        backgroundQueue.remove(accountId)
        accountUrls.remove(accountId)
    }

    /**
     * Completely destroys a WebView and its associated profile data.
     * Used when an account is deleted.
     */
    fun destroyWebView(accountId: String) {
        _activatedAccountIds.value = _activatedAccountIds.value - accountId
        backgroundQueue.remove(accountId)
        accountUrls.remove(accountId)
        deleteProfileDir(accountId)
    }

    /**
     * Destroys all WebView instances. Used during app shutdown.
     */
    fun destroyAll() {
        stopSuspendTimer()
        _activatedAccountIds.value = emptySet()
        backgroundQueue.clear()
        accountUrls.clear()
    }

    /**
     * Evicts the least recently used account from the background queue by suspending it.
     */
    fun evictLru() {
        val oldest = backgroundQueue.entries.firstOrNull() ?: return
        suspendWebView(oldest.key)
    }

    /**
     * Starts a periodic timer that checks for background WebViews exceeding
     * the suspend timeout and suspends them.
     */
    fun startSuspendTimer(scope: CoroutineScope) {
        suspendTimerJob?.cancel()
        suspendTimerJob =
            scope.launch {
                while (isActive) {
                    delay(checkIntervalMs)
                    checkTimeouts()
                }
            }
    }

    /**
     * Stops the periodic suspend timer.
     */
    fun stopSuspendTimer() {
        suspendTimerJob?.cancel()
        suspendTimerJob = null
    }

    /**
     * Updates the last known URL for an account. Called when a WebView navigates.
     */
    fun updateAccountUrl(
        accountId: String,
        url: String,
    ) {
        accountUrls[accountId] = url
    }

    /**
     * Returns the last known URL for an account, or null if not tracked.
     */
    fun getAccountUrl(accountId: String): String? = accountUrls[accountId]

    /**
     * Returns the profile directory path for an account.
     */
    fun getProfilePath(accountId: String): String = platformPaths.getCefProfileDir(accountId)

    /**
     * Returns the number of currently activated WebViews.
     */
    fun getActiveCount(): Int = _activatedAccountIds.value.size

    /**
     * Returns the number of WebViews in the background queue.
     */
    fun getBackgroundCount(): Int = backgroundQueue.size

    /**
     * Returns whether the given account is in the background queue.
     */
    fun isInBackground(accountId: String): Boolean = accountId in backgroundQueue

    private fun moveToBackground(accountId: String) {
        backgroundQueue.remove(accountId)
        backgroundQueue[accountId] = currentTimeMs()

        while (backgroundQueue.size > maxBackgroundCount) {
            evictLru()
        }
    }

    internal fun checkTimeouts() {
        val now = currentTimeMs()
        val expired =
            backgroundQueue.entries
                .filter { now - it.value >= suspendTimeoutMs }
                .map { it.key }
        expired.forEach { suspendWebView(it) }
    }

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

    companion object {
        const val DEFAULT_MAX_BACKGROUND_COUNT = 3
        const val DEFAULT_SUSPEND_TIMEOUT_MS = 300_000L // 5 minutes
        const val DEFAULT_CHECK_INTERVAL_MS = 60_000L // 1 minute

        const val WEBVIEW_STATUS_ACTIVE = "active"
        const val WEBVIEW_STATUS_BACKGROUND = "background"
        const val WEBVIEW_STATUS_SUSPENDED = "suspended"
        const val WEBVIEW_STATUS_DESTROYED = "destroyed"
    }
}
