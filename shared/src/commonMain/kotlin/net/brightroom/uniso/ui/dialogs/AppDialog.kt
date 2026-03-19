package net.brightroom.uniso.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun AppDialog(
    onDismiss: () -> Unit,
    maxWidth: Dp = Dimensions.DialogMaxWidthMd,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppColors.current

    // Scrim overlay
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        contentAlignment = Alignment.Center,
    ) {
        // Dialog card
        Column(
            modifier =
                Modifier
                    .widthIn(max = maxWidth)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ).shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(Dimensions.BorderRadiusLg),
                    ).clip(RoundedCornerShape(Dimensions.BorderRadiusLg))
                    .background(colors.backgroundPrimary)
                    .padding(24.dp),
            content = content,
        )
    }
}

@Composable
fun DialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AppColors.current.textSecondary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = AppColors.current
    val bgColor = if (isHovered) colors.backgroundTertiary else Color.Transparent

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(bgColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
        )
    }
}

@Composable
fun DialogPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.current.textPrimary,
    contentColor: Color = AppColors.current.backgroundPrimary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val alpha = if (isHovered) 0.85f else 1f

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(backgroundColor.copy(alpha = alpha))
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}
