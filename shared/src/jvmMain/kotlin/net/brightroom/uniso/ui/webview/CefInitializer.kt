package net.brightroom.uniso.ui.webview

import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import net.brightroom.uniso.platform.PlatformPaths
import java.io.File

sealed class CefInitState {
    data object NotStarted : CefInitState()

    data class Downloading(
        val progress: Float,
    ) : CefInitState()

    data object Initializing : CefInitState()

    data object Ready : CefInitState()

    data class Error(
        val message: String,
    ) : CefInitState()

    data object RestartRequired : CefInitState()
}

class CefInitializer(
    private val platformPaths: PlatformPaths,
) {
    private val _initState = MutableStateFlow<CefInitState>(CefInitState.NotStarted)
    val initState: StateFlow<CefInitState> = _initState.asStateFlow()

    suspend fun initialize() {
        _initState.value = CefInitState.Initializing

        withContext(Dispatchers.IO) {
            try {
                val installDir = File(platformPaths.getAppDataDir(), "kcef-bundle")

                KCEF.init(
                    builder = {
                        installDir(installDir)
                        progress {
                            onDownloading { progress ->
                                _initState.value =
                                    CefInitState.Downloading(
                                        maxOf(progress, 0f),
                                    )
                            }
                            onInitialized {
                                _initState.value = CefInitState.Ready
                            }
                        }
                        settings {
                            cachePath = File(platformPaths.getAppDataDir(), "cef_cache").absolutePath
                            userAgent = UserAgentProvider.getUserAgent("default")
                            persistSessionCookies = true
                        }
                    },
                    onError = { error ->
                        _initState.value =
                            CefInitState.Error(
                                error?.message ?: "Unknown CEF initialization error",
                            )
                    },
                    onRestartRequired = {
                        _initState.value = CefInitState.RestartRequired
                    },
                )
            } catch (e: Exception) {
                _initState.value =
                    CefInitState.Error(
                        e.message ?: "Failed to initialize CEF",
                    )
            }
        }
    }

    fun dispose() {
        KCEF.disposeBlocking()
    }
}
