package net.brightroom.uniso.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.brightroom.uniso.Constants
import net.brightroom.uniso.domain.settings.AppLocale
import net.brightroom.uniso.domain.settings.StringKey
import net.brightroom.uniso.ui.stringResource
import net.brightroom.uniso.ui.theme.AppColors
import net.brightroom.uniso.ui.theme.Dimensions

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
    onWebViewCleanup: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.current
    val accounts by viewModel.accounts.collectAsState()
    val currentLocale by viewModel.currentLocale.collectAsState()
    val telemetryEnabled by viewModel.telemetryEnabled.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary),
    ) {
        // Header
        SettingsHeader(onClose = onClose)

        // Separator
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(Dimensions.BorderWidthThin)
                    .background(colors.borderTertiary),
        )

        // Scrollable content
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 24.dp)
                    .widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Account Management Section
            AccountManagementSection(
                accounts = accounts,
                onUpdateDisplayName = { id, name -> viewModel.updateDisplayName(id, name) },
                onMoveUp = { viewModel.moveAccountUp(it) },
                onMoveDown = { viewModel.moveAccountDown(it) },
                onDelete = { viewModel.requestDeleteAccount(it) },
            )

            // General Section
            GeneralSection(
                currentLocale = currentLocale,
                onLocaleChange = { viewModel.setLocale(it) },
            )

            // Privacy Section
            PrivacySection(
                telemetryEnabled = telemetryEnabled,
                onTelemetryChange = { viewModel.setTelemetryEnabled(it) },
            )

            // Keyboard Shortcuts Section
            KeyboardShortcutsSection()

            // Application Info Section
            ApplicationInfoSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsHeader(onClose: () -> Unit) {
    val colors = AppColors.current

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Dimensions.HeaderHeight)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(StringKey.SETTINGS_TITLE),
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )

        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val bgColor = if (isHovered) colors.backgroundTertiary else colors.backgroundSecondary

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                    .background(bgColor)
                    .hoverable(interactionSource)
                    .clickable(onClick = onClose)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = stringResource(StringKey.BUTTON_CLOSE),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}

// ── Section: Account Management ─────────────────────────────────────────

@Composable
private fun AccountManagementSection(
    accounts: List<SettingsAccount>,
    onUpdateDisplayName: (String, String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onDelete: (SettingsAccount) -> Unit,
) {
    val colors = AppColors.current

    SectionHeader(stringResource(StringKey.SETTINGS_ACCOUNT_MANAGEMENT))

    if (accounts.isEmpty()) {
        Text(
            text = stringResource(StringKey.SETTINGS_ACCOUNT_MANAGEMENT_EMPTY),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            accounts.forEachIndexed { index, account ->
                AccountManagementItem(
                    account = account,
                    isFirst = index == 0,
                    isLast = index == accounts.size - 1,
                    onUpdateDisplayName = { name -> onUpdateDisplayName(account.accountId, name) },
                    onMoveUp = { onMoveUp(account.accountId) },
                    onMoveDown = { onMoveDown(account.accountId) },
                    onDelete = { onDelete(account) },
                )
            }
        }
    }
}

@Composable
private fun AccountManagementItem(
    account: SettingsAccount,
    isFirst: Boolean,
    isLast: Boolean,
    onUpdateDisplayName: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AppColors.current
    var editingName by remember(account.accountId) { mutableStateOf(account.accountName) }
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(colors.backgroundSecondary)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Service color indicator
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(account.brandColor),
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Editable display name
        Column(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = editingName,
                onValueChange = { editingName = it },
                singleLine = true,
                textStyle =
                    TextStyle(
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                    ),
                cursorBrush = SolidColor(colors.textPrimary),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (isFocused && !focusState.isFocused && editingName != account.accountName) {
                                onUpdateDisplayName(editingName)
                            }
                            isFocused = focusState.isFocused
                        },
            )
            Text(
                text = account.serviceName,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Move up/down buttons
        SmallIconButton(
            text = "\u2191",
            enabled = !isFirst,
            onClick = onMoveUp,
            contentDescription = stringResource(StringKey.SETTINGS_MOVE_UP),
        )
        SmallIconButton(
            text = "\u2193",
            enabled = !isLast,
            onClick = onMoveDown,
            contentDescription = stringResource(StringKey.SETTINGS_MOVE_DOWN),
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Delete button
        SmallIconButton(
            text = "\u2715",
            enabled = true,
            onClick = onDelete,
            contentDescription = stringResource(StringKey.BUTTON_DELETE),
            isDanger = true,
        )
    }
}

@Composable
private fun SmallIconButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    isDanger: Boolean = false,
) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val textColor =
        when {
            !enabled -> colors.textTertiary.copy(alpha = 0.4f)
            isDanger && isHovered -> colors.textDanger
            isHovered -> colors.textPrimary
            else -> colors.textTertiary
        }
    val bgColor = if (enabled && isHovered) colors.backgroundTertiary else colors.backgroundSecondary

    Box(
        modifier =
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(bgColor)
                .hoverable(interactionSource)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
            color = textColor,
        )
    }
}

