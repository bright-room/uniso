package net.brightroom.uniso.ui.sidebar

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

class SidebarViewModel(
    private val accountManager: AccountManager,
    private val servicePluginRegistry: ServicePluginRegistry,
    private val scope: CoroutineScope,
) {
    private val _sidebarAccounts = MutableStateFlow<List<SidebarAccount>>(emptyList())
    val sidebarAccounts: StateFlow<List<SidebarAccount>> = _sidebarAccounts.asStateFlow()

    val activeAccountId: StateFlow<String?> = accountManager.activeAccountId

    private val _showAddAccountDialog = MutableStateFlow(false)
    val showAddAccountDialog: StateFlow<Boolean> = _showAddAccountDialog.asStateFlow()

    private val pluginCache = mutableMapOf<String, ServicePlugin>()

    init {
        scope.launch {
            accountManager.accounts.collect { accounts ->
                _sidebarAccounts.value = accounts.map { it.toSidebarAccount() }
            }
        }
    }

    fun onAccountClick(accountId: String) {
        accountManager.setActiveAccount(accountId)
    }

    fun onAddAccountClick() {
        _showAddAccountDialog.value = true
    }

    fun dismissAddAccountDialog() {
        _showAddAccountDialog.value = false
    }

    fun getActiveAccount(): SidebarAccount? {
        val activeId = activeAccountId.value ?: return null
        return _sidebarAccounts.value.find { it.accountId == activeId }
    }

    private fun getPlugin(serviceId: String): ServicePlugin? =
        pluginCache.getOrPut(serviceId) {
            servicePluginRegistry.getById(serviceId) ?: return null
        }

    private fun Account.toSidebarAccount(): SidebarAccount {
        val plugin = getPlugin(serviceId)
        val name = displayName ?: plugin?.displayName ?: serviceId
        return SidebarAccount(
            accountId = accountId,
            serviceId = serviceId,
            serviceName = plugin?.displayName ?: serviceId,
            accountName = name,
            initials = deriveInitials(name),
            brandColor = plugin?.let { Color(it.brandColor) } ?: Color.Gray,
            url = plugin?.domainPatterns?.firstOrNull()?.let { "https://$it/" } ?: "",
        )
    }

    companion object {
        internal fun deriveInitials(name: String): String {
            val cleaned = name.removePrefix("@")
            if (cleaned.isBlank()) return "?"
            val words = cleaned.split(" ", "_", "-").filter { it.isNotBlank() }
            return if (words.size >= 2) {
                "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
            } else {
                cleaned.take(2).uppercase()
            }
        }
    }
}
