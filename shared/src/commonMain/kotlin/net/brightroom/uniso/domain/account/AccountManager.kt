package net.brightroom.uniso.domain.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.brightroom.uniso.data.model.Account
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.domain.plan.PlanProvider
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AccountManager(
    private val accountRepository: AccountRepository,
    private val sessionRepository: SessionRepository,
    private val planProvider: PlanProvider,
    private val clock: Clock = Clock.System,
) {
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _activeAccountId = MutableStateFlow<String?>(null)
    val activeAccountId: StateFlow<String?> = _activeAccountId.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    fun addAccount(serviceId: String): Result<Account> {
        val currentCount = accountRepository.getCount()
        if (!planProvider.checkAccountLimit(currentCount)) {
            planProvider.onLimitReached()
            return Result.failure(AccountLimitReachedException(planProvider.getCurrentPlan()))
        }

        val account =
            Account(
                accountId = Uuid.random().toString(),
                serviceId = serviceId,
                displayName = null,
                avatarUrl = null,
                sortOrder = currentCount,
                createdAt = clock.now().toString(),
            )

        accountRepository.insert(account)
        _accounts.value = accountRepository.getAll()

        if (_activeAccountId.value == null) {
            _activeAccountId.value = account.accountId
        }

        return Result.success(account)
    }

    fun removeAccount(accountId: String): Result<Unit> {
        val currentAccounts = _accounts.value
        val target =
            currentAccounts.find { it.accountId == accountId }
                ?: return Result.success(Unit)

        sessionRepository.deleteAccountState(accountId)
        accountRepository.delete(accountId)

        val updatedAccounts = accountRepository.getAll()
        _accounts.value = updatedAccounts

        if (_activeAccountId.value == target.accountId) {
            _activeAccountId.value = updatedAccounts.firstOrNull()?.accountId
        }

        return Result.success(Unit)
    }

    fun getAccountsByService(serviceId: String): List<Account> = _accounts.value.filter { it.serviceId == serviceId }

    fun setActiveAccount(accountId: String) {
        val exists = _accounts.value.any { it.accountId == accountId }
        if (exists) {
            _activeAccountId.value = accountId
        }
    }

    fun updateDisplayName(
        accountId: String,
        name: String,
    ) {
        accountRepository.updateDisplayName(accountId, name)
        _accounts.value = accountRepository.getAll()
    }

    fun swapSortOrder(
        accountId1: String,
        accountId2: String,
    ) {
        val accounts = _accounts.value
        val a1 = accounts.find { it.accountId == accountId1 } ?: return
        val a2 = accounts.find { it.accountId == accountId2 } ?: return
        accountRepository.updateSortOrder(accountId1, a2.sortOrder)
        accountRepository.updateSortOrder(accountId2, a1.sortOrder)
        _accounts.value = accountRepository.getAll()
    }

    fun switchToNextAccount() {
        val sorted = _accounts.value
        if (sorted.size <= 1) return
        val currentId = _activeAccountId.value ?: return
        val currentIndex = sorted.indexOfFirst { it.accountId == currentId }
        if (currentIndex < 0) return
        val nextIndex = (currentIndex + 1) % sorted.size
        _activeAccountId.value = sorted[nextIndex].accountId
    }

    fun switchToPreviousAccount() {
        val sorted = _accounts.value
        if (sorted.size <= 1) return
        val currentId = _activeAccountId.value ?: return
        val currentIndex = sorted.indexOfFirst { it.accountId == currentId }
        if (currentIndex < 0) return
        val prevIndex = (currentIndex - 1 + sorted.size) % sorted.size
        _activeAccountId.value = sorted[prevIndex].accountId
    }

    fun loadAccounts() {
        _accounts.value = accountRepository.getAll()
    }
}
