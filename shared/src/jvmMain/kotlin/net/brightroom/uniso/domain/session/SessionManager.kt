package net.brightroom.uniso.domain.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.brightroom.uniso.data.model.AccountState
import net.brightroom.uniso.data.model.AppState
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.ui.webview.WebViewLifecycleManager
import java.time.Instant

class SessionManager(
    private val sessionRepository: SessionRepository,
    private val accountManager: AccountManager,
    private val webViewLifecycleManager: WebViewLifecycleManager,
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
) {
    private var periodicSaveJob: Job? = null

    fun startPeriodicSave(
        scope: CoroutineScope,
        intervalMs: Long = DEFAULT_SAVE_INTERVAL_MS,
    ) {
        periodicSaveJob?.cancel()
        periodicSaveJob =
            scope.launch {
                while (isActive) {
                    delay(intervalMs)
                    saveImmediate()
                }
            }
    }

    fun stopPeriodicSave() {
        periodicSaveJob?.cancel()
        periodicSaveJob = null
    }

    fun saveImmediate() {
        val now = Instant.ofEpochMilli(currentTimeMs()).toString()

        // Save state for all activated WebViews
        val activatedIds = webViewLifecycleManager.activatedAccountIds.value
        for (accountId in activatedIds) {
            val url = webViewLifecycleManager.getAccountUrl(accountId)
            val status =
                if (webViewLifecycleManager.isInBackground(accountId)) {
                    WebViewLifecycleManager.WEBVIEW_STATUS_BACKGROUND
                } else {
                    WebViewLifecycleManager.WEBVIEW_STATUS_ACTIVE
                }
            sessionRepository.saveAccountState(
                AccountState(
                    accountId = accountId,
                    lastUrl = url,
                    scrollPositionY = 0,
                    webviewStatus = status,
                    lastAccessedAt = now,
                ),
            )
        }

        // Save app state
        sessionRepository.saveAppState(
            AppState(
                activeAccountId = accountManager.activeAccountId.value,
                cleanShutdown = false,
                lastSavedAt = now,
            ),
        )
    }

    fun restoreSession(): SessionState? {
        val appState = sessionRepository.getAppState() ?: return null
        val accountStates = sessionRepository.getAllAccountStates()

        return SessionState(
            activeAccountId = appState.activeAccountId,
            accountStates =
                accountStates.map { state ->
                    AccountSessionState(
                        accountId = state.accountId,
                        lastUrl = state.lastUrl,
                        scrollPositionY = state.scrollPositionY,
                        webViewStatus = state.webviewStatus,
                    )
                },
            wasCleanShutdown = appState.cleanShutdown,
        )
    }

    fun markCleanShutdown() {
        val now = Instant.ofEpochMilli(currentTimeMs()).toString()
        sessionRepository.saveAppState(
            AppState(
                activeAccountId = accountManager.activeAccountId.value,
                cleanShutdown = true,
                lastSavedAt = now,
            ),
        )
    }

    fun markStartup() {
        val now = Instant.ofEpochMilli(currentTimeMs()).toString()
        val current = sessionRepository.getAppState()
        sessionRepository.saveAppState(
            AppState(
                activeAccountId = current?.activeAccountId,
                cleanShutdown = false,
                lastSavedAt = now,
            ),
        )
    }

    fun isCleanShutdown(): Boolean {
        val appState = sessionRepository.getAppState() ?: return true
        return appState.cleanShutdown
    }

    companion object {
        const val DEFAULT_SAVE_INTERVAL_MS = 30_000L
    }
}
