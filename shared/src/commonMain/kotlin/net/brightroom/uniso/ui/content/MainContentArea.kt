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

            // Content area (placeholder for now)
            ContentPlaceholder(account = activeAccount)
        }
    }
}
