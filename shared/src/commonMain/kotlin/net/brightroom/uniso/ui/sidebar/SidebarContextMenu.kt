package net.brightroom.uniso.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun SidebarContextMenu(
    account: SidebarAccount,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current

    Column(
        modifier =
            modifier
                .widthIn(min = Dimensions.ContextMenuMinWidth)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(Dimensions.BorderRadiusMd),
                ).clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                .background(colors.backgroundPrimary)
                .padding(vertical = 4.dp),
    ) {
        // Header: service name — account name
        Text(
            text = "${account.serviceName} — ${account.accountName}",
            style = MaterialTheme.typography.titleSmall,
            color = colors.textTertiary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )

        // Separator
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(Dimensions.BorderWidthThin)
                    .background(colors.borderTertiary),
        )

        // Delete account
        ContextMenuItem(
            text = stringResource(StringKey.CONTEXT_MENU_DELETE_ACCOUNT),
            onClick = onDeleteClick,
            textColor = colors.textDanger,
        )
    }
}

@Composable
private fun ContextMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = AppColors.current.textPrimary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = AppColors.current
    val bgColor = if (isHovered) colors.backgroundSecondary else Color.Transparent

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(bgColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
        )
    }
}
