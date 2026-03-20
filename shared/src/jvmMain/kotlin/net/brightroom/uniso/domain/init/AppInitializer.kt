package net.brightroom.uniso.domain.init

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.error.AppError
import net.brightroom.uniso.domain.identity.IdentityManager
import net.brightroom.uniso.domain.session.SessionManager
import net.brightroom.uniso.domain.session.SessionState
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.ui.webview.CefInitState
import net.brightroom.uniso.ui.webview.CefInitializer
import net.brightroom.uniso.ui.webview.WebViewLifecycleManager

/**
 * Represents the application initialization state machine.
 * Transitions: Loading → CefInitializing → CrashRecoveryPrompt / Ready / Error
 */
sealed class InitState {
    /** DB, i18n, accounts being loaded */
    data object Loading : InitState()

    /** CEF browser engine initializing (may include download progress) */
    data class CefInitializing(
        val cefState: CefInitState,
    ) : InitState()

    /** Crash detected — waiting for user decision */
    data class CrashRecoveryPrompt(
        val restoredSession: SessionState?,
    ) : InitState()

    /** All initialization complete, app is ready */
    data object Ready : InitState()

    /** Fatal error during initialization */
    data class Error(
        val error: AppError,
    ) : InitState()
}

/**
 * Orchestrates the application startup sequence:
 * 1. DB initialization (via AppDependencies constructor)
 * 2. Local user ID verification
 * 3. Settings & i18n loading
 * 4. Account loading
 * 5. CEF initialization (background)
 * 6. Crash recovery check
 * 7. Session restore & WebView activation
 * 8. Periodic save timer start
 */
class AppInitializer(
    private val accountManager: AccountManager,
    private val sessionManager: SessionManager,
    private val webViewLifecycleManager: WebViewLifecycleManager,
    private val identityManager: IdentityManager,
    private val i18nManager: I18nManager,
    private val cefInitializer: CefInitializer? = null,
) {
    private val _state = MutableStateFlow<InitState>(InitState.Loading)
    val state: StateFlow<InitState> = _state.asStateFlow()

    private var restoredSession: SessionState? = null
    private var needsCrashRecovery = false

    /**
     * Phase 1: Initialize core services (identity, i18n, accounts).
     * Called before entering Compose composition.
     */
    fun initializeCore() {
        try {
            // Local user ID verification
            identityManager.getOrCreateLocalUserId()

            // Settings & i18n loading
            i18nManager.initialize()

            // Account loading
            accountManager.loadAccounts()

            // Check crash recovery state
            needsCrashRecovery = !sessionManager.isCleanShutdown()
            if (needsCrashRecovery) {
                restoredSession = sessionManager.restoreSession()
            }

            // Mark startup (sets clean_shutdown = 0)
            sessionManager.markStartup()
        } catch (e: Exception) {
            _state.value = InitState.Error(AppError.DatabaseError(e))
        }
    }

    /**
     * Phase 2: Initialize CEF in background, then transition to ready/crash prompt.
     * Called inside LaunchedEffect after composition starts.
     */
    suspend fun initializeCef() {
        val initializer = cefInitializer ?: return
        _state.value = InitState.CefInitializing(CefInitState.Initializing)

        initializer.initialize()

        initializeCefWithState(initializer.initState)
    }

    /**
     * Observe a CefInitState flow and transition this initializer's state accordingly.
     * Exposed for testing with a fake flow.
     */
    internal suspend fun initializeCefWithState(cefStateFlow: StateFlow<CefInitState>) {
        cefStateFlow.collect { cefState ->
            when (cefState) {
                is CefInitState.Ready -> {
                    if (needsCrashRecovery) {
                        _state.value = InitState.CrashRecoveryPrompt(restoredSession)
                    } else {
                        restoreNormalSession()
                        _state.value = InitState.Ready
                    }
                    return@collect
                }

                is CefInitState.Error -> {
                    _state.value =
                        InitState.Error(
                            AppError.CefInitFailed(RuntimeException(cefState.message)),
                        )
                    return@collect
                }

                is CefInitState.RestartRequired -> {
                    _state.value =
                        InitState.Error(
                            AppError.CefInitFailed(RuntimeException("Application restart required")),
                        )
                    return@collect
                }

                else -> {
                    _state.value = InitState.CefInitializing(cefState)
                }
            }
        }
    }

    /**
     * Called when user chooses to restore after crash.
     */
    fun onCrashRestore() {
        val activeId = restoredSession?.activeAccountId
        if (activeId != null) {
            accountManager.setActiveAccount(activeId)
        }
        _state.value = InitState.Ready
    }

    /**
     * Called when user chooses to start fresh after crash.
     */
    fun onCrashStartNew() {
        _state.value = InitState.Ready
    }

    /**
     * Start periodic session save and suspend timer.
     * Called once the app reaches Ready state.
     */
    fun startBackgroundTasks(scope: CoroutineScope) {
        sessionManager.startPeriodicSave(scope)
        webViewLifecycleManager.startSuspendTimer(scope)
    }

    private fun restoreNormalSession() {
        val session = sessionManager.restoreSession()
        val activeId = session?.activeAccountId
        if (activeId != null) {
            accountManager.setActiveAccount(activeId)
        }
    }
}
