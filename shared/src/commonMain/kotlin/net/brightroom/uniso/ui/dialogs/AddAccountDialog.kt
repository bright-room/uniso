package net.brightroom.uniso.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.brightroom.uniso.domain.plugin.ServicePlugin
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun AddAccountDialog(
    services: List<ServicePlugin>,
    onServiceSelected: (ServicePlugin) -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismiss = onDismiss,
        maxWidth = Dimensions.DialogMaxWidthMd,
    ) {
        val colors = AppColors.current

        // Title
        Text(
            text = stringResource(StringKey.DIALOG_ADD_ACCOUNT_TITLE),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        Text(
            text = stringResource(StringKey.DIALOG_ADD_ACCOUNT_SUBTITLE),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3-column service grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            items(services) { service ->
                ServiceCard(
                    service = service,
                    onClick = { onServiceSelected(service) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel button
        DialogTextButton(
            text = stringResource(StringKey.BUTTON_CANCEL),
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun ServiceCard(
    service: ServicePlugin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = AppColors.current
    val brandColor = Color(service.brandColor)

    val backgroundColor = if (isHovered) colors.backgroundTertiary else colors.backgroundSecondary

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                .background(backgroundColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Service icon (36x36, rounded)
        Box(
            modifier =
                Modifier
                    .size(Dimensions.ServiceIconLg)
                    .clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                    .background(brandColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = service.displayName.first().toString(),
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Service name
        Text(
            text = service.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
        )
    }
}
