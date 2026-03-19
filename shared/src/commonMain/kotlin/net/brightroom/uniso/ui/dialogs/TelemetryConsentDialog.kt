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
fun TelemetryConsentDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit,
) {
    AppDialog(
        onDismiss = onDeny,
        maxWidth = Dimensions.DialogMaxWidthSm,
    ) {
        val colors = AppColors.current

        // Title
        Text(
            text = stringResource(StringKey.DIALOG_TELEMETRY_TITLE),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Message
        Text(
            text = stringResource(StringKey.DIALOG_TELEMETRY_MESSAGE),
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
                text = stringResource(StringKey.DIALOG_TELEMETRY_DENY),
                onClick = onDeny,
            )

            Spacer(modifier = Modifier.width(8.dp))

            DialogPrimaryButton(
                text = stringResource(StringKey.DIALOG_TELEMETRY_ALLOW),
                onClick = onAllow,
            )
        }
    }
}
