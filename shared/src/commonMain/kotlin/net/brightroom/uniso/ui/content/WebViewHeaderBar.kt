package net.brightroom.uniso.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.brightroom.uniso.ui.sidebar.DummyAccount
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun WebViewHeaderBar(
    account: DummyAccount,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(Dimensions.HeaderHeight)
                .background(colors.backgroundPrimary)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Service icon (18x18, rounded 4px)
        Box(
            modifier =
                Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                    .background(account.brandColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = account.serviceName.first().toString(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }

        // Service name — Account name
        Text(
            text = "${account.serviceName} — ${account.accountName}",
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.weight(1f))

        // URL display (right-aligned)
        Text(
            text = account.url,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
