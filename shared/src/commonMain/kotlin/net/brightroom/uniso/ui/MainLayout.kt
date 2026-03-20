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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.brightroom.uniso.domain.link.LinkClassification
import net.brightroom.uniso.ui.content.MainContentArea
import net.brightroom.uniso.ui.dialogs.AccountSelectDialog
import net.brightroom.uniso.ui.dialogs.AddAccountDialog
import net.brightroom.uniso.ui.dialogs.DeleteAccountDialog
import net.brightroom.uniso.ui.settings.SettingsScreen
import net.brightroom.uniso.ui.settings.SettingsViewModel
import net.brightroom.uniso.ui.sidebar.Sidebar
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

sealed class MainScreen {
    data object WebView : MainScreen()

    data object Settings : MainScreen()
}

@Composable
fun MainLayout(
    viewModel: SidebarViewModel,
    settingsViewModel: SettingsViewModel,
    activatedAccounts: List<SidebarAccount>,
    currentScreen: MainScreen = MainScreen.WebView,
    onScreenChange: (MainScreen) -> Unit = {},
    webViewReady: Boolean = false,
    webViewContent: @Composable (accounts: List<SidebarAccount>, activeAccountId: String?, visible: Boolean) -> Unit = { _, _, _ -> },
    onWebViewCleanup: (String) -> Unit = {},
    onShowTutorial: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current
    val accounts by viewModel.sidebarAccounts.collectAsState()
    val activeAccountId by viewModel.activeAccountId.collectAsState()
    val activeAccount = accounts.find { it.accountId == activeAccountId }
    val showAddDialog by viewModel.showAddAccountDialog.collectAsState()
    val deleteTarget by viewModel.deleteTargetAccount.collectAsState()
    val settingsDeleteTarget by settingsViewModel.deleteTarget.collectAsState()

    // Account selection dialog state for link handling
    var accountSelectState by remember {
        mutableStateOf<LinkClassification.InternalMultiAccount?>(null)
    }

    // Shrink WebView to 0 when any overlay is active to avoid z-ordering issues
    // with the native CEF heavyweight component. The WebView stays in the composition
    // tree so its session state is preserved (no reload).
    val overlayActive =
        showAddDialog || deleteTarget != null || settingsDeleteTarget != null ||
            accountSelectState != null || currentScreen is MainScreen.Settings

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar
            Sidebar(
                accounts = accounts,
                activeAccountId = activeAccountId.orEmpty(),
                onAccountClick = {
                    onScreenChange(MainScreen.WebView)
                    viewModel.onAccountClick(it.accountId)
                },
                onAddAccountClick = { viewModel.onAddAccountClick() },
                isSettingsActive = currentScreen is MainScreen.Settings,
                onSettingsClick = {
                    if (currentScreen is MainScreen.Settings) {
                        onScreenChange(MainScreen.WebView)
                    } else {
                        onScreenChange(MainScreen.Settings)
                    }
                },
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
            when (currentScreen) {
                is MainScreen.Settings -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onClose = { onScreenChange(MainScreen.WebView) },
                        onWebViewCleanup = onWebViewCleanup,
                        onShowTutorial = onShowTutorial,
                        onCheckForUpdates = onCheckForUpdates,
                    )
                }

                is MainScreen.WebView -> {
                    MainContentArea(
                        activeAccount = activeAccount,
                        activatedAccounts = activatedAccounts,
                        webViewReady = webViewReady,
                        webViewVisible = !overlayActive,
                        webViewContent = webViewContent,
                    )
                }
            }
        }

        // Add Account Dialog
        if (showAddDialog) {
            AddAccountDialog(
                services = viewModel.getAvailableServices(),
                onServiceSelected = { service ->
                    viewModel.addAccount(service.serviceId)
                    onScreenChange(MainScreen.WebView)
                },
                onDismiss = { viewModel.dismissAddAccountDialog() },
            )
        }

        // Delete Account Confirmation Dialog (from sidebar)
        deleteTarget?.let { target ->
            DeleteAccountDialog(
                account = target,
                onConfirm = { viewModel.confirmDeleteAccount() },
                onDismiss = { viewModel.dismissDeleteDialog() },
            )
        }

        // Delete Account Confirmation Dialog (from settings)
        settingsDeleteTarget?.let { target ->
            DeleteAccountDialog(
                accountName = target.accountName,
                serviceName = target.serviceName,
                brandColor = target.brandColor,
                onConfirm = { settingsViewModel.confirmDeleteAccount(onWebViewCleanup) },
                onDismiss = { settingsViewModel.dismissDeleteDialog() },
            )
        }

        // Account Selection Dialog (from link handling)
        accountSelectState?.let { classification ->
            val selectableAccounts =
                classification.accounts.mapNotNull { account ->
                    accounts.find { it.accountId == account.accountId }
                }
            if (selectableAccounts.isNotEmpty()) {
                AccountSelectDialog(
                    accounts = selectableAccounts,
                    onAccountSelected = { selected ->
                        viewModel.onAccountClick(selected.accountId)
                        accountSelectState = null
                    },
                    onOpenExternal = {
                        viewModel.openExternalBrowser(classification.url)
                        accountSelectState = null
                    },
                    onDismiss = { accountSelectState = null },
                )
            }
        }
    }
}
