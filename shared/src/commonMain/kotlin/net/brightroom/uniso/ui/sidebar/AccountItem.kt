package net.brightroom.uniso.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun AccountItem(
    account: SidebarAccount,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = AppColors.current

    val backgroundColor =
        when {
            isActive -> colors.backgroundPrimary
            isHovered -> colors.backgroundTertiary
            else -> Color.Transparent
        }

    Box(
        modifier =
            modifier
                .size(Dimensions.SidebarAccountItemSize)
                .clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                .background(backgroundColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Active indicator (left edge bar)
        if (isActive) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .width(Dimensions.SidebarActiveIndicatorWidth)
                        .fillMaxHeight(0.5f)
                        .clip(
                            RoundedCornerShape(
                                topEnd = 2.dp,
                                bottomEnd = 2.dp,
                            ),
                        ).background(colors.textPrimary),
            )
        }

        // Icon + Avatar column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            // Service icon (24x24, rounded 6px, brand color bg)
            Box(
                modifier =
                    Modifier
                        .size(Dimensions.ServiceIconSm)
                        .clip(RoundedCornerShape(6.dp))
                        .background(account.brandColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.serviceName.first().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }

            // Avatar initials (14x14, circle)
            Box(
                modifier =
                    Modifier
                        .size(Dimensions.AvatarSm)
                        .clip(CircleShape)
                        .background(account.brandColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.initials,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}
