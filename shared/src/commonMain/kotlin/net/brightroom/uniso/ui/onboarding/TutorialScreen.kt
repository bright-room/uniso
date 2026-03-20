package net.brightroom.uniso.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.brightroom.uniso.domain.plugin.ServicePlugin
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun TutorialScreen(
    services: List<ServicePlugin>,
    onComplete: () -> Unit,
    appIcon: Painter? = null,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current
    val steps = TutorialStep.entries
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 480.dp)
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Step icon
            if (currentStep == TutorialStep.WELCOME && appIcon != null) {
                Image(
                    painter = appIcon,
                    contentDescription = "Uniso",
                    modifier = Modifier.size(64.dp),
                )
            } else {
                Text(
                    text = currentStep.icon,
                    style = TextStyle(fontSize = 48.sp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Service icons row (Welcome step only)
            if (currentStep == TutorialStep.WELCOME && services.isNotEmpty()) {
                ServiceIconsRow(services = services)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            Text(
                text = stringResource(currentStep.titleKey),
                style =
                    TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = stringResource(currentStep.descriptionKey),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Dot indicators
            DotIndicator(
                totalSteps = steps.size,
                currentStep = currentStepIndex,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation buttons
            NavigationButtons(
                currentStepIndex = currentStepIndex,
                totalSteps = steps.size,
                onBack = { currentStepIndex-- },
                onNext = { currentStepIndex++ },
                onSkip = onComplete,
                onComplete = onComplete,
            )
        }
    }
}

@Composable
private fun ServiceIconsRow(services: List<ServicePlugin>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        services.forEach { service ->
            Box(
                modifier =
                    Modifier
                        .size(Dimensions.ServiceIconLg)
                        .clip(RoundedCornerShape(Dimensions.BorderRadiusMd))
                        .background(Color(service.brandColor)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = service.displayName.first().toString(),
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun DotIndicator(
    totalSteps: Int,
    currentStep: Int,
) {
    val colors = AppColors.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentStep) colors.textPrimary else colors.borderSecondary,
                        ),
            )
        }
    }
}

@Composable
private fun NavigationButtons(
    currentStepIndex: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
) {
    val isFirst = currentStepIndex == 0
    val isLast = currentStepIndex == totalSteps - 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isFirst) {
            // Skip button
            TutorialTextButton(
                text = stringResource(StringKey.TUTORIAL_SKIP),
                onClick = onSkip,
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Next button
            TutorialPrimaryButton(
                text = stringResource(StringKey.TUTORIAL_NEXT),
                onClick = onNext,
            )
        } else if (isLast) {
            // Back button
            TutorialTextButton(
                text = stringResource(StringKey.TUTORIAL_BACK),
                onClick = onBack,
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Start button
            TutorialPrimaryButton(
                text = stringResource(StringKey.TUTORIAL_START),
                onClick = onComplete,
            )
        } else {
            // Back button
            TutorialTextButton(
                text = stringResource(StringKey.TUTORIAL_BACK),
                onClick = onBack,
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Next button
            TutorialPrimaryButton(
                text = stringResource(StringKey.TUTORIAL_NEXT),
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun TutorialTextButton(
    text: String,
    onClick: () -> Unit,
) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor = if (isHovered) colors.backgroundTertiary else Color.Transparent

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(bgColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun TutorialPrimaryButton(
    text: String,
    onClick: () -> Unit,
) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val alpha = if (isHovered) 0.85f else 1f

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(colors.textPrimary.copy(alpha = alpha))
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.backgroundPrimary,
        )
    }
}
