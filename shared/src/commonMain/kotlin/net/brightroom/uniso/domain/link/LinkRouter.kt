package net.brightroom.uniso.domain.link

import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.plugin.ServicePlugin
import net.brightroom.uniso.domain.plugin.ServicePluginRegistry
import java.net.URI

class LinkRouter(
    private val servicePluginRegistry: ServicePluginRegistry,
    private val accountManager: AccountManager,
) {
    fun classifyLink(
        url: String,
        sourceAccountId: String,
    ): LinkClassification {
        val targetDomain = extractDomain(url) ?: return LinkClassification.ExternalLink(url)

        // Check if the URL is same domain as source account's service
        val sourceAccount = accountManager.accounts.value.find { it.accountId == sourceAccountId }
        if (sourceAccount != null) {
            val sourcePlugin = servicePluginRegistry.getById(sourceAccount.serviceId)
            if (sourcePlugin != null && matchesDomain(targetDomain, sourcePlugin)) {
                return LinkClassification.SameDomainNavigation(url)
            }
        }

        // Try to match target domain to a registered service
        val targetPlugin =
            servicePluginRegistry.findByDomain(targetDomain)
                ?: return LinkClassification.ExternalLink(url)

        // Find accounts for the matched service
        val accounts = accountManager.getAccountsByService(targetPlugin.serviceId)
        return when (accounts.size) {
            0 -> LinkClassification.InternalNoAccount(url)
            1 -> LinkClassification.InternalSingleAccount(url, accounts.first())
            else -> LinkClassification.InternalMultiAccount(url, targetPlugin.serviceId, accounts)
        }
    }

    fun handleNavigation(
        url: String,
        sourceAccountId: String,
        onExternalLink: (String) -> Unit,
        onSwitchAccount: (accountId: String, url: String) -> Unit,
        onShowAccountSelector: (LinkClassification.InternalMultiAccount) -> Unit,
    ): Boolean {
        val classification = classifyLink(url, sourceAccountId)
        return when (classification) {
            is LinkClassification.SameDomainNavigation -> {
                false
            }

            // allow navigation

            is LinkClassification.ExternalLink -> {
                onExternalLink(classification.url)
                true // cancel navigation
            }

            is LinkClassification.InternalNoAccount -> {
                onExternalLink(classification.url)
                true // cancel navigation
            }

            is LinkClassification.InternalSingleAccount -> {
                onSwitchAccount(classification.account.accountId, classification.url)
                true // cancel navigation
            }

            is LinkClassification.InternalMultiAccount -> {
                onShowAccountSelector(classification)
                true // cancel navigation
            }
        }
    }

    private fun matchesDomain(
        domain: String,
        plugin: ServicePlugin,
    ): Boolean {
        val normalizedDomain = domain.removePrefix("www.").lowercase()
        return plugin.domainPatterns.any { pattern ->
            normalizedDomain == pattern || normalizedDomain.endsWith(".$pattern")
        }
    }

    companion object {
        internal fun extractDomain(url: String): String? =
            try {
                URI(url).host
            } catch (_: Exception) {
                null
            }
    }
}
