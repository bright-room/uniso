package net.brightroom.uniso.ui.webview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

@Composable
fun WebViewContent(
    url: String,
    modifier: Modifier = Modifier,
) {
    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(url) {
        navigator.loadUrl(url)
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier.fillMaxSize(),
    )
}
