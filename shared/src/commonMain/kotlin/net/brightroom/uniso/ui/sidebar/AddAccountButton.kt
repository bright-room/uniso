package net.brightroom.uniso.ui.sidebar

import androidx.compose.foundation.border
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
fun AddAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = AppColors.current

    val borderColor =
        if (isHovered) {
            colors.textTertiary
        } else {
            colors.borderSecondary
        }

    Box(
        modifier =
            modifier
                .size(Dimensions.SidebarAddButtonSize)
                .clip(CircleShape)
                .border(
                    width = Dimensions.BorderWidthDashed,
                    color = borderColor,
                    shape = CircleShape,
                ).hoverable(interactionSource)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            style =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                ),
            color = colors.textTertiary,
        )
    }
}
