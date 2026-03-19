package net.brightroom.uniso.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun MainContentArea(
    activeAccount: SidebarAccount?,
    webViewReady: Boolean,
    webViewVisible: Boolean,
    webViewContent: @Composable (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current

    Column(modifier = modifier.fillMaxSize()) {
        if (activeAccount != null) {
            // Header bar
            WebViewHeaderBar(account = activeAccount)

            // Separator between header and content
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(Dimensions.BorderWidthThin)
                        .background(colors.borderTertiary),
            )

            if (webViewReady && activeAccount.url.isNotBlank()) {
                // Keep WebView in composition tree to preserve session state.
                // When overlays are active, shrink to 0 to avoid z-ordering issues
                // with the native CEF heavyweight component, then show placeholder on top.
                Box(
                    modifier =
                        if (webViewVisible) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.size(0.dp)
                        },
                ) {
                    webViewContent(activeAccount.url)
                }
                if (!webViewVisible) {
                    ContentPlaceholder(account = activeAccount)
                }
            } else {
                ContentPlaceholder(account = activeAccount)
            }
        }
    }
}
