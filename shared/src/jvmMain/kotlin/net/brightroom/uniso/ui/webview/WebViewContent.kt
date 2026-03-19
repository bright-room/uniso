package net.brightroom.uniso.ui.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import net.brightroom.uniso.domain.link.LinkClassification
import net.brightroom.uniso.domain.link.LinkRouter
import net.brightroom.uniso.platform.ExternalBrowserLauncher
import net.brightroom.uniso.ui.sidebar.SidebarAccount

/**
 * Registry that tracks WebViewNavigator instances per account for reload support.
 */
class WebViewNavigatorRegistry {
    private val navigators = mutableMapOf<String, WebViewNavigator>()

    fun register(accountId: String, navigator: WebViewNavigator) {
        navigators[accountId] = navigator
    }

    fun unregister(accountId: String) {
        navigators.remove(accountId)
    }

    fun reload(accountId: String) {
        navigators[accountId]?.reload()
    }

    fun forceReload(accountId: String) {
        // WebViewNavigator doesn't expose reloadIgnoringCache directly,
        // so we use reload which is the available API
        navigators[accountId]?.reload()
    }
}

/**
 * Renders per-account WebView instances with session isolation.
 *
 * Each account gets its own WebView composable (keyed by accountId) with an independent
 * CefRequestContext, providing isolated cookie and storage per account.
 * Only the active account's WebView is displayed; others are kept in the composition
 * tree at size(0.dp) to preserve their session state.
 *
 * @param accounts List of accounts that have been activated (should have WebViews).
 * @param activeAccountId The currently active account ID.
 * @param visible Whether WebViews should be visible (false when dialogs are active for z-ordering).
 * @param linkRouter Optional LinkRouter for intercepting navigation events.
 * @param navigatorRegistry Optional registry for tracking WebView navigators (enables reload).
 * @param onUrlChanged Callback invoked when a WebView navigates to a new URL.
 * @param onAccountSwitch Callback invoked when link routing decides to switch to another account.
 * @param onShowAccountSelector Callback invoked when link routing needs to show account selection dialog.
 */
@Composable
fun WebViewPanel(
    accounts: List<SidebarAccount>,
    activeAccountId: String?,
    visible: Boolean,
    linkRouter: LinkRouter? = null,
    navigatorRegistry: WebViewNavigatorRegistry? = null,
    onUrlChanged: ((accountId: String, url: String) -> Unit)? = null,
    onAccountSwitch: ((accountId: String, url: String) -> Unit)? = null,
    onShowAccountSelector: ((LinkClassification.InternalMultiAccount) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        accounts.forEach { account ->
            key(account.accountId) {
                val isActive = account.accountId == activeAccountId
                val shouldShow = isActive && visible

                Box(
                    modifier = if (shouldShow) Modifier.fillMaxSize() else Modifier.size(0.dp),
                ) {
                    AccountWebView(
                        accountId = account.accountId,
                        url = account.url,
                        linkRouter = linkRouter,
                        navigatorRegistry = navigatorRegistry,
                        onUrlChanged =
                            onUrlChanged?.let { callback ->
                                { url -> callback(account.accountId, url) }
                            },
                        onAccountSwitch = onAccountSwitch,
                        onShowAccountSelector = onShowAccountSelector,
                    )
                }
            }
        }
    }
}

/**
 * A single WebView instance for one account.
 * Each instance has its own WebViewState and CefRequestContext (created by the library),
 * providing cookie and storage isolation.
 */
@Composable
private fun AccountWebView(
    accountId: String,
    url: String,
    linkRouter: LinkRouter? = null,
    navigatorRegistry: WebViewNavigatorRegistry? = null,
    onUrlChanged: ((String) -> Unit)? = null,
    onAccountSwitch: ((accountId: String, url: String) -> Unit)? = null,
    onShowAccountSelector: ((LinkClassification.InternalMultiAccount) -> Unit)? = null,
) {
    val state = rememberWebViewState(url)

    val interceptor =
        remember(accountId, linkRouter) {
            if (linkRouter != null) {
                createRequestInterceptor(
                    linkRouter = linkRouter,
                    accountId = accountId,
                    onAccountSwitch = onAccountSwitch,
                    onShowAccountSelector = onShowAccountSelector,
                )
            } else {
                null
            }
        }

    val navigator = rememberWebViewNavigator(requestInterceptor = interceptor)

    // Register navigator for reload support
    if (navigatorRegistry != null) {
        DisposableEffect(accountId, navigator) {
            navigatorRegistry.register(accountId, navigator)
            onDispose {
                navigatorRegistry.unregister(accountId)
            }
        }
    }

    if (onUrlChanged != null) {
        LaunchedEffect(state) {
            snapshotFlow { state.lastLoadedUrl }
                .collect { loadedUrl ->
                    if (!loadedUrl.isNullOrBlank()) {
                        onUrlChanged(loadedUrl)
                    }
                }
        }
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = Modifier.fillMaxSize(),
    )
}

private fun createRequestInterceptor(
    linkRouter: LinkRouter,
    accountId: String,
    onAccountSwitch: ((accountId: String, url: String) -> Unit)?,
    onShowAccountSelector: ((LinkClassification.InternalMultiAccount) -> Unit)?,
): RequestInterceptor =
    object : RequestInterceptor {
        override fun onInterceptUrlRequest(
            request: WebRequest,
            navigator: WebViewNavigator,
        ): WebRequestInterceptResult {
            // Only intercept main frame navigations
            if (!request.isForMainFrame) return WebRequestInterceptResult.Allow

            val shouldCancel =
                linkRouter.handleNavigation(
                    url = request.url,
                    sourceAccountId = accountId,
                    onExternalLink = { url -> ExternalBrowserLauncher.open(url) },
                    onSwitchAccount = { targetAccountId, url ->
                        onAccountSwitch?.invoke(targetAccountId, url)
                    },
                    onShowAccountSelector = { classification ->
                        onShowAccountSelector?.invoke(classification)
                    },
                )

            return if (shouldCancel) {
                WebRequestInterceptResult.Reject
            } else {
                WebRequestInterceptResult.Allow
            }
        }
    }
