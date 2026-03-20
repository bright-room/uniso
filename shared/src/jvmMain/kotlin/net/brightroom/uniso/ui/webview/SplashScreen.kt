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
import net.brightroom.uniso.domain.init.InitState
import net.brightroom.uniso.ui.theme.AppColors

@Composable
fun SplashScreen(
    initState: InitState,
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
                is InitState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.width(200.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading...",
                        style = TextStyle(fontSize = 12.sp),
                        color = colors.textSecondary,
                    )
                }

                is InitState.CefInitializing -> {
                    val cefState = initState.cefState
                    when (cefState) {
                        is CefInitState.Downloading -> {
                            LinearProgressIndicator(
                                progress = { cefState.progress / 100f },
                                modifier = Modifier.width(200.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Downloading browser engine... ${cefState.progress.toInt()}%",
                                style = TextStyle(fontSize = 12.sp),
                                color = colors.textSecondary,
                            )
                        }

                        else -> {
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
                    }
                }

                is InitState.Error -> {
                    Text(
                        text = initState.error.message ?: "Unknown error",
                        style = TextStyle(fontSize = 12.sp),
                        color = colors.textDanger,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please restart the application.",
                        style = TextStyle(fontSize = 12.sp),
                        color = colors.textSecondary,
                    )
                }

                // Ready and CrashRecoveryPrompt are handled by Main.kt
                is InitState.Ready,
                is InitState.CrashRecoveryPrompt,
                -> {}
            }
        }
    }
}
