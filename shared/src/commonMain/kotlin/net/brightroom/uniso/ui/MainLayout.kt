package net.brightroom.uniso.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import net.brightroom.uniso.ui.content.MainContentArea
import net.brightroom.uniso.ui.sidebar.Sidebar
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun MainLayout(
    viewModel: SidebarViewModel,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current
    val accounts by viewModel.sidebarAccounts.collectAsState()
    val activeAccountId by viewModel.activeAccountId.collectAsState()
    val activeAccount = accounts.find { it.accountId == activeAccountId }

    Row(modifier = modifier.fillMaxSize()) {
        // Sidebar
        Sidebar(
            accounts = accounts,
            activeAccountId = activeAccountId.orEmpty(),
            onAccountClick = { account -> viewModel.onAccountClick(account.accountId) },
            onAddAccountClick = { viewModel.onAddAccountClick() },
        )

        // Vertical separator between sidebar and main area
        Box(
            modifier =
                Modifier
                    .width(Dimensions.BorderWidthThin)
                    .fillMaxHeight()
                    .background(colors.borderTertiary),
        )

        // Main content area
        MainContentArea(activeAccount = activeAccount)
    }
}
