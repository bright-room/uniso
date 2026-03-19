package net.brightroom.uniso.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.brightroom.uniso.ui.content.MainContentArea
import net.brightroom.uniso.ui.sidebar.Sidebar
import net.brightroom.uniso.ui.sidebar.dummyAccounts
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun MainLayout(modifier: Modifier = Modifier) {
    val colors = AppColors.current
    var activeAccountId by remember { mutableStateOf(dummyAccounts.firstOrNull()?.id.orEmpty()) }
    val activeAccount = dummyAccounts.find { it.id == activeAccountId }

    Row(modifier = modifier.fillMaxSize()) {
        // Sidebar
        Sidebar(
            accounts = dummyAccounts,
            activeAccountId = activeAccountId,
            onAccountClick = { account -> activeAccountId = account.id },
            onAddAccountClick = { /* Will be connected in Step 7 */ },
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
