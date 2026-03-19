package net.brightroom.uniso.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import net.brightroom.uniso.ui.sidebar.SidebarAccount
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun ContentPlaceholder(
    account: SidebarAccount,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Service icon (64x64, rounded 16px)
            Box(
                modifier =
                    Modifier
                        .size(Dimensions.ServiceIconXl)
                        .clip(RoundedCornerShape(Dimensions.BorderRadiusXl))
                        .background(account.brandColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.serviceName.first().toString(),
                    style =
                        TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Service name
            Text(
                text = account.serviceName,
                style =
                    TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                color = colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Account name
            Text(
                text = account.accountName,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // URL placeholder box
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                        .background(colors.backgroundSecondary)
                        .padding(16.dp),
            ) {
                Text(
                    text = account.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}
