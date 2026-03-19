package net.brightroom.uniso.domain.link

import net.brightroom.uniso.data.model.Account

sealed class LinkClassification {
    data class ExternalLink(
        val url: String,
    ) : LinkClassification()

    data class InternalSingleAccount(
        val url: String,
        val account: Account,
    ) : LinkClassification()

    data class InternalMultiAccount(
        val url: String,
        val serviceId: String,
        val accounts: List<Account>,
    ) : LinkClassification()

    data class InternalNoAccount(
        val url: String,
    ) : LinkClassification()

    data class SameDomainNavigation(
        val url: String,
    ) : LinkClassification()
}
