package net.brightroom.uniso.ui.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import net.brightroom.uniso.Constants
import net.brightroom.uniso.ui.theme.AppColors

@Composable
fun SplashScreen(
    initState: CefInitState,
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
            // App icon placeholder
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.textInfo),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "U",
                    style =
                        TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Constants.APP_NAME,
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                color = colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (initState) {
                is CefInitState.Downloading -> {
                    LinearProgressIndicator(
                        progress = { initState.progress / 100f },
                        modifier = Modifier.width(200.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Downloading browser engine... ${initState.progress.toInt()}%",
                        style = TextStyle(fontSize = 12.sp),
                        color = colors.textSecondary,
                    )
                }

                is CefInitState.Initializing, is CefInitState.NotStarted -> {
                    LinearProgressIndicator(
                        modifier = Modifier.width(200.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Initializing...",
                        style = TextStyle(fontSize = 12.sp),
                        color = colors.textSecondary,
                    )
                }

                is CefInitState.Error -> {
                    Text(
                        text = initState.message,
                        style = TextStyle(fontSize = 12.sp),
                        color = colors.textDanger,
                    )
                }

                is CefInitState.RestartRequired -> {
                    Text(
                        text = "Application restart required.",
                        style = TextStyle(fontSize = 12.sp),
                        color = colors.textWarning,
                    )
                }

                is CefInitState.Ready -> {}
            }
        }
    }
}
