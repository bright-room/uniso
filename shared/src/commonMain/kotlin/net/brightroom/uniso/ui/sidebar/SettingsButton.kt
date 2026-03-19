package net.brightroom.uniso.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun SettingsButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = AppColors.current

    val bgColor =
        when {
            isActive -> colors.backgroundTertiary
            isHovered -> colors.backgroundTertiary.copy(alpha = 0.5f)
            else -> colors.backgroundSecondary
        }

    Box(
        modifier =
            modifier
                .size(Dimensions.SidebarAddButtonSize)
                .clip(CircleShape)
                .background(bgColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Gear icon using unicode character
        Text(
            text = "\u2699",
            style =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                ),
            color = if (isActive) colors.textPrimary else colors.textTertiary,
        )
    }
}
