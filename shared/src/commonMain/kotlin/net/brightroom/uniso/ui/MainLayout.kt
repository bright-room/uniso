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
import net.brightroom.uniso.ui.dialogs.AddAccountDialog
import net.brightroom.uniso.ui.dialogs.DeleteAccountDialog
import net.brightroom.uniso.ui.sidebar.Sidebar
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun MainLayout(
    viewModel: SidebarViewModel,
    activatedAccounts: List<SidebarAccount>,
    webViewReady: Boolean = false,
    webViewContent: @Composable (accounts: List<SidebarAccount>, activeAccountId: String?, visible: Boolean) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current
    val accounts by viewModel.sidebarAccounts.collectAsState()
    val activeAccountId by viewModel.activeAccountId.collectAsState()
    val activeAccount = accounts.find { it.accountId == activeAccountId }
    val showAddDialog by viewModel.showAddAccountDialog.collectAsState()
    val deleteTarget by viewModel.deleteTargetAccount.collectAsState()

    // Shrink WebView to 0 when any overlay is active to avoid z-ordering issues
    // with the native CEF heavyweight component. The WebView stays in the composition
    // tree so its session state is preserved (no reload).
    val overlayActive = showAddDialog || deleteTarget != null

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar
            Sidebar(
                accounts = accounts,
                activeAccountId = activeAccountId.orEmpty(),
                onAccountClick = { viewModel.onAccountClick(it.accountId) },
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
            MainContentArea(
                activeAccount = activeAccount,
                activatedAccounts = activatedAccounts,
                webViewReady = webViewReady,
                webViewVisible = !overlayActive,
                webViewContent = webViewContent,
            )
        }

        // Add Account Dialog
        if (showAddDialog) {
            AddAccountDialog(
                services = viewModel.getAvailableServices(),
                onServiceSelected = { service -> viewModel.addAccount(service.serviceId) },
                onDismiss = { viewModel.dismissAddAccountDialog() },
            )
        }

        // Delete Account Confirmation Dialog
        deleteTarget?.let { target ->
            DeleteAccountDialog(
                account = target,
                onConfirm = { viewModel.confirmDeleteAccount() },
                onDismiss = { viewModel.dismissDeleteDialog() },
            )
        }
    }
}
