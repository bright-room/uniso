package net.brightroom.uniso.ui.sidebar

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun Sidebar(
    accounts: List<SidebarAccount>,
    activeAccountId: String,
    onAccountClick: (SidebarAccount) -> Unit,
    onAddAccountClick: () -> Unit,
    onAccountContextMenu: (SidebarAccount) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current
    val deleteLabel = stringResource(StringKey.CONTEXT_MENU_DELETE_ACCOUNT)

    Column(
        modifier =
            modifier
                .width(Dimensions.SidebarWidth)
                .fillMaxHeight()
                .background(colors.backgroundSecondary)
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Scrollable account list
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            var previousServiceId: String? = null
            accounts.forEach { account ->
                // Service separator
                if (previousServiceId != null && previousServiceId != account.serviceId) {
                    ServiceSeparator()
                }
                previousServiceId = account.serviceId

                ContextMenuArea(
                    items = {
                        listOf(
                            ContextMenuItem(deleteLabel) {
                                onAccountContextMenu(account)
                            },
                        )
                    },
                ) {
                    AccountItem(
                        account = account,
                        isActive = account.accountId == activeAccountId,
                        onClick = { onAccountClick(account) },
                    )
                }
            }
        }

        // Add account button (bottom-fixed)
        Spacer(modifier = Modifier.height(8.dp))
        AddAccountButton(onClick = onAddAccountClick)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ServiceSeparator() {
    Box(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .size(
                    width = Dimensions.SidebarSeparatorWidth,
                    height = Dimensions.SidebarSeparatorHeight,
                ).background(AppColors.current.borderTertiary),
    )
}
