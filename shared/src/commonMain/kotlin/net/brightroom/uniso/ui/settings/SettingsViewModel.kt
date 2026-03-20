package net.brightroom.uniso.ui.settings

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.brightroom.uniso.data.model.Account
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.plugin.ServicePlugin
import net.brightroom.uniso.domain.plugin.ServicePluginRegistry
import net.brightroom.uniso.domain.settings.AppLocale
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.domain.settings.SettingsRepository

data class SettingsAccount(
    val accountId: String,
    val serviceId: String,
    val serviceName: String,
    val accountName: String,
    val brandColor: Color,
)

class SettingsViewModel(
    private val accountManager: AccountManager,
    private val servicePluginRegistry: ServicePluginRegistry,
    private val i18nManager: I18nManager,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
) {
    private val _accounts = MutableStateFlow<List<SettingsAccount>>(emptyList())
    val accounts: StateFlow<List<SettingsAccount>> = _accounts.asStateFlow()

    val currentLocale: StateFlow<AppLocale> = i18nManager.currentLocale

    private val _telemetryEnabled = MutableStateFlow(false)
    val telemetryEnabled: StateFlow<Boolean> = _telemetryEnabled.asStateFlow()

    private val _deleteTarget = MutableStateFlow<SettingsAccount?>(null)
    val deleteTarget: StateFlow<SettingsAccount?> = _deleteTarget.asStateFlow()

    private val pluginCache = mutableMapOf<String, ServicePlugin>()

    init {
        _telemetryEnabled.value = settingsRepository.getBoolean(TELEMETRY_KEY) ?: false

        scope.launch {
            accountManager.accounts.collect { accounts ->
                _accounts.value = accounts.map { it.toSettingsAccount() }
            }
        }
    }

    fun setLocale(locale: AppLocale) {
        i18nManager.setLocale(locale)
    }

    fun setTelemetryEnabled(enabled: Boolean) {
        settingsRepository.setBoolean(TELEMETRY_KEY, enabled)
        _telemetryEnabled.value = enabled
    }

    fun updateDisplayName(
        accountId: String,
        name: String,
    ) {
        accountManager.updateDisplayName(accountId, name)
    }

    fun moveAccountUp(accountId: String) {
        val list = _accounts.value
        val index = list.indexOfFirst { it.accountId == accountId }
        if (index > 0) {
            val above = list[index - 1]
            accountManager.swapSortOrder(accountId, above.accountId)
        }
    }

    fun moveAccountDown(accountId: String) {
        val list = _accounts.value
        val index = list.indexOfFirst { it.accountId == accountId }
        if (index >= 0 && index < list.size - 1) {
            val below = list[index + 1]
            accountManager.swapSortOrder(accountId, below.accountId)
        }
    }

    fun requestDeleteAccount(account: SettingsAccount) {
        _deleteTarget.value = account
    }

    fun dismissDeleteDialog() {
        _deleteTarget.value = null
    }

    fun confirmDeleteAccount(onWebViewCleanup: (String) -> Unit) {
        val target = _deleteTarget.value ?: return
        onWebViewCleanup(target.accountId)
        accountManager.removeAccount(target.accountId)
        _deleteTarget.value = null
    }

    private fun getPlugin(serviceId: String): ServicePlugin? =
        pluginCache.getOrPut(serviceId) {
            servicePluginRegistry.getById(serviceId) ?: return null
        }

    private fun Account.toSettingsAccount(): SettingsAccount {
        val plugin = getPlugin(serviceId)
        return SettingsAccount(
            accountId = accountId,
            serviceId = serviceId,
            serviceName = plugin?.displayName ?: serviceId,
            accountName = displayName ?: plugin?.displayName ?: serviceId,
            brandColor = plugin?.let { Color(it.brandColor) } ?: Color.Gray,
        )
    }

    fun resetTutorial() {
        settingsRepository.setBoolean(TUTORIAL_COMPLETED_KEY, false)
    }

    companion object {
        private const val TELEMETRY_KEY = "telemetry_enabled"
        private const val TUTORIAL_COMPLETED_KEY = "tutorial_completed"
    }
}
