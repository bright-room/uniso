package net.brightroom.uniso.domain.identity

import net.brightroom.uniso.data.model.LocalUser
import net.brightroom.uniso.data.repository.SqlSettingsRepository
import net.brightroom.uniso.domain.account.currentTimestamp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class IdentityManager(
    private val settingsRepository: SqlSettingsRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    fun getOrCreateLocalUserId(): String {
        val existing = settingsRepository.getLocalUser()
        if (existing != null) return existing.id

        val newId = Uuid.random().toString()
        settingsRepository.insertLocalUser(
            LocalUser(
                id = newId,
                createdAt = currentTimestamp(),
            ),
        )
        return newId
    }
}
