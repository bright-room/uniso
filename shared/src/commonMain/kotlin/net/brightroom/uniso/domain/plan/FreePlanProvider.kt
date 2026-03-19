package net.brightroom.uniso.domain.plan

class FreePlanProvider : PlanProvider {
    override fun checkAccountLimit(currentCount: Int): Boolean = true

    override fun getCurrentPlan(): PlanInfo =
        PlanInfo(
            planType = "free",
            displayName = "Free (OSS)",
            unlimited = true,
        )

    override fun onLimitReached() {
        // no-op: OSS版では上限に達しない
    }
}
