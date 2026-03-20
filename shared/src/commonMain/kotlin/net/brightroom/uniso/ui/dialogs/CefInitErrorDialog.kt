package net.brightroom.uniso.ui.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.LocalI18n
import net.brightroom.uniso.ui.theme.AppColors

@Composable
fun CefInitErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
) {
    val i18n = LocalI18n.current
    val colors = AppColors.current

    AppDialog(onDismiss = onDismiss) {
        Text(
            text = i18n.getString(StringKey.ERROR_CEF_INIT_FAILED),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.textDanger,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        DialogPrimaryButton(
            text = i18n.getString(StringKey.BUTTON_CLOSE),
            onClick = onDismiss,
            backgroundColor = colors.textDanger,
            modifier = Modifier.align(Alignment.End).fillMaxWidth(),
        )
    }
}
