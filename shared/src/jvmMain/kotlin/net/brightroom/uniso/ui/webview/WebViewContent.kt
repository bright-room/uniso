package net.brightroom.uniso.ui.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import net.brightroom.uniso.ui.sidebar.SidebarAccount

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
 */
@Composable
fun WebViewPanel(
    accounts: List<SidebarAccount>,
    activeAccountId: String?,
    visible: Boolean,
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
                        url = account.url,
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
private fun AccountWebView(url: String) {
    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator()

    WebView(
        state = state,
        navigator = navigator,
        modifier = Modifier.fillMaxSize(),
    )
}
