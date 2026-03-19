package net.brightroom.uniso.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun UpdateDialog(
    version: String,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismiss = onDismiss,
        maxWidth = Dimensions.DialogMaxWidthSm,
    ) {
        val colors = AppColors.current

        // Title
        Text(
            text = stringResource(StringKey.DIALOG_UPDATE_TITLE),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Version info
        Text(
            text = stringResource(StringKey.DIALOG_UPDATE_MESSAGE).format(version),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DialogTextButton(
                text = stringResource(StringKey.BUTTON_LATER),
                onClick = onDismiss,
            )

            Spacer(modifier = Modifier.width(8.dp))

            DialogPrimaryButton(
                text = stringResource(StringKey.BUTTON_UPDATE_NOW),
                onClick = onUpdate,
            )
        }
    }
}
