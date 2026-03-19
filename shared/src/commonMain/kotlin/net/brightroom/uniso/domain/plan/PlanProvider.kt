package net.brightroom.uniso.domain.plan

interface PlanProvider {
    fun checkAccountLimit(currentCount: Int): Boolean

    fun getCurrentPlan(): PlanInfo

    fun onLimitReached()
}

data class PlanInfo(
    val planType: String,
    val displayName: String,
    val unlimited: Boolean = false,
)
