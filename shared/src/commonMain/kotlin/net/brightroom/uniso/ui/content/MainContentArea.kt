package net.brightroom.uniso.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun MainContentArea(
    activeAccount: SidebarAccount?,
    activatedAccounts: List<SidebarAccount>,
    webViewReady: Boolean,
    webViewVisible: Boolean,
    webViewContent: @Composable (accounts: List<SidebarAccount>, activeAccountId: String?, visible: Boolean) -> Unit,
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

            if (webViewReady && activatedAccounts.isNotEmpty()) {
                // Keep all activated WebViews in composition tree to preserve session state.
                // When overlays are active, shrink all to 0 to avoid z-ordering issues
                // with the native CEF heavyweight component, then show placeholder on top.
                Box(modifier = Modifier.fillMaxSize()) {
                    webViewContent(
                        activatedAccounts,
                        activeAccount.accountId,
                        webViewVisible,
                    )
                    if (!webViewVisible) {
                        ContentPlaceholder(account = activeAccount)
                    }
                }
            } else {
                ContentPlaceholder(account = activeAccount)
            }
        }
    }
}