// ── Section: General ────────────────────────────────────────────────────

@Composable
private fun GeneralSection(
    currentLocale: AppLocale,
    onLocaleChange: (AppLocale) -> Unit,
) {
    val colors = AppColors.current

    SectionHeader(stringResource(StringKey.SETTINGS_GENERAL))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(StringKey.SETTINGS_LANGUAGE),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AppLocale.entries.forEach { locale ->
                LocaleChip(
                    locale = locale,
                    isSelected = locale == currentLocale,
                    onClick = { onLocaleChange(locale) },
                )
            }
        }
    }
}

@Composable
private fun LocaleChip(
    locale: AppLocale,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor =
        when {
            isSelected -> colors.textPrimary
            isHovered -> colors.backgroundTertiary
            else -> colors.backgroundSecondary
        }
    val textColor = if (isSelected) colors.backgroundPrimary else colors.textSecondary

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(Dimensions.BorderRadiusSm))
                .background(bgColor)
                .hoverable(interactionSource)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = locale.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
        )
    }
}

// ── Section: Privacy ────────────────────────────────────────────────────

@Composable
private fun PrivacySection(
    telemetryEnabled: Boolean,
    onTelemetryChange: (Boolean) -> Unit,
) {
    val colors = AppColors.current

    SectionHeader(stringResource(StringKey.SETTINGS_PRIVACY))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(StringKey.SETTINGS_TELEMETRY),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(StringKey.SETTINGS_TELEMETRY_DESCRIPTION),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }

        Switch(
            checked = telemetryEnabled,
            onCheckedChange = onTelemetryChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = colors.backgroundPrimary,
                    checkedTrackColor = colors.textPrimary,
                    uncheckedThumbColor = colors.backgroundPrimary,
                    uncheckedTrackColor = colors.borderSecondary,
                    uncheckedBorderColor = colors.borderSecondary,
                ),
        )
    }
}

// ── Section: Keyboard Shortcuts ─────────────────────────────────────────

@Composable
private fun KeyboardShortcutsSection() {
    val isMac = System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true)
    val mod = if (isMac) "\u2318" else "Ctrl"

    SectionHeader(stringResource(StringKey.SETTINGS_KEYBOARD_SHORTCUTS))

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ShortcutRow(stringResource(StringKey.SETTINGS_SHORTCUT_NEXT), "Ctrl+Tab")
        ShortcutRow(stringResource(StringKey.SETTINGS_SHORTCUT_PREV), "Ctrl+Shift+Tab")
        ShortcutRow(stringResource(StringKey.SETTINGS_SHORTCUT_ADD), "$mod+N")
        ShortcutRow(stringResource(StringKey.SETTINGS_SHORTCUT_SETTINGS), "$mod+,")
        ShortcutRow(stringResource(StringKey.SETTINGS_SHORTCUT_RELOAD), "$mod+R")
        ShortcutRow(stringResource(StringKey.SETTINGS_SHORTCUT_CLOSE), "$mod+W")
    }
}

@Composable
private fun ShortcutRow(
    label: String,
    shortcut: String,
) {
    val colors = AppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
        )
        Text(
            text = shortcut,
            style =
                TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
            color = colors.textTertiary,
            modifier =
                Modifier
                    .border(
                        width = Dimensions.BorderWidthThin,
                        color = colors.borderTertiary,
                        shape = RoundedCornerShape(Dimensions.BorderRadiusSm),
                    ).padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

// ── Section: Application Info ───────────────────────────────────────────

@Composable
private fun ApplicationInfoSection() {
    val colors = AppColors.current

    SectionHeader(stringResource(StringKey.SETTINGS_APP_INFO))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(StringKey.SETTINGS_VERSION),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
        )
        Text(
            text = Constants.APP_VERSION,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )
    }
}

// ── Shared Components ───────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    val colors = AppColors.current

    Text(
        text = title,
        style =
            TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
            ),
        color = colors.textTertiary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
