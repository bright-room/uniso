package net.brightroom.uniso.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun AccountSelectDialog(
    accounts: List<SidebarAccount>,
    onAccountSelected: (SidebarAccount) -> Unit,
    onOpenExternal: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismiss = onDismiss,
        maxWidth = Dimensions.DialogMaxWidthSm,
    ) {
        val colors = AppColors.current

        // Service icon + Title
        if (accounts.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(Dimensions.ServiceIconSm)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accounts.first().brandColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            accounts
                                .first()
                                .serviceName
                                .first()
                                .toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(StringKey.DIALOG_ACCOUNT_SELECT_TITLE),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
            }
        } else {
            Text(
                text = stringResource(StringKey.DIALOG_ACCOUNT_SELECT_TITLE),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account list
        Column {
            accounts.forEach { account ->
                AccountSelectRow(
                    account = account,
                    onClick = { onAccountSelected(account) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Open in external browser
        DialogTextButton(
            text = stringResource(StringKey.DIALOG_ACCOUNT_SELECT_OPEN_EXTERNAL),
            onClick = onOpenExternal,
            color = colors.textInfo,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun AccountSelectRow(
    account: SidebarAccount,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = AppColors.current
    val bgColor = if (isHovered) colors.backgroundSecondary else Color.Transparent

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(bgColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        Box(
            modifier =
                Modifier
                    .size(Dimensions.AvatarMd)
                    .clip(CircleShape)
                    .background(account.brandColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = account.initials,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = account.accountName,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
        )
    }
}
