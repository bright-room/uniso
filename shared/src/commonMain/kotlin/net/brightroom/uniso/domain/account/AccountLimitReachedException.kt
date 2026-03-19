package net.brightroom.uniso.domain.account

import net.brightroom.uniso.domain.plan.PlanInfo

class AccountLimitReachedException(
    val planInfo: PlanInfo,
) : Exception("Account limit reached for plan: ${planInfo.displayName}")
