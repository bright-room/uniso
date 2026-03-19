package net.brightroom.uniso.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.ui.content.MainContentArea
import net.brightroom.uniso.ui.dialogs.AddAccountDialog
import net.brightroom.uniso.ui.dialogs.DeleteAccountDialog
import net.brightroom.uniso.ui.sidebar.Sidebar
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.sidebar.SidebarContextMenu
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun MainLayout(
    viewModel: SidebarViewModel,
    webViewReady: Boolean = false,
    webViewContent: @Composable (url: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current
    val accounts by viewModel.sidebarAccounts.collectAsState()
    val activeAccountId by viewModel.activeAccountId.collectAsState()
    val activeAccount = accounts.find { it.accountId == activeAccountId }
    val showAddDialog by viewModel.showAddAccountDialog.collectAsState()
    val deleteTarget by viewModel.deleteTargetAccount.collectAsState()
    val contextMenuTarget by viewModel.contextMenuTargetAccount.collectAsState()

    // Shrink WebView to 0 when any overlay is active to avoid z-ordering issues
    // with the native CEF heavyweight component. The WebView stays in the composition
    // tree so its session state is preserved (no reload).
    val overlayActive = showAddDialog || deleteTarget != null || contextMenuTarget != null

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar
            Sidebar(
                accounts = accounts,
                activeAccountId = activeAccountId.orEmpty(),
                onAccountClick = { account -> viewModel.onAccountClick(account.accountId) },
                onAddAccountClick = { viewModel.onAddAccountClick() },
                onAccountRightClick = { account -> viewModel.showContextMenu(account) },
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
                webViewReady = webViewReady,
                webViewVisible = !overlayActive,
                webViewContent = webViewContent,
            )
        }

        // Context Menu
        contextMenuTarget?.let { target ->
            ContextMenuOverlay(
                account = target,
                onDeleteClick = { viewModel.requestDeleteAccount(target) },
                onDismiss = { viewModel.dismissContextMenu() },
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

@Composable
private fun ContextMenuOverlay(
    account: SidebarAccount,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Full-screen scrim to dismiss on outside click
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
    ) {
        // Position context menu near the sidebar
        Box(
            modifier =
                Modifier
                    .padding(start = Dimensions.SidebarWidth + Dimensions.BorderWidthThin)
                    .padding(top = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
        ) {
            SidebarContextMenu(
                account = account,
                onDeleteClick = onDeleteClick,
            )
        }
    }
}
