package net.brightroom.uniso.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun DeleteAccountDialog(
    accountName: String,
    serviceName: String,
    brandColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DeleteAccountDialogContent(
        accountName = accountName,
        serviceName = serviceName,
        brandColor = brandColor,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun DeleteAccountDialog(
    account: SidebarAccount,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DeleteAccountDialogContent(
        accountName = account.accountName,
        serviceName = account.serviceName,
        brandColor = account.brandColor,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun DeleteAccountDialogContent(
    accountName: String,
    serviceName: String,
    brandColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismiss = onDismiss,
        maxWidth = Dimensions.DialogMaxWidthSm,
    ) {
        val colors = AppColors.current

        // Trash icon
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                    .background(colors.backgroundDanger)
                    .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\uD83D\uDDD1",
                style = TextStyle(fontSize = 20.sp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = stringResource(StringKey.DIALOG_DELETE_TITLE),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm message
        Text(
            text = stringResource(StringKey.DIALOG_DELETE_CONFIRM),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Account info card
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                    .background(colors.backgroundSecondary)
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Service icon
            Box(
                modifier =
                    Modifier
                        .size(Dimensions.ServiceIconSm)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brandColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = serviceName.first().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = serviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                )
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Warning
        Text(
            text = stringResource(StringKey.DIALOG_DELETE_WARNING),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textDanger,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DialogTextButton(
                text = stringResource(StringKey.BUTTON_CANCEL),
                onClick = onDismiss,
            )

            Spacer(modifier = Modifier.width(8.dp))

            DialogPrimaryButton(
                text = stringResource(StringKey.BUTTON_DELETE),
                onClick = onConfirm,
                backgroundColor = colors.textDanger,
                contentColor = Color.White,
            )
        }
    }
}
